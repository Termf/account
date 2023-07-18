package com.binance.account.service.kyc;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateNextStep;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.KycSubStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.certificate.impl.UserKycBusiness;
import com.binance.account.service.kyc.convert.KycApiTransferAdapterConvertor;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.impl.FaceBusiness;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.certificate.KycDetailResponse;
import com.binance.account.vo.certificate.request.KycForceToExpiredRequest;
import com.binance.account.vo.certificate.request.SaveCompanyCertificateRequest;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.face.request.FaceInitRequest;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.response.GetKycStatusResponse;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.account.vo.security.request.UserIdAndIdRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.UserKycVo;
import com.binance.account.vo.user.request.KycBaseInfoRequest;
import com.binance.account.vo.user.request.SaveJumioSdkScanRefRequest;
import com.binance.account.vo.user.response.InitSdkUserKycResponse;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.certification.api.KycCertificateApi;
import com.binance.certification.api.UserFaceApi;
import com.binance.certification.common.model.CurrentKycStatusSupportVo;
import com.binance.certification.request.FacePcPrivateResultRequest;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.WebUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;

@Service
@Log4j2
public class KycApiTransferAdapter {

	@Autowired
	private ApolloCommonConfig config;

	@Resource
	private IUserKyc kyc;

	@Resource
	private IUserCertificate iUserCertificate;

	@Resource
	private KycCertificateService kycCertificateService;

	@Autowired
	private IUserFace iUserFace;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private KycCertificateMapper kycCertificateMapper;
	
	@Resource
	private KycFillInfoMapper kycFillInfoMapper;

	@Resource
	private UserCommonBusiness userCommonBusiness;

	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;

	@Resource
	private UserKycBusiness userKycBusiness;

	@Resource
	private KycCertificateApi kycCertificateApi;

	@Resource
	private CertificateCenterDispatcher certificateCenterDispatcher;

	@Resource
    private IFace iFace;
	@Resource
	private UserFaceApi userFaceApi;

	/**
	 * 个人认证 baseInfoSubmit 新老适配.更具开关来.
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public APIResponse<JumioTokenResponse> submitUserBaseInfo(@RequestBody() APIRequest<KycBaseInfoRequest> request)
			throws Exception {
		long tempUser = request.getBody() == null ? 0
				: request.getBody().getUserId() == null ? 0 : request.getBody().getUserId().longValue();

		if (!config.isKycUseNewFlowSwitch() || !(tempUser % 100 < config.getKycUseNewFlowThreshold())) {
			return kyc.submitBaseInfo(request);
		}
		log.info("用户kyc执行新流程. userId:{}", tempUser);
		KycBaseInfoRequest requestBody = request.getBody();
		UserKyc.BaseInfo baseInfo = null;
		if (null != requestBody.getBaseInfo()) {
			baseInfo = new UserKyc.BaseInfo();
			BeanUtils.copyProperties(requestBody.getBaseInfo(), baseInfo);
		}
		// 参数检查
		if (!UserKyc.validateBaseInfo(baseInfo)) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		BaseInfoRequest baseInfoRequest = KycApiTransferAdapterConvertor.convert2UserBaseInfoRequest(request.getBody(),
				TerminalEnum.WEB);

		JumioInitResponse response = kycCertificateService.baseInfoSubmitWithJumio(baseInfoRequest);

		return APIResponse.getOKJsonResult(KycApiTransferAdapterConvertor.convert2JumioTokenResponse(response));
	}

	/**
	 * 企业认证 baseInfoSubmit 新老适配.更具开关来.
	 *
	 * @param request
	 * @return
	 */
	public APIResponse<JumioTokenResponse> submitCompanyBaseInfo(APIRequest<SaveCompanyCertificateRequest> request) {
//		return iUserCertificate.uploadCompanyCertificate(request);
		long tempUser = request.getBody() == null ? 0
				: request.getBody().getUserId() == null ? 0 : request.getBody().getUserId().longValue();
		if (!config.isKycUseNewFlowSwitch() || !(tempUser % 100 < config.getKycUseNewFlowThreshold())) {
			return iUserCertificate.uploadCompanyCertificate(request);
		}
		log.info("用户kyc执行新流程. userId:{}", tempUser);
		BaseInfoRequest baseInfoRequest = KycApiTransferAdapterConvertor
				.convert2CompanyBaseInfoRequest(request.getBody(), TerminalEnum.WEB);

		JumioInitResponse response = kycCertificateService.baseInfoSubmitWithJumio(baseInfoRequest);

		return APIResponse.getOKJsonResult(KycApiTransferAdapterConvertor.convert2JumioTokenResponse(response));
	}

