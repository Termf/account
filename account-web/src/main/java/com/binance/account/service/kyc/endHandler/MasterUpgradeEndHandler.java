package com.binance.account.service.kyc.endHandler;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.JumioConst;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillInfoRefType;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.certificate.impl.UserKycHelper;
import com.binance.account.service.country.impl.CountryBusiness;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.service.security.impl.FaceBusiness;
import com.binance.account.vo.country.CountryVo;
import com.binance.certification.api.KycCertificateApi;
import com.binance.certification.common.model.KycCertificateVo;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.common.enums.JumioError;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 升级处理器 PROCESS -> PASS/FORBID_PASS (主状态处理中, 所有子状态PASS) REFUSED ->
 * PASS/FORBID_PASS (主状态拒绝，所有子状态PASS)
 */
@Slf4j
@Service
public class MasterUpgradeEndHandler extends AbstractEndHandler {

	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private UserKycApproveMapper userKycApproveMapper;
	@Resource
	private CompanyCertificateMapper companyCertificateMapper;
	@Resource
	private JumioBusiness jumioBusiness;
	@Resource
	private CountryBusiness countryBusiness;
	@Resource
	private KycFillInfoMapper kycFillInfoMapper;
	@Resource
	private FaceBusiness faceBusiness;
	
	@Resource
	private KycCertificateApi certificateApi;

	@Override
	public boolean isDoHandler() {
		KycEndContext context = KycEndContext.getContext();
		KycCertificate kycCertificate = context.getKycCertificate();
		if (kycCertificate == null) {
			log.warn("kyc end context error.");
			return false;
		}
		KycCertificateStatus status = KycCertificateStatus.getByName(kycCertificate.getStatus());
		if (status != KycCertificateStatus.PROCESS && status != KycCertificateStatus.REFUSED) {
			// 当前不是处理中或者拒绝状态下，不能做升级处理
			return false;
		}
		// 检查所有子状态是否都为通过状态
		KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
		// 判断basic是否通过

		if (KycCertificateStatus.PASS != context.getBasicStatus()) {
			return false;
		}
		if (KycCertificateKycType.COMPANY == kycType && context.getGoogleFormStatus() != KycCertificateStatus.PASS) {
			// 企业认证下，googleFrom 必须通过
			return false;
		}
		if (context.getFaceStatus() != KycCertificateStatus.PASS
				&& context.getFaceStatus() != KycCertificateStatus.SKIP) {
			// 人脸识别不是通过或者跳过
			return false;
		}
		if (context.isOcrFlow() && context.getFaceOrcStatus() == KycCertificateStatus.PASS) {
			log.info("执行MasterUpgradeEndHandler 处理器 userId:{}", kycCertificate.getUserId());
			return true;
		} else if (context.getJumioStatus() == KycCertificateStatus.PASS) {
			log.info("执行MasterUpgradeEndHandler 处理器 userId:{}", kycCertificate.getUserId());
			return true;
		}
		// 其他状态都不能通过
		return false;
	}

