package com.binance.account.mq;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.impl.RiskManageBusiness;
import com.binance.account.service.security.impl.UserSecurityResetBusiness;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.utils.DateUtils;
import com.binance.risk.vo.OperationType;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author liliang1
 * @date 2018-09-29 18:44
 */
@Log4j2
@Component
public class ResetJumioInfoMsgExecutor {

	@Resource
	private UserSecurityResetMapper userSecurityResetMapper;
	@Resource
	private CountryMapper countryMapper;
	@Resource
	private UserSecurityResetBusiness userSecurityResetBusiness;
	@Resource
	private RiskManageBusiness riskManageBusiness;
	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Resource
	private IUserFace iUserFace;

	public String executor(Long userId, String resetId, JumioHandlerType handlerType, JumioInfoVo jumioInfoVo) {
		UserSecurityResetType type = UserSecurityResetType.getByName(handlerType.getCode());
		if (type == null) {
			return "类型错误";
		}
		if (!JumioStatus.isEndStatus(jumioInfoVo.getStatus())) {
			// JUMIO 的状态不是最终态时，先不做处理
			log.info("JUMIO结果处理还未到最终态, userId:{} resetId:{} type:{}", userId, resetId, jumioInfoVo.getHandlerType());
			return "状态不处于终态";
		}
		// 先根据ID 获取对应的记录
		UserSecurityReset reset = userSecurityResetBusiness.getFromMasterDbById(resetId);
		if (reset == null || !userId.equals(reset.getUserId()) || reset.getType() != type) {
			log.info("JUMIO结果处理 获取重置流程记录失败. userId:{} resetId:{}, type:{}", userId, resetId, type);
			return "根据ID获取到的重置记录信息有误";
		}
		// 如果重置记录的状态已经是在JUMIO状态之后的，不需要再次处理。
		if (!UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
			log.info("JUMIO结果处理 重置记录状态不是处理中状态，不能再根据消息进行状态数据修改. userId:{} resetId:{} type:{} status:{}", userId, resetId,
					type, reset.getStatus());
			return "已经结束流程";
		}
		if (!StringUtils.equalsIgnoreCase(reset.getScanReference(), jumioInfoVo.getScanReference())) {
			log.info("JUMIO结果处理 当前重置记录的JUMIO唯一标识已经不能配匹，不做处理. userId:{} resetId:{} scanRef:{} jumioInfoScanRefer:{}",
					userId, resetId, reset.getScanReference(), jumioInfoVo.getScanReference());
			return "重置记录的JUMIO唯一标识不一致";
		}
		// 先把照片设置到对象中
		reset.setFront(jumioInfoVo.getFront());
		reset.setBack(jumioInfoVo.getBack());
		reset.setHand(jumioInfoVo.getFace());
		reset.setDocumentType(jumioInfoVo.getDocumentType());
		reset.setIdNumber(jumioInfoVo.getNumber());
		reset.setIssuingCountry(countryConvertedTo(resetId, jumioInfoVo.getIssuingCountry()));
		// 根据JUMIO_INFO 信息判断是否能通过或者拒绝
		setStatusByJumio(userId, resetId, jumioInfoVo, reset);
		reset.setUpdateTime(DateUtils.getNewUTCDate());
		// 变更修改数据
		userSecurityResetMapper.updateJumioInfo(reset);

		// 根据resetModel的状态是否为通过，或者拒绝，进行发送邮件
		if (UserSecurityResetStatus.passed.equals(reset.getStatus())
				|| UserSecurityResetStatus.refused.equals(reset.getStatus())) {
			log.info("JUMIO结果处理 由于用户重置流程进入通过或者拒绝状态，直接发送邮件通知. userId:{} resetId:{}", userId, resetId);
			userSecurityResetBusiness.sendResetAuthEmail(reset.getStatus(), reset, reset.getFailReason(), true);
		}
		// 如果是jumio 通过状态，检查是否需要发起人脸识别，如果人脸识别已经通过，检查是否能自动审核通过
		if (StringUtils.equalsIgnoreCase(UserSecurityResetStatus.jumioPassed.name(), reset.getJumioStatus())) {
			log.info("JUMIO结果处理 JUMIO通过后检查是否需要发起人脸或自动通过: userId:{} resetId:{}", userId, resetId);
			checkFaceAndAutoPass(userId, resetId, reset);
		}
		// 触发下风控的调用登记
		idNumberRiskHandler(userId, reset, jumioInfoVo);
		return "处理成功";
	}