	/**
	 * SDK端 baseInfo提交
	 *
	 * @param request
	 * @return
	 */
	public APIResponse<InitSdkUserKycResponse> initSdkUserKyc(APIRequest<KycBaseInfoRequest> request) {

		long tempUser = request.getBody() == null ? 0
				: request.getBody().getUserId() == null ? 0 : request.getBody().getUserId().longValue();
		if (!config.isKycUseNewFlowSwitch() || !(tempUser % 100 < config.getKycUseNewFlowThreshold())) {
			return kyc.initSdkUserKyc(request);
		}
		log.info("用户kyc执行新流程. userId:{}", tempUser);
		KycBaseInfoRequest requestBody = request.getBody();
		UserKyc.BaseInfo baseInfo = null;
		if (null != requestBody.getBaseInfo()) {
			baseInfo = new UserKyc.BaseInfo();
			BeanUtils.copyProperties(requestBody.getBaseInfo(), baseInfo);
		}
		// 参数检查
		if (!UserKyc.validateBaseInfo(baseInfo)) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		BaseInfoRequest baseInfoRequest = KycApiTransferAdapterConvertor.convert2UserBaseInfoRequest(request.getBody(),
				request.getTerminal());

		JumioInitResponse response = kycCertificateService.baseInfoSubmitWithJumio(baseInfoRequest);

		return APIResponse.getOKJsonResult(KycApiTransferAdapterConvertor.convert2InitSdkUserKycResponse(response));
	}

	public APIResponse<Void> saveJumioSdkScanRef(APIRequest<SaveJumioSdkScanRefRequest> request) {
		long tempUser = request.getBody() == null ? 0
				: request.getBody().getUserId() == null ? 0 : request.getBody().getUserId().longValue();
		// 新流程关闭
		if (!config.isKycUseNewFlowSwitch() || !(tempUser % 100 < config.getKycUseNewFlowThreshold())) {
			kyc.saveJumioSdkScanRef(request.getBody());
			return APIResponse.getOKJsonResult();
		}
		// 新流程开启，双写打开
		if (config.isKycFlowDoubleWrite()) {
			kyc.saveJumioSdkScanRef(request.getBody());
			return APIResponse.getOKJsonResult();
		}

		// 新流程关闭，双写关闭
		return APIResponse.getOKJsonResult();
	}

	/**
	 * facePcInit web端初始化页面跳转
	 *
	 * @param request
	 * @return
	 */
	public APIResponse<FaceInitResponse> facePcInit(@Validated @RequestBody APIRequest<FaceInitRequest> request) {
		String transId = request.getBody().getTransId();
        String type = request.getBody().getType();
        FaceTransType transType = FaceTransType.getByCode(type);
        FaceInitResponse response;
		TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(transId, transType.name());
		if(faceLog ==  null) {
			log.info("face pc init get face log by transId:{} type:{}", transId, type);
			response = iUserFace.facePcInit(request.getBody());
			return APIResponse.getOKJsonResult(response);
		}
        switch (transType) {
		case KYC_USER:
		case KYC_COMPANY:

			if(faceLog ==  null) {
				response = iUserFace.facePcInit(request.getBody());
			}else {
				CertificateCenterDispatcherParam<FaceInitResponse> param =
						certificateCenterDispatcher.faceInit(faceLog.getUserId(), transId, type,false);
				if(param.isDispatcher()) {
					response = param.getResponse();
				}else {
					response = iUserFace.facePcInit(request.getBody());
				}
			}
			break;
		case WITHDRAW_FACE:
			if (certificateCenterDispatcher.gray("WITHDRAW_FACE", faceLog.getUserId())) {
				log.info("WITHDRAW_FACE goto certification-center userId:{}", faceLog.getUserId());
				com.binance.certification.request.face.FaceInitRequest faceInitRequest = new com.binance.certification.request.face.FaceInitRequest();
				faceInitRequest.setTransId(transId);
				faceInitRequest.setType(type);
				faceInitRequest.setUserId(faceLog.getUserId());
				APIResponse<com.binance.certification.response.face.FaceInitResponse> apiResponse = userFaceApi.faceWebInit(APIRequest.instance(WebUtils.getAPIRequestHeader(), faceInitRequest));
				com.binance.certification.response.face.FaceInitResponse faceInitResponse = checkFaceResponse(apiResponse);
				response = new FaceInitResponse();
				BeanUtils.copyProperties(faceInitResponse, response);
			}else {
				log.info("WITHDRAW_FACE goto certification-center userId:{}", faceLog.getUserId());
				response = iUserFace.facePcInit(request.getBody());
			}
			break;
		case RESET_APPLY_UNLOCK:
		case RESET_APPLY_2FA:
			com.binance.certification.request.face.FaceInitRequest faceInitRequest = new com.binance.certification.request.face.FaceInitRequest();
			faceInitRequest.setTransId(transId);
			faceInitRequest.setType(type);
			faceInitRequest.setUserId(faceLog.getUserId());
			APIResponse<com.binance.certification.response.face.FaceInitResponse> apiResponse = userFaceApi.faceWebInit(APIRequest.instance(WebUtils.getAPIRequestHeader(), faceInitRequest));
			com.binance.certification.response.face.FaceInitResponse faceInitResponse = checkFaceResponse(apiResponse);
			response = new FaceInitResponse();
			BeanUtils.copyProperties(faceInitResponse, response);
			break;
		default:
			response = iUserFace.facePcInit(request.getBody());
			break;
		}
		return APIResponse.getOKJsonResult(response);
	}