	@Override
	public void handler() {
		// update kyc_certificate status
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		if (kycCertificate == null) {
			log.error("kyc end context error.");
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		KycEndContext context = KycEndContext.getContext();
		Long userId = context.getUserId();
		KycCertificateKycType kycType = context.getKycType();

		// 验证证件号是否已经被占用，如果已经被占用，则不能审核通过, 企业认证不做校验
		if (KycCertificateKycType.USER == kycType && userCertificateBusiness.isIDNumberOccupied(context.getIdNumber(),
				context.getCountry(), context.getDocumentType(), userId)) {
			log.warn("user kyc pass handler check idNumber be used by other. userId:{}", userId);
			handlerIdNumberBeUsed(kycCertificate, context);
			return;
		}
		// 1. 变更状态
		KycCertificateStatus endStatus = context.isForbidCountry() ? KycCertificateStatus.FORBID_PASS
				: KycCertificateStatus.PASS;
		String messageTips = context.isForbidCountry() ? JumioConst.KYC_PASS_FORBID_COUNTRY_MESSAGE : null;
		int count = updateKycFlowStatus(userId, KycCertificateKycLevel.L2.getCode(), endStatus, messageTips);
		kycCertificate.setStatus(endStatus.name());
		kycCertificate.setMessageTips(messageTips);
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		log.info("kyc pass update kyc status userId:{} kycType:{} count:{}", userId, kycType, count);

		// 2. user kyc 存储 idNumber index
		int saveIdNumber = super.saveIdNumberMapIndex(context);
		log.info("kyc pass save id number map index userId:{} kycType:{} count:{}", userId, kycType, saveIdNumber);

		if (!context.isForbidCountry()) {
			// 3. kyc pass 变更 user status信息
			int userStatus = iUserCertificate.updateCertificateStatus(userId, true);
			log.info("kyc pass change user status userId:{} kycType:{} count:{}", userId, kycType, userStatus);

			// 4. kyc pass 变更 user security level
			int securityLevel = super.updateSecurityLevel(userId, 2);
			log.info("kyc pass change user security level userId:{} kycType:{} count:{}", userId, kycType,
					securityLevel);
			// 如果是企业认证，开启子母账户功能
			if (KycCertificateKycType.COMPANY == kycType) {
				AsyncTaskExecutor.execute(() -> {
					super.enableSubUserFunction(userId);
				});
			}
		}
		// 5. 发送变更邮件
		String emailTemplate = context.isForbidCountry() ? AccountConstants.USER_KYC_PASS_FORBID_COUNTRY_EMAIL_TEMPLATE
				: KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType()) 
				? Constant.JUMIO_COMPANY_CHECK_SUCCESS :Constant.JUMIO_KYC_CHECK_SUCCESS;
		super.sendLevelChangeEmail(userId, context.getLanguage(), kycCertificate.getMessageTips(), emailTemplate,
				"Kyc认证升级邮件");

		// kyc 通过后的处理逻辑
		afterPassHandler(userId, context);
		log.info("用户是否是ForbidCountry. userId:{},isForbidCountry:{}",userId,context.isForbidCountry());
		if (!context.isForbidCountry()) {
			KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
			// Face ocr流程
			if (context.getFaceOrcStatus() != null) {
				UserKycApprove approve = UserKycApprove.toKycApprove(kycCertificate, kycFillInfo, null);
				try {
					FaceIdCardOcrVo ocrVo = faceBusiness.getFaceIdCardOcr(userId);
					if(ocrVo != null) {
						if (StringUtils.isNotBlank(ocrVo.getValidDateStart())) {
							String data = ocrVo.getValidDateStart();
							if (data.contains(".")) {
								data = data.replace(".", "-");
							} else if (data.length() == 8) {
								data = data.substring(0, 4) + "-" + data.substring(4, 6) + "-" + data.substring(6);
							}
							approve.setCertificateIssuingDate(ocrVo.getValidDateStart());
						}
					}
				}catch(Exception e) {
					
				}
				approve.setCertificateFirstName(kycFillInfo.getFirstName());
				approve.setCertificateLastName(kycFillInfo.getLastName());
				approve.setCertificateDob(kycFillInfo.getBirthday());
				approve.setCertificateCountry(kycFillInfo.getCountry());
				userKycApproveMapper.insert(approve);
				context.setUserKycApprove(approve);
			} else {
				JumioInfoVo jumioInfo = jumioBusiness.getLastByUserId(userId);
				UserKycApprove approve = UserKycApprove.toKycApprove(kycCertificate, kycFillInfo, jumioInfo.getScanReference());
				approve.setCertificateFirstName(StringUtils.isBlank(jumioInfo.getFirstName())?"":jumioInfo.getFirstName().trim().replaceAll("N/A", ""));
				approve.setCertificateLastName(StringUtils.isBlank(jumioInfo.getLastName())?"":jumioInfo.getLastName().trim().replaceAll("N/A", ""));
				approve.setCertificateDob(jumioInfo.getDob());
				approve.setCertificateIssuingDate(jumioInfo.getIssuingDate());
				CountryVo countryVo = countryBusiness.getCountryByAlpha3(jumioInfo.getIssuingCountry());
				approve.setCertificateCountry(countryVo.getCode());
				userKycApproveMapper.insert(approve);
				context.setUserKycApprove(approve);
			}

		}


		// 老数据双写
		if (apolloCommonConfig.isKycFlowDoubleWrite()) {
			handlerDataDoubleWrite(userId, kycType, context);
		}
	}

	private void handlerIdNumberBeUsed(KycCertificate kycCertificate, KycEndContext context) {
		if (context.isOcrFlow()) {
			// 把 ocr 结果拒绝
			kycCertificate.setFaceOcrStatus(KycCertificateStatus.REFUSED.name());
			kycCertificate.setFaceOcrTips(JumioError.ID_NUMBER_USED.name());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateFaceOcrStatus(kycCertificate);
			super.refusedFaceOcrByNumberUsed(context.getFaceIdCardOcrVo());
		} else {
			// 把 jumio 结果拒绝
			kycCertificate.setJumioStatus(KycCertificateStatus.REFUSED.name());
			kycCertificate.setJumioTips(JumioError.ID_NUMBER_USED.name());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateJumioStatus(kycCertificate);
			super.refusedJumioByNumberUsed(context.getJumioInfoVo());
		}
		if (!KycCertificateStatus.SKIP.name().equalsIgnoreCase(kycCertificate.getFaceStatus())) {
			kycCertificate.setFaceStatus(KycCertificateStatus.REFUSED.name());
			kycCertificate.setFaceTips(JumioError.ID_NUMBER_USED.name());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateFaceStatus(kycCertificate);
		}
		// 把人脸识别也进行拒绝且清除人脸识别到关联照片
		iFace.removeFaceReferenceRefImage(context.getUserId());
	}

