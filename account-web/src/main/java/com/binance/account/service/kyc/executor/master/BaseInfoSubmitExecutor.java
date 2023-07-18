package com.binance.account.service.kyc.executor.master;

import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillInfoRefType;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.KycFillInfoHistory;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.service.certificate.impl.UserCertificateBusiness;
import com.binance.account.service.certificate.impl.UserKycBusiness;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.kyc.validator.BaseInfoSubmitValidator;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.certification.api.KycCertificateApi;
import com.binance.certification.common.model.KycCertificateVo;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Objects;

@Service
@Log4j2
public class BaseInfoSubmitExecutor extends AbstractKycFlowCommonExecutor {

	@Autowired
	private BaseInfoSubmitValidator validator;

	@Resource
	private UserSecurityMapper userSecurityMapper;

	@Resource
	private UserKycApproveMapper userKycApproveMapper;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;

	@Resource
	private CompanyCertificateMapper companyCertificateMapper;

	@Resource
	private JumioMapper jumioMapper;

	@Resource
	private UserCertificateBusiness userCertificateBusiness;

	@Resource
	private UserKycBusiness userKycBusiness;

	@Autowired
	private ApolloCommonConfig config;
	
	@Resource
	private KycCertificateApi certificateApi;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {

		BaseInfoRequest baseInfo = (BaseInfoRequest) kycFlowRequest;

		validator.validateApiRequest(baseInfo);

		BaseInfoResponse response = new BaseInfoResponse();
		response.setKycType(baseInfo.getKycType());

		Long userId = kycFlowRequest.getUserId();

		log.info("KYC Base info submit ==> user request kyc basic submit by userId:{}", userId);
		try {

			UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);

			//是否已经通过KYC，已经通过的不能再提交
			if(userKycApproveMapper.selectByPrimaryKey(userId)!=null){
				log.info("用户已经KYC验证通过，无需再次验证. userId:{}", userId);
				throw new BusinessException(GeneralCode.USER_KYC_PASSED);
			}
			switch (baseInfo.getKycType()) {
			case USER:
				handleUser(baseInfo, userSecurity);
				break;
			case COMPANY:
				handleCompany(baseInfo, userSecurity);
				break;
			default:
				break;
			}
			
			certificateToSendKycChangeMsg(KycFlowContext.getContext().getKycCertificate());

			return response;

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error(String.format("提交基本信息处理异常 userId:%s", userId), e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	/**
	 * 处理个人流程
	 *
	 * @param baseInfo
	 * @param userSecurity
	 * @throws Exception
	 */
	private void handleUser(BaseInfoRequest baseInfo, UserSecurity userSecurity) throws Exception {
		Long userId = baseInfo.getUserId();
		// 老流程使用userKyc逻辑
		/**
		 * C2C已经使用executor处理器逻辑，所以executor 当初用的是userKyc逻辑而非kycCertificate，所以通过开关判断。
		 */
		long tempUser = userId == null ? 0l:userId.longValue();
//		if (!config.isKycUseNewFlowSwitch() || !(tempUser%100 <config.getKycUseNewFlowThreshold())) {
//			// 是否已经通过KYC
//			UserKyc oldUserKyc = userKycMapper.getLast(userId);
//			// 获取用户当前的KYC认证状态,包含正在进行的个人认证和企业认证
//			userKycBusiness.validateHadReviewKyc(userId, oldUserKyc);
//
//			UserKyc newUserKyc = saveUseKyc(userId, baseInfo, oldUserKyc, userSecurity);
//			KycFlowContext.getContext().setUserKyc(newUserKyc);
//			return;
//		}
		log.info("用户kyc执行新流程. userId:{}",tempUser);
		// 新流程
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		UserKyc userKyc = userKycMapper.getLast(userId);
		// 之前做过userkyc. check一下状态是否正确
		// 防止已过kyc用户 在新流程上提交kyc
		if (userKyc != null && kycCertificate == null) {
			userKycBusiness.validateHadReviewKyc(userId, userKyc);
		}

		if (kycCertificate != null) {
			validator.validateKycCertificateStatus(kycCertificate);
			validator.validateRequestCount(kycCertificate);
		}
		// 保存 kyc_certificate 信息
		initKycBaseInfoHandler(baseInfo, kycCertificate, userSecurity);

		// 双写老流程
//		if (doubleWrite()) {
//			UserKyc newUserKyc = saveUseKyc(userId, baseInfo, userKyc, userSecurity);
//			KycFlowContext.getContext().setUserKyc(newUserKyc);
//		}
		return;
	}

	/**
	 * 处理个人流程
	 *
	 * @param baseInfo
	 * @param userSecurity
	 * @throws Exception
	 */
	private void handleCompany(BaseInfoRequest baseInfo, UserSecurity userSecurity) throws Exception {
		// C2C不涉及企业，所以不需要通过开关考虑老逻辑

		// 新流程
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(baseInfo.getUserId());

		CompanyCertificate companyCertificate = this.companyCertificateMapper.getLast(baseInfo.getUserId());
		if (companyCertificate != null && kycCertificate  == null) {
			userCertificateBusiness.validateHadReviewKyc(baseInfo.getUserId(), companyCertificate);
		}

		if (kycCertificate != null) {
			validator.validateKycCertificateStatus(kycCertificate);
			validator.validateRequestCount(kycCertificate);
		}

		initKycBaseInfoHandler(baseInfo, kycCertificate, userSecurity);
		// 双写老流程
//		if (doubleWrite()) {
//			companyCertificate = saveCompany(baseInfo.getUserId(), baseInfo, companyCertificate, userSecurity);
//			KycFlowContext.getContext().setCompanyCertificate(companyCertificate);
//		}
		return;

	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void initKycBaseInfoHandler(BaseInfoRequest baseInfoRequest, KycCertificate kycCertificate,
			UserSecurity userSecurity) throws ParseException {
		Long userId = baseInfoRequest.getUserId();
		if (kycCertificate == null) {
			kycCertificate = KycCertificateConvertor.convert2KycCertificate(baseInfoRequest);
//			kycCertificate.setGoogleFormStatus(null);
			kycCertificate.setBaseFillStatus(KycCertificateStatus.PASS.name());
			nextStatus(kycCertificate, baseInfoRequest);
			kycCertificate.setFlowDefine("master");
			kycCertificateMapper.insert(kycCertificate);
		} else {
			// KYC_TYPE 请求和db 不相同，更改kycType
			kycCertificate.setKycType(baseInfoRequest.getKycType().getCode());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificate.setStatus(KycCertificateStatus.PROCESS.name());
			kycCertificate.setMessageTips(null);
			kycCertificate.setBaseFillStatus(KycCertificateStatus.PASS.name());
			kycCertificate.setAddressStatus(null);
			kycCertificate.setAddressTips(null);
			kycCertificate.setBindMobile(null);
			kycCertificate.setMobileCode(null);
			if (KycCertificateKycType.COMPANY.equals(baseInfoRequest.getKycType())) {
				kycCertificate.setGoogleFormStatus(KycCertificateStatus.REVIEW.name());
			}else {
				kycCertificate.setGoogleFormStatus(null);
			}
			kycCertificate.setGoogleFormTips(null);
			nextStatus(kycCertificate, baseInfoRequest);
			kycCertificate.setFlowDefine("master");
			kycCertificateMapper.updateKycType(kycCertificate);
		}

		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		if (kycFillInfo == null) {
			kycFillInfo = KycCertificateConvertor.convert2FillInfo(null, baseInfoRequest);

		} else {
			// 已有记录 则将fill_info 迁入history，并且删掉原纪录重新插入新纪录。
			KycFillInfoHistory kycFillInfoHistory = new KycFillInfoHistory();
			BeanUtils.copyProperties(kycFillInfo, kycFillInfoHistory);
			kycFillInfoHistoryMapper.insert(kycFillInfoHistory);
			kycFillInfoMapper.deleteByUk(kycFillInfo.getUserId(), kycFillInfo.getFillType());

			kycFillInfo = KycCertificateConvertor.convert2FillInfo(kycFillInfo, baseInfoRequest);
			kycFillInfo.setIdmTid(kycFillInfoHistory.getIdmTid());
		}

		if (userSecurity != null && UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO
				.equals(userSecurity.getWithdrawSecurityFaceStatus())) {
			// 如果当前已经开启了提币人脸信息，进行设置最后一笔提币人脸信息的提币标识：transFaceLogId
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId,
					FaceTransType.WITHDRAW_FACE.name(), null);
			if (faceLog != null) {
				kycFillInfo.setRefType(KycFillInfoRefType.WITHDRAW_FACE.name());
				kycFillInfo.setRefId(faceLog.getTransId());
			}
		}
		kycFillInfoMapper.insert(kycFillInfo);
//		KycFlowContext.getContext().setNeedWordCheck(true);
		KycFlowContext.getContext().setKycCertificate(kycCertificate);
		KycFlowContext.getContext().setKycFillInfo(kycFillInfo);

	}

	private void nextStatus(KycCertificate kycCertificate, BaseInfoRequest request) {
		if(KycCertificateKycType.COMPANY.equals(request.getKycType())) {
			if (!KycCertificateStatus.PASS.name().equalsIgnoreCase(kycCertificate.getJumioStatus())) {
				kycCertificate.setJumioStatus(KycCertificateStatus.PROCESS.name());
			}
			kycCertificate.setFaceOcrStatus(null);
			return;
		}

		boolean isSdk = Objects.equals(TerminalEnum.ANDROID, request.getSource())
				|| Objects.equals(TerminalEnum.IOS, request.getSource());
		//中国用户，新接口，sdk端->ocr
		if ("CN".equals(request.getCountry()) && !request.isOldApi() && isSdk) {
			kycCertificate.setFaceOcrStatus(KycCertificateStatus.PROCESS.name());
			kycCertificate.setJumioStatus(null);
		} else {
			if (!KycCertificateStatus.PASS.name().equalsIgnoreCase(kycCertificate.getJumioStatus())) {
				kycCertificate.setJumioStatus(KycCertificateStatus.PROCESS.name());
			}
			kycCertificate.setFaceOcrStatus(null);
		}

	}
	
	private void certificateToSendKycChangeMsg(KycCertificate kycCertificate) {
    	try {
    		if(!KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
    			return;
    		}
    		KycCertificateVo certificate = new KycCertificateVo();
    		BeanUtils.copyProperties(kycCertificate, certificate);
    		certificate.setKycType(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType()) ? com.binance.certification.common.enums.KycCertificateKycType.COMPANY:
    			com.binance.certification.common.enums.KycCertificateKycType.USER);
    		certificateApi.sendBasicChangeMq(APIRequest.instance(certificate));
    	}catch(Exception e) {
    		log.warn("调用certificate发送kyc mq异常 userId:{}",kycCertificate.getUserId(),e);
    	}
    }

}