	/**
	 * facePcInit sdk端初始化页面跳转
	 *
	 * @param request
	 * @return
	 */
	public APIResponse<FaceInitResponse> faceSdkInit(@Validated @RequestBody APIRequest<FaceInitRequest> request) {
		String transId = request.getBody().getTransId();
        String type = request.getBody().getType();
        FaceTransType transType = FaceTransType.getByCode(type);
        FaceInitResponse response;
		TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(transId, transType.name());
        if (faceLog == null) {
        	log.info("face sdk init get face log by transId {} type:{} fail.", transId, type);
			response = iUserFace.faceSdkInit(request.getBody());
			return APIResponse.getOKJsonResult(response);
		}
        switch (transType) {
		case KYC_USER:
		case KYC_COMPANY:
			CertificateCenterDispatcherParam<FaceInitResponse> param =
					certificateCenterDispatcher.faceInit(faceLog.getUserId(), transId, type,true);
			if(param.isDispatcher()) {
				response = param.getResponse();
			}else {
				response = iUserFace.faceSdkInit(request.getBody());
			}
			break;
		case WITHDRAW_FACE:
			if (certificateCenterDispatcher.gray("WITHDRAW_FACE", faceLog.getUserId())) {
				log.info("WITHDRAW_FACE goto certification-center userId:{}", faceLog.getUserId());
				com.binance.certification.request.face.FaceInitRequest faceInitRequest = new com.binance.certification.request.face.FaceInitRequest();
				faceInitRequest.setTransId(transId);
				faceInitRequest.setType(type);
				faceInitRequest.setUserId(faceLog.getUserId());
				APIResponse<com.binance.certification.response.face.FaceInitResponse> apiResponse = userFaceApi.faceSdkInit(APIRequest.instance(WebUtils.getAPIRequestHeader(), faceInitRequest));
				com.binance.certification.response.face.FaceInitResponse faceInitResponse = checkFaceResponse(apiResponse);
				response = new FaceInitResponse();
				BeanUtils.copyProperties(faceInitResponse, response);
			}else {
				log.info("WITHDRAW_FACE goto certification-center userId:{}", faceLog.getUserId());
				response = iUserFace.faceSdkInit(request.getBody());
			}
			break;
		case RESET_APPLY_2FA:
		case RESET_APPLY_UNLOCK:
			com.binance.certification.request.face.FaceInitRequest faceInitRequest = new com.binance.certification.request.face.FaceInitRequest();
			faceInitRequest.setTransId(transId);
			faceInitRequest.setType(type);
			faceInitRequest.setUserId(faceLog.getUserId());
			APIResponse<com.binance.certification.response.face.FaceInitResponse> apiResponse = userFaceApi.faceSdkInit(APIRequest.instance(WebUtils.getAPIRequestHeader(), faceInitRequest));
			com.binance.certification.response.face.FaceInitResponse faceInitResponse = checkFaceResponse(apiResponse);
			response = new FaceInitResponse();
			BeanUtils.copyProperties(faceInitResponse, response);
			break;
		default:
			response = iUserFace.faceSdkInit(request.getBody());
			break;
		}
		return APIResponse.getOKJsonResult(response);
	}