	public String executorWithLock(UserSecurityReset reset, JumioInfoVo jumioInfoVo) {
		Long userId = reset.getUserId();
		String resetId = reset.getId();
		UserSecurityResetType type = reset.getType();
		if (!JumioStatus.isEndStatus(jumioInfoVo.getStatus())) {
			// JUMIO 的状态不是最终态时，先不做处理
			log.info("JUMIO结果处理还未到最终态, userId:{} resetId:{} type:{}", userId, resetId, jumioInfoVo.getHandlerType());
			return "状态不处于终态";
		}

		// 如果重置记录的状态已经是在JUMIO状态之后的，不需要再次处理。
		if (!UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
			log.info("JUMIO结果处理 重置记录状态不是处理中状态，不能再根据消息进行状态数据修改. userId:{} resetId:{} type:{} status:{}", userId, resetId,
					type, reset.getStatus());
			return "已经结束流程";
		}

		// 先把照片设置到对象中
		reset.setFront(jumioInfoVo.getFront());
		reset.setBack(jumioInfoVo.getBack());
		reset.setHand(jumioInfoVo.getFace());
		reset.setDocumentType(jumioInfoVo.getDocumentType());
		reset.setIdNumber(jumioInfoVo.getNumber());
		reset.setIssuingCountry(countryConvertedTo(resetId, jumioInfoVo.getIssuingCountry()));
		// 根据JUMIO_INFO 信息判断是否能通过或者拒绝
		setStatusByJumio(userId, resetId, jumioInfoVo, reset);
		reset.setUpdateTime(DateUtils.getNewUTCDate());
		// 变更修改数据
		userSecurityResetMapper.updateJumioInfo(reset);

		// 根据resetModel的状态是否为通过，或者拒绝，进行发送邮件
		if (UserSecurityResetStatus.passed.equals(reset.getStatus())
				|| UserSecurityResetStatus.refused.equals(reset.getStatus())) {
			log.info("JUMIO结果处理 由于用户重置流程进入通过或者拒绝状态，直接发送邮件通知. userId:{} resetId:{}", userId, resetId);
			userSecurityResetBusiness.sendResetAuthEmail(reset.getStatus(), reset, reset.getFailReason(), true);
		}
//		// 如果是jumio 通过状态，检查是否需要发起人脸识别，如果人脸识别已经通过，检查是否能自动审核通过
//		if (StringUtils.equalsIgnoreCase(UserSecurityResetStatus.jumioPassed.name(), reset.getJumioStatus())) {
//			log.info("JUMIO结果处理 JUMIO通过后检查是否需要发起人脸或自动通过: userId:{} resetId:{}", userId, resetId);
//			checkFaceAndAutoPass(userId, resetId, reset);
//		}
		// 触发下风控的调用登记
		idNumberRiskHandler(userId, reset, jumioInfoVo);
		return "处理成功";
	}