	private void afterPassHandler(Long userId, KycEndContext context) {
		// 6. 清理缓存
		UserKycHelper.clearKycCountryCache(userId);

		// 7. 把人脸识别的检查照片同步到业务正常通过的对比照片信息中
		boolean saveRefImage = iFace.saveFaceReferenceRefImage(userId);
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		if (saveRefImage) {
			FaceTransType transType = KycCertificateKycType.USER == context.getKycType() ? FaceTransType.KYC_USER
					: FaceTransType.KYC_COMPANY;
			KycFillInfo fillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, transType.name(), null);
			String refId = null;
			String transId = null;
			if (fillInfo != null
					&& StringUtils.equalsIgnoreCase(KycFillInfoRefType.WITHDRAW_FACE.name(), fillInfo.getRefType())) {
				refId = fillInfo.getRefId();
			}
			if (faceLog != null) {
				transId = faceLog.getTransId();
			}
			log.info("更新人脸识别正常通过的对比照片信息成功, 触发检测是否需要提币人脸流程: userId:{} transId:{} refId:{}", userId, transId, refId);
			iUserCertificate.kycPassCheckSecurityFaceCheck(userId, transId, transType, refId,kycCertificate.getStatus(),kycCertificate.getUpdateTime());
		}
		// 8. 如果是中国用户，且ocr通过，则进行创建法币账户
		if (apolloCommonConfig.isKycPassCreateFiatAccount() && KycCertificateKycType.USER == context.getKycType()
			//	&& StringUtils.equalsIgnoreCase("CN", context.getCountry())
			) {

			log.info("中国用户,开通法币账户 userId:{}", userId);

			// 如果用户的ocr通过了，则进行创建法币账户
			FaceIdCardOcrVo ocrVo = context.getFaceIdCardOcrVo();
			if (ocrVo == null) {
				ocrVo = iFace.getFaceIdCardOcr(userId);
			}
			log.info("中国用户,开通法币账户.获取faceIdOcr userId:{},idCardStatus:{}", userId,
					ocrVo == null ? null : ocrVo.getStatus() == null ? null : ocrVo.getStatus());
			if (ocrVo != null && IdCardOcrStatus.PASS == ocrVo.getStatus()) {
				UserKycHelper.createFiatAccount(userId, true);
			}
		}
		
		certificateToSendKycChangeMsg(kycCertificate);
	}

	private void handlerDataDoubleWrite(Long userId, KycCertificateKycType kycType, KycEndContext context) {
		KycCertificate kycCertificate = context.getKycCertificate();
		KycCertificateStatus status = KycCertificateStatus.getByName(kycCertificate.getStatus());
		KycStatus kycStatus = null;
		CompanyCertificateStatus companyCertificateStatus = null;
		if (KycCertificateStatus.PASS == status) {
			kycStatus = KycStatus.passed;
			companyCertificateStatus = CompanyCertificateStatus.passed;
		} else if (KycCertificateStatus.FORBID_PASS == status) {
			kycStatus = KycStatus.forbidPassed;
			companyCertificateStatus = CompanyCertificateStatus.forbidPassed;
		} else {
			log.warn("kyc certificate status is not pass. userId:{} status:{}", userId, status);
			return;
		}
		if (KycCertificateKycType.USER == kycType) {
			// 个人认证双写
			UserKyc userKyc = userKycMapper.getLast(userId);
			if (userKyc == null || KycStatus.isEndStatus(userKyc.getStatus())) {
				log.warn("当前老 user kyc 数据获取失败或者已经不是处理中，不进行双写变更. userId:{}", userId);
				return;
			}
			// 变更到通过状态
			userKyc.setStatus(kycStatus);
			userKyc.setFailReason(kycCertificate.getMessageTips());
			userKyc.setUpdateTime(DateUtils.getNewUTCDate());
			userKycMapper.updateStatus(userKyc);
			if (!context.isForbidCountry()) {
				log.info("save old user kyc pass approve data. userId:{}", userId);
				userKycApproveMapper.insert(UserKycApprove.toKycApprove(userKyc));
			}
		} else {
			// 企业认证双写
			CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userId);
			if (companyCertificate == null || CompanyCertificateStatus.isEndStatus(companyCertificate.getStatus())) {
				log.warn("当前 company kyc 数据获取失败或者已经不是处理中，不进行双写变更. userId:{}", userId);
				return;
			}
			companyCertificate.setStatus(companyCertificateStatus);
			companyCertificate.setInfo(kycCertificate.getMessageTips());
			companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			companyCertificateMapper.updateByPrimaryKey(companyCertificate);
			if (!context.isForbidCountry()) {
				userKycApproveMapper.insert(UserKycApprove.toKycApprove(companyCertificate));
			}
		}
	}
	
	 private void certificateToSendKycChangeMsg(KycCertificate kycCertificate) {
	    	if(!KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
	    		return;
	    	}
	    	try {
	    		KycCertificateVo certificate = new KycCertificateVo();
	    		BeanUtils.copyProperties(kycCertificate, certificate);
	    		certificate.setKycType(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType()) ? com.binance.certification.common.enums.KycCertificateKycType.COMPANY:
	    			com.binance.certification.common.enums.KycCertificateKycType.USER);
	    		certificateApi.sendKycChangeMq(APIRequest.instance(certificate));
	    	}catch(Exception e) {
	    		log.warn("调用certificate发送kyc mq异常 userId:{}",kycCertificate.getUserId(),e);
	    	}
	    }
}