	private <T> T checkFaceResponse(APIResponse<T> apiResponse) {
		if (apiResponse == null || apiResponse.getStatus() != APIResponse.Status.OK) {
			if (apiResponse == null) {
				throw new BusinessException(GeneralCode.COMMON_ERROR);
			}else {
				throw new BusinessException(apiResponse.getCode(), apiResponse.getErrorData().toString(), apiResponse.getParams());
			}
		}
		return apiResponse.getData();
	}

	public APIResponse<Void> forceKycPassedToExpired(APIRequest<KycForceToExpiredRequest> request) {
		Long userId = request.getBody().getUserId();

		if (request == null || request.getBody() == null || request.getBody().getUserId() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate == null) {
			this.kyc.forceKycPassedToExpired(request.getBody());
			return APIResponse.getOKJsonResult();
		}

		kycCertificateService.forceKycPassedToExpired(request.getBody(), kycCertificate);

		// 双写关闭
		if (!config.isKycFlowDoubleWrite()) {
			return APIResponse.getOKJsonResult();
		}

		UserKyc userKyc = userKycMapper.getLast(userId);
		if (userKyc == null) {
			log.warn("获取KYC记录失败 userId: {}" + userId);
			return APIResponse.getOKJsonResult();
		}
		userKyc.setFailReason(request.getBody().getFailReason());
		iUserCertificate.userKycExpired(userKyc);

		return APIResponse.getOKJsonResult();
	}

	public APIResponse<KycDetailResponse> getCurrentKycStatus(
			@Validated @RequestBody() APIRequest<UserIdRequest> request) {

		Long userId = request.getBody().getUserId();

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate == null) {
			KycDetailResponse data = kyc.getCurrentKycStatus(userId);
			log.info("currentStatus返回结果 userId:{},response:{}", userId, data);
			return APIResponse.getOKJsonResult(kyc.getCurrentKycStatus(userId));
		}
		// 中国流程
		if ("cn".equals(kycCertificate.getFlowDefine()) || "cnv2".equals(kycCertificate.getFlowDefine())) {
			return getCnCurrentKycStatus(kycCertificate);
		}
		
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		if(baseInfo != null && StringUtils.equalsIgnoreCase("AU", baseInfo.getCountry())) {
			return getCnCurrentKycStatus(kycCertificate);
		}

		// 新流程适配
		GetKycStatusResponse getKycStatusResult = kycCertificateService.getKycStatus(userId);

		log.info("getCurrentStatus 处理器执行结果 userId:{},result:{}", userId, getKycStatusResult);

		KycDetailResponse kycDetailResponse = new KycDetailResponse();
		kycDetailResponse.setNeedAddress(getKycStatusResult.isNeedAddress());