	public String executorWithLockFromFaceOcr(UserSecurityReset reset, FaceIdCardOcrVo faceIdCardOcrVo) {
		Long userId = reset.getUserId();
		String resetId = reset.getId();
		UserSecurityResetType type = reset.getType();
		if (IdCardOcrStatus.PROCESS.equals(faceIdCardOcrVo.getStatus())
				|| IdCardOcrStatus.REVIEW.equals(faceIdCardOcrVo.getStatus())) {
			// JUMIO 的状态不是最终态时，先不做处理
			log.info("FACE OCR 结果处理还未到最终态, userId:{} resetId:{} type:{}", userId, resetId, reset.getType());
			return "状态不处于终态";
		}

		// 如果重置记录的状态已经是在JUMIO状态之后的，不需要再次处理。
		if (!UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
			log.info("JUMIO结果处理 重置记录状态不是处理中状态，不能再根据消息进行状态数据修改. userId:{} resetId:{} type:{} status:{}", userId, resetId,
					type, reset.getStatus());
			return "已经结束流程";
		}

		// 先把照片设置到对象中
		reset.setFront(faceIdCardOcrVo.getFront());
		reset.setBack(faceIdCardOcrVo.getBack());
		reset.setHand(faceIdCardOcrVo.getFace());
		reset.setDocumentType("ID_CARD");
		reset.setIdNumber(faceIdCardOcrVo.getIdcardNumber());
		reset.setIssuingCountry("CN");
		// 根据JUMIO_INFO 信息判断是否能通过或者拒绝
		setStatusByIdCard(userId, resetId, faceIdCardOcrVo, reset);
		reset.setUpdateTime(DateUtils.getNewUTCDate());
		// 变更修改数据
		userSecurityResetMapper.updateJumioInfo(reset);

		// 根据resetModel的状态是否为通过，或者拒绝，进行发送邮件
		if (UserSecurityResetStatus.passed.equals(reset.getStatus())
				|| UserSecurityResetStatus.refused.equals(reset.getStatus())) {
			log.info("JUMIO结果处理 由于用户重置流程进入通过或者拒绝状态，直接发送邮件通知. userId:{} resetId:{}", userId, resetId);
			userSecurityResetBusiness.sendResetAuthEmail(reset.getStatus(), reset, reset.getFailReason(), true);
		}
		// 如果是jumio 通过状态，检查是否需要发起人脸识别，如果人脸识别已经通过，检查是否能自动审核通过
		if (StringUtils.equalsIgnoreCase(UserSecurityResetStatus.jumioPassed.name(), reset.getJumioStatus())) {
			log.info("JUMIO结果处理 JUMIO通过后检查是否需要发起人脸或自动通过: userId:{} resetId:{}", userId, resetId);
			checkFaceAndAutoPass(userId, resetId, reset);
		}
		// 触发下风控的调用登记
		idNumberRiskHandler(userId, reset, faceIdCardOcrVo);
		return "处理成功";
	}

	/**
	 * 检查是否需要发起人脸识别，
	 * 
	 * @param userId
	 * @param transId
	 * @param reset
	 */
	private void checkFaceAndAutoPass(Long userId, String transId, UserSecurityReset reset) {
		FaceTransType transType = FaceTransType.getByCode(reset.getType().name());
		TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(transId, transType.name());
		if (faceLog == null) {
			log.info("JUMIO结果处理 当前重置流程还未发起人脸识别，进行发起人脸识别。userId:{} transId:{} faceTransType:{}", userId, transId,
					transType);
			iUserFace.initFaceFlowByTransId(transId, userId, transType, true, false);
		} else {
			if (TransFaceLogStatus.PASSED == faceLog.getStatus()) {
				// 人脸识别已经通过的情况下，直接检查是否能自动通过
				log.info("JUMIO结果处理 当前重置流程的人脸识别已经通过，直接尝试进行自动通过逻辑. userId:{} transId:{}", userId, transId);
				userSecurityResetBusiness.autoPassResetHandler(userId, transId, reset.getJumioIp(), TerminalEnum.OTHER);
			} else {
				log.info("JUMIO结果处理 当前重置流程已经发起人脸识别了，不需再次发起. userId:{} transId:{} faceTransType:{}", userId, transId,
						transType);
			}
		}
	}

	private void idNumberRiskHandler(final Long userId, final UserSecurityReset resetModel,
			final JumioInfoVo jumioInfoVo) {
		// 额外操作，先触发下风控规则的调用
		AsyncTaskExecutor.execute(() -> {
			// 添加face索引
			riskManageBusiness.indexFaceIfNeeded(resetModel.getFront());
			// 触发下风控的调用登记
			final String documentType = jumioInfoVo.getDocumentType();
			final String number = jumioInfoVo.getNumber();
			final String issuingCountry = resetModel.getIssuingCountry();
			OperationType operationType = null;
			switch (resetModel.getType()) {
			case mobile:
				operationType = OperationType.MOBILE_RESET;
				break;
			case google:
				operationType = OperationType.GOOGLE_RESET;
				break;
			case enable:
				operationType = OperationType.ENABLE_RESET;
				break;
			default:
				operationType = null;
				break;
			}
			riskManageBusiness.checkIdNumberBackList(userId, documentType, number, issuingCountry, operationType,
					resetModel.getHand());
		});
	}
	