		if (getKycStatusResult.getBase() == null) {
			kycDetailResponse.setKycStatus(-1);
//			if (config.isFaceOcrSwitch()) {
//				kycDetailResponse.setKycSubStatus(KycSubStatus.BASIC);
//			} else {
//				kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
//			}
			kycDetailResponse.setKycSubStatus(KycSubStatus.BASIC);
			log.info("currentStatus返回结果 userId:{},response:{}", userId, kycDetailResponse);
			return APIResponse.getOKJsonResult(kycDetailResponse);
		}
		LanguageEnum language = LanguageEnum.findByLang(WebUtils.getHeader(Constant.LANG));
		kycDetailResponse.setFlowDefine("master");
		kycDetailResponse.setBaseFillStatus(kycCertificate.getBaseFillStatus());
		kycDetailResponse.setBaseFillTips(StringUtils.isBlank(kycCertificate.getBaseFillTips()) ? null : MessageMapHelper.getMessage(kycCertificate.getBaseFillTips(), language));
		kycDetailResponse.setGoogleFormStatus(kycCertificate.getGoogleFormStatus());
		kycDetailResponse.setGoogleFormTips(StringUtils.isBlank(kycCertificate.getGoogleFormTips()) ? null : MessageMapHelper.getMessage(kycCertificate.getGoogleFormTips(), language));
		kycDetailResponse.setFaceOcrStatus(kycCertificate.getFaceOcrStatus());
		kycDetailResponse.setFaceOcrTips(StringUtils.isBlank(kycCertificate.getFaceOcrTips()) ? null : MessageMapHelper.getMessage(kycCertificate.getFaceOcrTips(), language));
		kycDetailResponse.setJumioStatus(kycCertificate.getJumioStatus());
		kycDetailResponse.setJumioTips(StringUtils.isBlank(kycCertificate.getJumioTips()) ? null : MessageMapHelper.getMessage(kycCertificate.getJumioTips(), language));
		kycDetailResponse.setFaceStatus(kycCertificate.getFaceStatus());
		kycDetailResponse.setFaceTips(StringUtils.isBlank(kycCertificate.getFaceTips()) ? null : MessageMapHelper.getMessage(kycCertificate.getFaceTips(), language));
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			kycDetailResponse.setKycStatus(KycCertificateResult.STATUS_PASS);
		} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getStatus())) {
			kycDetailResponse.setKycStatus(KycCertificateResult.STATUS_REFUSED);
		} else if (KycCertificateStatus.FORBID_PASS.name().equals(kycCertificate.getStatus())) {
			kycDetailResponse.setKycStatus(KycCertificateResult.STATUS_REFUSED);
			kycDetailResponse.setForbidCountryPassed(true);
		} else {
			kycDetailResponse.setKycStatus(KycCertificateResult.STATUS_REVIEW);
		}

		kycDetailResponse.setAddressStatus(getKycStatusResult.getAddressStatus() == null ? null
				: KycCertificateStatus.valueOf(getKycStatusResult.getAddressStatus()));
		kycDetailResponse.setAddressTips(getKycStatusResult.getAddressTips());

		String message = "";

		if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getJumioStatus())) {
			message = kycCertificate.getJumioTips();
		} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceOcrStatus())) {
			message = kycCertificate.getFaceOcrTips();
		} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceStatus())) {
			message = kycCertificate.getFaceTips();
		}

		if (language == LanguageEnum.ZH_CN) {
			message = userCommonBusiness.getJumioFailReason(message, true);
		} else {
			message = userCommonBusiness.getJumioFailReason(message, false);
		}

		kycDetailResponse.setKycMessage(message);

		KycFillInfoVo base = getKycStatusResult.getBase();

		KycDetailResponse.FillInfo fillInfo = new KycDetailResponse.FillInfo();
		fillInfo.setCompanyName(StringUtils.isBlank(base.getCompanyName()) ? "" : base.getCompanyName());
		fillInfo.setFirstName(StringUtils.isBlank(base.getFirstName()) ? "" : base.getFirstName());
		fillInfo.setMiddleName(StringUtils.isBlank(base.getMiddleName()) ? "" : base.getMiddleName());
		fillInfo.setLastName(StringUtils.isBlank(base.getLastName()) ? "" : base.getLastName());
		fillInfo.setCountry(StringUtils.isBlank(base.getCountry()) ? "" : base.getCountry());
		fillInfo.setCity(StringUtils.isBlank(base.getCity()) ? "" : base.getCity());
		fillInfo.setAddress(StringUtils.isBlank(base.getAddress()) ? "" : base.getAddress());
		fillInfo.setDob(StringUtils.isBlank(base.getBirthday()) ? "" : base.getBirthday());
		fillInfo.setPostalCode(StringUtils.isBlank(base.getPostalCode()) ? "" : base.getPostalCode());
		kycDetailResponse.setFillInfo(fillInfo);

		if (KycCertificateKycType.USER.equals(getKycStatusResult.getKycType())) {
			// 个人认证，type 使用人脸识别类型中的code, 平衡做人脸识别时需要的类型
			kycDetailResponse.setType(FaceTransType.KYC_USER.getCode());
		} else {
			// 企业认证
			kycDetailResponse.setType(FaceTransType.KYC_COMPANY.getCode());
		}
		FaceTransType faceTransType = getKycStatusResult.getKycType() == KycCertificateKycType.USER
				? FaceTransType.KYC_USER
				: FaceTransType.KYC_COMPANY;
		if (StringUtils.isNotBlank(getKycStatusResult.getFaceStatus())
				&& !StringUtils.equalsIgnoreCase(KycCertificateStatus.SKIP.name(), kycCertificate.getFaceStatus())) {
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
			if (faceLog != null) {
				kycDetailResponse.setTransId(faceLog.getTransId());
			}
		}
		KycCertificateNextStep nextStep = getKycStatusResult.getNextStep();

		if (nextStep == null) {
			kycDetailResponse.setKycSubStatus(KycSubStatus.AUDITING);
			log.info("currentStatus返回结果 userId:{},response:{}", userId, kycDetailResponse);
			return APIResponse.getOKJsonResult(kycDetailResponse);
		}

		if (KycCertificateStatus.PASS.name().equals(getKycStatusResult.getStatus())) {
			log.info("currentStatus返回结果 userId:{},response:{}", userId, kycDetailResponse);
			return APIResponse.getOKJsonResult(kycDetailResponse);
		}

		switch (nextStep) {
		case BASE:
			kycDetailResponse.setKycSubStatus(KycSubStatus.BASIC);
			break;
		case FACE_OCR:
			kycDetailResponse.setKycSubStatus(KycSubStatus.FACE_OCR);
			break;
		case JUMIO:
			kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
			break;
		case FACE:
			kycDetailResponse.setKycSubStatus(KycSubStatus.FACE_PENDING);
			kycDetailResponse.setQrCode(getKycStatusResult.getQrCode());
			kycDetailResponse.setFaceTransId(getKycStatusResult.getFaceTransId());
			break;
		default:
			break;
		}
		log.info("currentStatus返回结果 userId:{},response:{}", userId, kycDetailResponse);
		return APIResponse.getOKJsonResult(kycDetailResponse);
	}

	private APIResponse<KycDetailResponse> getCnCurrentKycStatus(KycCertificate kycCertificate) {
		Long userId = kycCertificate.getUserId();
		APIResponse<com.binance.certification.response.GetKycStatusResponse> apiResp = kycCertificateApi
				.getKycStatus(APIRequest.instance(userId));
		if (apiResp == null || apiResp.getData() == null) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

		com.binance.certification.response.GetKycStatusResponse data = apiResp.getData();
		CurrentKycStatusSupportVo support = data.getSupport();
		log.info("getCurrentStatus cn处理器执行结果 userId:{},result:{}", userId, data);
		KycDetailResponse kycDetailResponse = new KycDetailResponse();

		if (data.getBase() == null) {
			kycDetailResponse.setKycStatus(-1);
			kycDetailResponse.setKycSubStatus(KycSubStatus.FACE_OCR);
			log.info("currentStatus cn返回结果 userId:{},response:{}", userId, kycDetailResponse);
			return APIResponse.getOKJsonResult(kycDetailResponse);
		}
		kycDetailResponse.setFlowDefine(data.getFlowDefine());
		kycDetailResponse.setNeedAddress(data.isNeedAddress());

		kycDetailResponse.setKycSubStatus(KycSubStatus.valueOf(support.getKycSubStatus().name()));
		kycDetailResponse.setKycStatus(support.getKycStatus());
		kycDetailResponse.setForbidCountryPassed(support.getForbidCountryPassed());
		kycDetailResponse.setAddressStatus(support.getAddressStatus() == null ? null
				: KycCertificateStatus.valueOf(support.getAddressStatus().name()));
		kycDetailResponse.setAddressTips(support.getAddressTips());
		kycDetailResponse.setNeedAddress(data.isNeedAddress());
		kycDetailResponse.setKycMessage(support.getKycMessage());
		kycDetailResponse.setBaseFillStatus(data.getBaseFillStatus());
		kycDetailResponse.setBaseSubStatus(data.getBaseSubStatus());
		kycDetailResponse.setBaseFillTips(data.getBaseFillTips());
		kycDetailResponse.setGoogleFormStatus(data.getGoogleFormStatus());
		kycDetailResponse.setGoogleFormTips(data.getGoogleFormTips());
		kycDetailResponse.setFaceOcrStatus(data.getFaceOcrStatus());
		kycDetailResponse.setFaceOcrTips(data.getFaceOcrTips());
		kycDetailResponse.setJumioStatus(data.getJumioStatus());
		kycDetailResponse.setJumioTips(data.getJumioTips());
		kycDetailResponse.setFaceStatus(data.getFaceStatus());
		kycDetailResponse.setFaceTips(data.getFaceTips());
		com.binance.certification.common.model.KycFillInfoVo base = data.getBase();

		KycDetailResponse.FillInfo fillInfo = new KycDetailResponse.FillInfo();
		fillInfo.setCompanyName(StringUtils.isBlank(base.getCompanyName()) ? "" : base.getCompanyName());
		fillInfo.setFirstName(StringUtils.isBlank(base.getFirstName()) ? "" : base.getFirstName());
		fillInfo.setMiddleName(StringUtils.isBlank(base.getMiddleName()) ? "" : base.getMiddleName());
		fillInfo.setLastName(StringUtils.isBlank(base.getLastName()) ? "" : base.getLastName());
		fillInfo.setCountry(StringUtils.isBlank(base.getCountry()) ? "" : base.getCountry());
		fillInfo.setCity(StringUtils.isBlank(base.getCity()) ? "" : base.getCity());
		fillInfo.setAddress(StringUtils.isBlank(base.getAddress()) ? "" : base.getAddress());
		fillInfo.setDob(StringUtils.isBlank(base.getBirthday()) ? "" : base.getBirthday());
		fillInfo.setPostalCode(StringUtils.isBlank(base.getPostalCode()) ? "" : base.getPostalCode());
		fillInfo.setIdcardNumber(data.getIdNumber());
		kycDetailResponse.setFillInfo(fillInfo);
		kycDetailResponse.setType(support.getType());
		kycDetailResponse.setFaceTransId(data.getFaceTransId());
		kycDetailResponse.setQrCode(data.getQrCode());
		kycDetailResponse.setKycSubStatus(KycSubStatus.valueOf(support.getKycSubStatus().name()));
		kycDetailResponse.setKycLevel(data.getKycLevel());
		log.info("currentStatus返回结果 userId:{},response:{}", userId, kycDetailResponse);
		return APIResponse.getOKJsonResult(kycDetailResponse);

	}

	public APIResponse<UserKycVo> getKycByUserId(@RequestBody() @Validated APIRequest<UserIdRequest> request) {

		Long userId = request.getBody().getUserId();

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate == null) {
			return kyc.getKycByUserId(request);
		}
		// 新逻辑适配
		KycCertificateVo kycCertificateVo = kycCertificateService.getKycCertificateDetail(userId, true);

		TransactionFaceLog faceLog = null;
		if (StringUtils.isNotBlank(kycCertificateVo.getFaceStatus())) {
			faceLog = transactionFaceLogMapper.findLastByUserId(userId, FaceTransType.KYC_USER.name(), null);
		}
		UserKycVo userKycVo = KycApiTransferAdapterConvertor.convert2UserKycVo(kycCertificateVo, faceLog);

		userKycBusiness.generateKycImageUrls(userKycVo.getCheckInfo());
		return APIResponse.getOKJsonResult(userKycVo);
	}

	public UserKycCountryResponse getKycCountry(Long userId) throws Exception {
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate == null) {
			return kyc.getKycCountry(userId);
		} else {
			return kycCertificateService.getKycCountry(userId);
		}
	}

	public APIResponse<UserKycVo> getUserKycById(APIRequest<UserIdAndIdRequest> request) {
		UserIdAndIdRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate == null) {
			return kyc.getUserKycById(request);
		}

		KycCertificateVo kycCertificateVo = kycCertificateService.getKycCertificateDetail(userId, true);
		UserKycVo userKycVo = KycApiTransferAdapterConvertor.convert2UserKycVo(kycCertificateVo, null);

		return APIResponse.getOKJsonResult(userKycVo);
	}

	public APIResponse<Void> pcFaceVerifyPrivate(APIRequest<FacePcPrivateResult> request) {
		FacePcPrivateResult resultRequest = request.getBody();
		FaceTransType transType = FaceTransType.getByCode(resultRequest.getFaceTransType());
		TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(resultRequest.getTransId(), transType.name());
		if(faceLog == null) {
			log.info("pc face verify private get face log fail by transId:{} type:{}", resultRequest.getTransId(), resultRequest.getFaceTransType());
			iUserFace.facePcVerifyPrivate(resultRequest);
			return APIResponse.getOKJsonResult();
		}
		switch (transType) {
			case KYC_USER:
			case KYC_COMPANY:
				CertificateCenterDispatcherParam<Void> apiResp = certificateCenterDispatcher.pcFaceVerifyPrivate(faceLog.getUserId(), resultRequest);
				if(!apiResp.isDispatcher()) {
					iUserFace.facePcVerifyPrivate(resultRequest);
				}
				break;
			case WITHDRAW_FACE:
				if (certificateCenterDispatcher.gray("WITHDRAW_FACE", faceLog.getUserId())) {
					log.info("WITHDRAW_FACE face web verify result goto certification-center. userId:{}", faceLog.getUserId());
					FacePcPrivateResultRequest privateResultRequest = new FacePcPrivateResultRequest();
					BeanUtils.copyProperties(resultRequest, privateResultRequest);
					APIResponse<Void> apiResponse = userFaceApi.webFaceVerifyPrivate(APIRequest.instance(WebUtils.getAPIRequestHeader(), privateResultRequest));
					checkFaceResponse(apiResponse);
				}else {
					log.info("WITHDRAW_FACE face web verify result goto iUserFace. userId:{}", faceLog.getUserId());
					iUserFace.facePcVerifyPrivate(resultRequest);
				}
				break;
			case RESET_APPLY_UNLOCK:
			case RESET_APPLY_2FA:
				FacePcPrivateResultRequest privateResultRequest = new FacePcPrivateResultRequest();
				BeanUtils.copyProperties(resultRequest, privateResultRequest);
				APIResponse<Void> apiResponse = userFaceApi.webFaceVerifyPrivate(APIRequest.instance(WebUtils.getAPIRequestHeader(), privateResultRequest));
				checkFaceResponse(apiResponse);
				break;
			default:
				iUserFace.facePcVerifyPrivate(resultRequest);
				break;
		}
		return APIResponse.getOKJsonResult();
	}

	public APIResponse<FaceSdkResponse> appFaceSdkVerify(@Validated @RequestBody APIRequest<FaceSdkVerifyRequest> request) {
		FaceSdkVerifyRequest body = request.getBody();
		String qrCode = body.getTransId();
        FaceBusiness.FaceSdkCache faceSdkCache = iFace.getFaceSdkCacheByQrCode(qrCode);
        FaceTransType faceTransType = faceSdkCache == null ? null : faceSdkCache.getTransType();
        Long userId = faceSdkCache == null ? null : faceSdkCache.getUserId();

        if(userId == null) {
        	return APIResponse.getOKJsonResult(iUserFace.appFaceSdkVerify(request.getBody()));
        }

        if(faceTransType == null) {
        	return APIResponse.getOKJsonResult(iUserFace.appFaceSdkVerify(request.getBody()));
        }
        switch (faceTransType) {
        case KYC_USER:
		case KYC_COMPANY:
			CertificateCenterDispatcherParam<FaceSdkResponse> param = certificateCenterDispatcher.appFaceSdkVerify(body, userId);
			if(param.isDispatcher()) {
				return APIResponse.getOKJsonResult(param.getResponse());
			}
			return APIResponse.getOKJsonResult(iUserFace.appFaceSdkVerify(request.getBody()));
		case WITHDRAW_FACE:
			if (certificateCenterDispatcher.gray("WITHDRAW_FACE", userId)) {
				log.info("WITHDRAW_FACE face sdk verify result goto certification-center. userId:{}", userId);
				com.binance.certification.request.FaceSdkVerifyRequest faceSdkVerifyRequest = new com.binance.certification.request.FaceSdkVerifyRequest();
				BeanUtils.copyProperties(body, faceSdkVerifyRequest);
				faceSdkVerifyRequest.setUserId(userId);
				APIResponse<com.binance.certification.response.face.FaceSdkResponse> apiResponse = userFaceApi.appFaceSdkVerify(APIRequest.instance(WebUtils.getAPIRequestHeader(), faceSdkVerifyRequest));
				com.binance.certification.response.face.FaceSdkResponse sdkResponse = checkFaceResponse(apiResponse);
				FaceSdkResponse response = new FaceSdkResponse();
				BeanUtils.copyProperties(sdkResponse, response);
				if(sdkResponse.getStatus()!=null) {
					response.setStatus(TransFaceLogStatus.valueOf(sdkResponse.getStatus().name()));
				}
				return APIResponse.getOKJsonResult(response);
			}else {
				log.info("WITHDRAW_FACE face sdk verify result goto iUserFace. userId:{}", userId);
				return APIResponse.getOKJsonResult(iUserFace.appFaceSdkVerify(request.getBody()));
			}
		case RESET_APPLY_2FA:
		case RESET_APPLY_UNLOCK:
			com.binance.certification.request.FaceSdkVerifyRequest faceSdkVerifyRequest = new com.binance.certification.request.FaceSdkVerifyRequest();
			BeanUtils.copyProperties(body, faceSdkVerifyRequest);
			faceSdkVerifyRequest.setUserId(userId);
			APIResponse<com.binance.certification.response.face.FaceSdkResponse> apiResponse = userFaceApi.appFaceSdkVerify(APIRequest.instance(WebUtils.getAPIRequestHeader(), faceSdkVerifyRequest));
			com.binance.certification.response.face.FaceSdkResponse sdkResponse = checkFaceResponse(apiResponse);
			FaceSdkResponse response = new FaceSdkResponse();
			BeanUtils.copyProperties(sdkResponse, response);
			if(sdkResponse.getStatus()!=null) {
				response.setStatus(TransFaceLogStatus.valueOf(sdkResponse.getStatus().name()));
			}
			return APIResponse.getOKJsonResult(response);
		default:
			return APIResponse.getOKJsonResult(iUserFace.appFaceSdkVerify(request.getBody()));
		}
	}

}