	private void idNumberRiskHandler(final Long userId, final UserSecurityReset resetModel,
			final FaceIdCardOcrVo faceIdCardOcrVo) {
		// 额外操作，先触发下风控规则的调用
		AsyncTaskExecutor.execute(() -> {
			// 添加face索引
			riskManageBusiness.indexFaceIfNeeded(resetModel.getFront());
			// 触发下风控的调用登记
			final String documentType = "ID_CARD";
			final String number = faceIdCardOcrVo.getIdcardNumber();
			final String issuingCountry = "CN";
			OperationType operationType = null;
			switch (resetModel.getType()) {
			case mobile:
				operationType = OperationType.MOBILE_RESET;
				break;
			case google:
				operationType = OperationType.GOOGLE_RESET;
				break;
			case enable:
				operationType = OperationType.ENABLE_RESET;
				break;
			default:
				operationType = null;
				break;
			}
			riskManageBusiness.checkIdNumberBackList(userId, documentType, number, issuingCountry, operationType,
					resetModel.getHand());
		});
	}

	private String countryConvertedTo(String resetId, String jumioCountry) {
		if (StringUtils.isBlank(jumioCountry)) {
			return null;
		}
		Country country = countryMapper.selectByCode2(jumioCountry);
		if (country == null) {
			log.info("jumio country:{} 不再本地国家列表中. resetId:{}", jumioCountry, resetId);
			return jumioCountry;
		} else {
			return country.getCode();
		}
	}

	private void setStatusByJumio(Long userId, String resetId, JumioInfoVo jumioInfoVo, UserSecurityReset resetModel) {
		JumioStatus jumioStatus = jumioInfoVo.getStatus();
		if (JumioStatus.REFUED.equals(jumioStatus)) {
			// 拒绝
			log.info("JUMIO 模块审核拒绝, 直接进入拒绝状态. userId:{} resetId:{}", userId, resetId);
			resetModel.setStatus(UserSecurityResetStatus.refused);
			resetModel.setFailReason(jumioInfoVo.getFailReason());
			resetModel.setJumioStatus(UserSecurityResetStatus.jumioRefused.name());
		} else if (JumioStatus.PASSED.equals(jumioStatus)) {
			log.info("JUMIO 模块审核通过, 直接进入JUMIO_PASSED状态. userId:{} resetId:{}", userId, resetId);
			resetModel.setStatus(UserSecurityResetStatus.jumioPassed);
			resetModel.setJumioStatus(UserSecurityResetStatus.jumioPassed.name());
		} else {
			// 设置到过期
			log.info("JUMIO 模块审核不是过期和通过下, 设置为过期取消状态, userId:{}, resetId:{}", userId, resetId);
			resetModel.setStatus(UserSecurityResetStatus.cancelled);
			resetModel.setJumioStatus(UserSecurityResetStatus.jumioRefused.name());
		}
	}
	
	private void setStatusByIdCard(Long userId, String resetId, FaceIdCardOcrVo faceIdCardOcrVo, UserSecurityReset resetModel) {
		IdCardOcrStatus faceIdCardStatus = faceIdCardOcrVo.getStatus();
		if (IdCardOcrStatus.REFUSED.equals(faceIdCardStatus)) {
			// 拒绝
			log.info("FACE OCR 模块审核拒绝, 直接进入拒绝状态. userId:{} resetId:{}", userId, resetId);
			resetModel.setStatus(UserSecurityResetStatus.refused);
			resetModel.setFailReason(faceIdCardOcrVo.getFailReason());
			resetModel.setJumioStatus(UserSecurityResetStatus.jumioRefused.name());
		} else if (IdCardOcrStatus.PASS.equals(faceIdCardOcrVo.getStatus())) {
			log.info("FACE OCR 模块审核通过, 直接进入JUMIO_PASSED状态. userId:{} resetId:{}", userId, resetId);
			resetModel.setStatus(UserSecurityResetStatus.jumioPassed);
			resetModel.setJumioStatus(UserSecurityResetStatus.jumioPassed.name());
		} else {
			// 设置到过期
			log.info("JUMIO 模块审核不是过期和通过下, 设置为过期取消状态, userId:{}, resetId:{}", userId, resetId);
			resetModel.setStatus(UserSecurityResetStatus.cancelled);
			resetModel.setJumioStatus(UserSecurityResetStatus.jumioRefused.name());
		}
	}
}
