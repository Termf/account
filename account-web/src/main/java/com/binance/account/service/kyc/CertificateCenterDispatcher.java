package com.binance.account.service.kyc;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.service.security.IWithdrawSecurityFace;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.kyc.request.AddresAuthResultRequest;
import com.binance.account.vo.kyc.request.AddressInfoSubmitRequest;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.request.FaceOcrAuthRequest;
import com.binance.account.vo.kyc.request.FaceOcrSubmitRequest;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.AddressInfoSubmitResponse;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.FaceOcrSubmitResponse;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.account.vo.security.request.SecurityFaceStatusRequest;
import com.binance.account.vo.withdraw.request.WithdrawFaceInHoursRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawFaceTipResponse;
import com.binance.certification.api.KycCertificateApi;
import com.binance.certification.api.KycCertificateAuditApi;
import com.binance.certification.api.UserFaceApi;
import com.binance.certification.api.WithdrawFaceApi;
import com.binance.certification.common.enums.IdentityType;
import com.binance.certification.common.enums.KycCertificateKycType;
import com.binance.certification.common.enums.KycCertificateStatus;
import com.binance.certification.common.enums.KycFillInfoGender;
import com.binance.certification.common.enums.WithdrawFaceCheckSource;
import com.binance.certification.request.FacePcPrivateResultRequest;
import com.binance.certification.request.JumioAuditRequest;
import com.binance.certification.request.WithdrawFaceOnOffRequest;
import com.binance.certification.request.face.FaceInitRequest;
import com.binance.certification.response.WithdrawFaceCheckResponse;
import com.binance.certification.response.WithdrawFaceOnOffResponse;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
public class CertificateCenterDispatcher {

	@Resource
	private KycCertificateApi kycCertificateApi;
	@Resource
	private KycCertificateAuditApi kycCertificateAuditApi;
	@Resource
	private KycCertificateMapper kycCertificateMapper;
	@Resource
	private KycFillInfoMapper kycFillInfoMapper;

	private static final String CN_FLOW_DEFINE = "cn";
	private static final String CN_FLOW_V2_DEFINE = "cnv2";

	@Resource
	private ApolloCommonConfig config;
	@Resource
	private UserFaceApi userFaceApi;

	@Resource
	private IWithdrawSecurityFace iWithdrawSecurityFace;
	@Resource
	private WithdrawFaceApi withdrawFaceApi;

	public boolean gray(String type,Long userId) {
		String users = config.getCertificateCenterGrayUsers();

		if(userId == null) {
			return false;
		}

		if(StringUtils.isNotBlank(users) && users.contains(userId.toString())){
			log.info("提交转发 用户白名单转发 userId:{}", userId);
			return true;
		}

		Map<String, Integer> grayMap = config.getCertificateCenterGray();
		if(grayMap == null || grayMap.isEmpty()) {
			return false;
		}
		Integer grayValue = grayMap.get(type);
		if(grayValue == null) {
			return false;
		}

		boolean gray = (userId.longValue() % 1000 < grayValue.intValue());
		log.info("提交转发 用户灰度转发 userId:{} gray:{}", userId,gray);
		return gray;
	}

	public CertificateCenterDispatcherParam<BaseInfoResponse> baseInfoSubmit(BaseInfoRequest request) {
		Long userId = request.getUserId();

		CertificateCenterDispatcherParam<BaseInfoResponse> param = new CertificateCenterDispatcherParam<>();

		log.info("base提交转发逻辑判断 userId:{}", userId);

		if(CN_FLOW_DEFINE.equals(request.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(request.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(request.getCountry(), "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		}else{
			param.setDispatcher(gray("BASIC", userId));
		}

		if (!param.isDispatcher()) {
			log.info("base提交account处理 userId:{}", userId);
			return param;
		}

		com.binance.certification.request.BaseInfoRequest apiReq = new com.binance.certification.request.BaseInfoRequest();
		BeanUtils.copyProperties(request, apiReq);
		apiReq.setKycType(KycCertificateKycType.getByCode(request.getKycType().getCode()));
		// IdType转换，非澳洲可能为空
		if (StringUtils.isNotBlank(request.getIdType())) {
			apiReq.setIdType(IdentityType.valueOf(request.getIdType()));
		}
		if(request.getGender() != null) {
			KycFillInfoGender gender =KycFillInfoGender.getGender(request.getGender().getGender());
			apiReq.setGender(gender);
		}
		try {
			log.info("base提交转发certificate_center userId:{}", userId);
			APIResponse<com.binance.certification.response.BaseInfoResponse> apiResp = kycCertificateApi
					.baseInfoSubmit(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			if (apiResp.getData() == null) {
				log.warn("base提交转发异常.应答结果为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			log.info("base提交转发certificate_center返回 userId:{} response:{}", userId,apiResp.getData());
			com.binance.certification.response.BaseInfoResponse data = apiResp.getData();
			BaseInfoResponse resp = new BaseInfoResponse();
			BeanUtils.copyProperties(data, resp);
			if(data.getBaseFillStatus() != null) {
				try {
					resp.setBaseFillStatus(com.binance.account.common.enums.KycCertificateStatus.valueOf(data.getBaseFillStatus().name()));
				}catch(Exception e) {
					
				}
			}
			param.setResponse(resp);
			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("base提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	public CertificateCenterDispatcherParam<FaceOcrSubmitResponse> kycFaceOcrSubmit(FaceOcrSubmitRequest request) {
		Long userId = request.getUserId();

		CertificateCenterDispatcherParam<FaceOcrSubmitResponse> param = new CertificateCenterDispatcherParam<>();

		if(CN_FLOW_DEFINE.equals(request.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(request.getFlowDefine())) {
			param.setDispatcher(true);
		}else {
			param.setDispatcher(gray("FACEOCR", userId));
		}

		if (!param.isDispatcher()) {
			log.info("face ocr提交account处理 userId:{}", userId);
			return param;
		}
		log.info("face ocr提交转发逻辑判断 userId:{}", userId);

		com.binance.certification.request.FaceOcrSubmitRequest apiReq = new com.binance.certification.request.FaceOcrSubmitRequest();
		BeanUtils.copyProperties(request, apiReq);
		apiReq.setKycType(KycCertificateKycType.getByCode(request.getKycType().getCode()));
		try {
			log.info("face ocr提交转发certificate_center userId:{}", userId);
			APIResponse<com.binance.certification.response.FaceOcrSubmitResponse> apiResp = kycCertificateApi
					.kycFaceOcrSubmit(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			if (apiResp.getData() == null) {
				log.warn("face ocr提交转发异常.应答结果为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			log.info("face ocr提交转发certificate_center userId:{} response:{}", userId,apiResp.getData());
			com.binance.certification.response.FaceOcrSubmitResponse data = apiResp.getData();
			FaceOcrSubmitResponse resp = new FaceOcrSubmitResponse();
			BeanUtils.copyProperties(data, resp);
			if(data.getGender() != null) {
				resp.setGender(com.binance.account.common.enums.KycFillInfoGender.getGender(data.getGender().getGender()));
			}
			param.setResponse(resp);
			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("face ocr提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<AddressInfoSubmitResponse> addressInfoSubmit(
			AddressInfoSubmitRequest request) {
		Long userId = request.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();

		CertificateCenterDispatcherParam<AddressInfoSubmitResponse> param = new CertificateCenterDispatcherParam<>();

		if (kycCertificate == null) {
			param.setDispatcher(false);
		} else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		} else {
			param.setDispatcher(gray("ADDRESS",request.getUserId()));
		}

		if (!param.isDispatcher()) {
			log.info("address提交account处理 userId:{}", userId);
			return param;
		}
		log.info("address 提交转发逻辑判断 userId:{}", userId);
		com.binance.certification.request.AddressInfoSubmitRequest apiReq = new com.binance.certification.request.AddressInfoSubmitRequest();
		BeanUtils.copyProperties(request, apiReq);
		try {
			log.info("address提交转发certificate_center userId:{}", userId);
			APIResponse<com.binance.certification.response.AddressInfoSubmitResponse> apiResp = kycCertificateApi
					.addressInfoSubmit(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));

			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			if (apiResp.getData() == null) {
				log.warn("address提交转发异常.应答结果为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}

			log.info("address提交转发certificate_center返回 userId:{} response:{}", userId,apiResp.getData());
			com.binance.certification.response.AddressInfoSubmitResponse data = apiResp.getData();
			AddressInfoSubmitResponse resp = new AddressInfoSubmitResponse();
			BeanUtils.copyProperties(data, resp);
			param.setResponse(resp);
			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("address提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<JumioInitResponse> kycJumioInit(KycFlowRequest request) {
		Long userId = request.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();

		CertificateCenterDispatcherParam<JumioInitResponse> param = new CertificateCenterDispatcherParam<>();

		if (kycCertificate == null) {
			param.setDispatcher(false);
		} else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		}else {
			param.setDispatcher(gray("JUMIO",request.getUserId()));
		}

		if (!param.isDispatcher()) {
			log.info("jumio init提交account处理 userId:{}", userId);
			return param;
		}
		log.info("jumio init 提交转发逻辑判断 userId:{}", userId);

		com.binance.certification.request.KycFlowRequest apiReq = new com.binance.certification.request.KycFlowRequest();
		BeanUtils.copyProperties(request, apiReq);
		try {
			log.info("jumio ini 提交转发certificate_center userId:{}", userId);
			APIResponse<com.binance.certification.response.JumioInitResponse> apiResp = kycCertificateApi
					.kycJumioInit(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			if (apiResp.getData() == null) {
				log.warn("jumio init提交转发异常.应答结果为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			log.info("jumio init提交转发certificate_center返回 userId:{} response:{}", userId,apiResp.getData());
			com.binance.certification.response.JumioInitResponse data = apiResp.getData();
			JumioInitResponse resp = new JumioInitResponse();
			BeanUtils.copyProperties(data, resp);
			param.setResponse(resp);
			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("jumio ini 提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<Void> addressAuthResult(AddresAuthResultRequest request) {
		Long userId = request.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();
		
		CertificateCenterDispatcherParam<Void> param = new CertificateCenterDispatcherParam<>();

		if (kycCertificate == null) {
			param.setDispatcher(false);
		} else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		} else {
			param.setDispatcher(gray("ADDRESS",request.getUserId()));
		}

		if (!param.isDispatcher()) {
			log.info("address auth提交account处理 userId:{}", userId);
			return param;
		}

		log.info("address auth 提交转发逻辑判断 userId:{}", userId);

		com.binance.certification.request.KycAuditRequest apiReq = new com.binance.certification.request.KycAuditRequest();
		apiReq.setUserId(request.getUserId());
		apiReq.setTips(request.getAddressTips());
		apiReq.setKycCertificateStatus(KycCertificateStatus.getByName(request.getAddressStatus().name()));
		apiReq.setOperator(request.getOperator());
		try {
			log.info("address auth提交转发certificate_center userId:{}", userId);
			APIResponse<Void> apiResp = kycCertificateAuditApi.addressAuthResult(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}
			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("address auth提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<Void> jumioAuditResult(JumioInfoVo jumioInfoVo,String bizId){
		Long userId = jumioInfoVo.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();
		
		CertificateCenterDispatcherParam<Void> param = new CertificateCenterDispatcherParam<>();
		if (kycCertificate == null) {
			param.setDispatcher(false);
		} else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		} else {
			param.setDispatcher(gray("JUMIO",jumioInfoVo.getUserId()));
		}

		if(!param.isDispatcher()) {
			log.info("jumio auth 提交account处理 userId:{}", userId);
			return param;
		}

		log.info("jumio auth 提交转发逻辑判断 userId:{}", userId);

		JumioAuditRequest apiReq = new JumioAuditRequest();
		apiReq.setUserId(userId);
		apiReq.setMessage(jumioInfoVo.getFailReason());
		apiReq.setJumioStatus(jumioInfoVo.getStatus().name());
		apiReq.setBizId(bizId);
		apiReq.setJumioInfoVo(JSON.toJSONString(jumioInfoVo));

		try {
			log.info("jumio audit提交转发certificate_center userId:{}", userId);
			APIResponse<Void> apiResp = kycCertificateAuditApi.jumioAuditResult(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}
			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("jumio audit提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<Void> auditGoogleForm(KycAuditRequest request) {
		Long userId = request.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();

		CertificateCenterDispatcherParam<Void> param = new CertificateCenterDispatcherParam<>();
		if (kycCertificate == null) {
			param.setDispatcher(false);
		} else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		} else {
			param.setDispatcher(gray("GOOGLE",request.getUserId()));
		}

		if(!param.isDispatcher()) {
			log.info("google auth提交account处理 userId:{}", userId);
			return param;
		}

		log.info("google auth 提交转发逻辑判断 userId:{}", userId);

		com.binance.certification.request.KycAuditRequest apiReq = new com.binance.certification.request.KycAuditRequest();
		apiReq.setUserId(request.getUserId());
		apiReq.setKycCertificateStatus(KycCertificateStatus.valueOf(request.getKycCertificateStatus().name()));
		apiReq.setTips(request.getTips());
		try {
			log.info("google audit 提交转发certificate_center userId:{}", userId);
			APIResponse<Void> apiResp = kycCertificateAuditApi.auditGoogleForm(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}
			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("google audit 提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<Void> auditFaceOcr(FaceOcrAuthRequest request) {
		Long userId = request.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();
		
		CertificateCenterDispatcherParam<Void> param = new CertificateCenterDispatcherParam<>();
		if (kycCertificate == null) {
			param.setDispatcher(false);
		} else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		} else {
			param.setDispatcher(gray("FACEOCR",request.getUserId()));
		}

		if(!param.isDispatcher()) {
			log.info("face ocr审核account处理 userId:{}", userId);
			return param;
		}

		log.info("face ocr审核提交转发逻辑判断 userId:{}", userId);

		com.binance.certification.request.FaceOcrAuthRequest apiReq = new com.binance.certification.request.FaceOcrAuthRequest();
		BeanUtils.copyProperties(request, apiReq);

		try {
			log.info("face ocr审核提交转发certificate_center userId:{}", userId);
			APIResponse<Void> apiResp = kycCertificateAuditApi.auditFaceOcr(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("face ocr审核提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<FaceInitResponse> faceInit(Long userId,String transId,String transType,boolean isSdk) {
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		CertificateCenterDispatcherParam<FaceInitResponse> param = new CertificateCenterDispatcherParam<>();
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();

		if(kycCertificate == null) {
			param.setDispatcher(false);
		}else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		}else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		} else {
			param.setDispatcher(gray("FACE",userId));
		}

		if(!param.isDispatcher()) {
			log.info("face init提交account处理 userId:{}", userId);
			return param;
		}
		FaceInitRequest apiReq = new FaceInitRequest();
		apiReq.setUserId(userId);
		apiReq.setTransId(transId);
		apiReq.setType(transType);

		try {
			log.info("face init转发certificate center处理 userId:{}", userId);
			APIResponse<com.binance.certification.response.face.FaceInitResponse> apiResp ;
			if(isSdk) {
				apiResp = userFaceApi.faceSdkInit(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			}else {
				apiResp = userFaceApi.faceWebInit(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			}
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			if (apiResp.getData() == null) {
				log.warn("face init提交转发异常.应答结果为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			log.info("face init提交转发certificate_center返回 userId:{} response:{}", userId,apiResp.getData());
			FaceInitResponse response = new FaceInitResponse();
			BeanUtils.copyProperties(apiResp.getData(), response);
//			if(StringUtils.isNotBlank(response.getQrCode())) {
//				String qrCode = response.getQrCode().replace("nav/face/", "");
//				String cacheKey = String.format(FaceBusiness.FACE_SDK_QACODE_VALID_CACHE, qrCode);
//				//先删除历史的内容然后再放入新内容
//		        RedisCacheUtils.del(cacheKey);
//		        long timeOut = config.getQrCodeValidSecond();
//		        String cacheValue = JSON.toJSONString(new FaceBusiness.FaceSdkCache(userId, transId, FaceTransType.getByCode(transType)));
//		        RedisCacheUtils.set(cacheKey, cacheValue, timeOut);
//			}

			param.setResponse(response);
			return param;
		}catch(BusinessException e) {
			throw e;
		}catch (Exception e) {
			log.warn("face init提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public CertificateCenterDispatcherParam<Void> pcFaceVerifyPrivate(Long userId,FacePcPrivateResult result) {
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		CertificateCenterDispatcherParam<Void> param = new CertificateCenterDispatcherParam<>();

		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();
		
		if(kycCertificate == null) {
			param.setDispatcher(false);
		}else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		}else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		} else {
			param.setDispatcher(gray("FACE",userId));
		}

		if(!param.isDispatcher()) {
			log.info("pc face verify提交account处理 userId:{}",userId);
			return param;
		}

		FacePcPrivateResultRequest apiReq = new FacePcPrivateResultRequest();
		BeanUtils.copyProperties(result, apiReq);

		try {
			log.info("pc face verify提交certificate center处理 userId:{}",userId);
			APIResponse<Void> apiResp = userFaceApi.webFaceVerifyPrivate(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}
			return param;
		}catch(BusinessException e) {
			throw e;
		}catch (Exception e) {
			log.warn("face verify提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	public CertificateCenterDispatcherParam<FaceSdkResponse> appFaceSdkVerify(FaceSdkVerifyRequest request, Long userId) {
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		CertificateCenterDispatcherParam<FaceSdkResponse> param = new CertificateCenterDispatcherParam<>();
		
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();

		if(kycCertificate == null) {
			param.setDispatcher(false);
		}else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		}else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		}else {
			param.setDispatcher(gray("FACE",userId));
		}

		if(!param.isDispatcher()) {
			log.info("sdl face verify提交account处理 userId:{}",userId);
			return param;
		}

		com.binance.certification.request.FaceSdkVerifyRequest apiReq = new com.binance.certification.request.FaceSdkVerifyRequest();
		BeanUtils.copyProperties(request, apiReq);
		apiReq.setUserId(userId);
		try {
			log.info("sdk face verify提交certificate center处理 userId:{}",userId);
			APIResponse<com.binance.certification.response.face.FaceSdkResponse> apiResp = userFaceApi.appFaceSdkVerify(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			if (apiResp.getData() == null) {
				log.warn("face verify提交转发异常.应答结果为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			log.info("sdk face verify提交转发certificate_center返回 userId:{} response:{}", userId,apiResp.getData());
			FaceSdkResponse response = new FaceSdkResponse();
			BeanUtils.copyProperties(apiResp.getData(), response);
			if(apiResp.getData().getStatus()!=null) {
				response.setStatus(TransFaceLogStatus.valueOf(apiResp.getData().getStatus().name()));
			}
			param.setResponse(response);
			return param;
		}catch(BusinessException e) {
			throw e;
		}catch (Exception e) {
			log.warn("face verify提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public Integer changeWithdrawSecurityFaceStatus(SecurityFaceStatusRequest request) {
		Long userId = request.getUserId();
		if (gray("WITHDRAW_STATUS", userId)) {
			log.info("WITHDRAW_STATUS goto certificate-center userId:{}", userId);
			WithdrawFaceOnOffRequest onOffRequest = new WithdrawFaceOnOffRequest();
			onOffRequest.setUserId(userId);
			onOffRequest.setWithdrawFaceStatus(request.getWithdrawSecurityFaceStatus());
			onOffRequest.setWithdrawId(request.getWithdrawId());
			onOffRequest.setNeedEmail(request.isNeedEmail());
			onOffRequest.setSource(request.getSource() == null ? null : WithdrawFaceCheckSource.valueOf(request.getSource().name()));
			APIRequestHeader header = WebUtils.getAPIRequestHeader();
			APIResponse<WithdrawFaceOnOffResponse> response = withdrawFaceApi.onOffWithdrawFaceCheck(APIRequest.instance(header, onOffRequest));
			if (response == null || response.getStatus() != APIResponse.Status.OK) {
				if (response == null) {
					throw new BusinessException(GeneralCode.COMMON_ERROR);
				}else {
					throw new BusinessException(response.getCode(), response.getErrorData().toString(), response.getParams());
				}
			}else {
				return response.getData().getResult() != null && response.getData().getResult() ? 1 : 0;
			}
		}else {
			log.info("WITHDRAW_STATUS goto iWithdrawSecurityFace service. userId:{}", userId);
			return iWithdrawSecurityFace.changeWithdrawSecurityFaceStatus(request);
		}
	}

	public UserWithdrawFaceTipResponse checkWithdrawFaceStatus(Long userId) {
		if (gray("WITHDRAW_CHECK", userId)) {
			log.info("WITHDRAW_CHECK goto certificate-center userId:{}", userId);
			APIResponse<WithdrawFaceCheckResponse> response = withdrawFaceApi.withdrawFaceCheckStatus(userId);
			if (response == null || response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
				if (response == null) {
					throw new BusinessException(GeneralCode.COMMON_ERROR);
				}else {
					throw new BusinessException(response.getCode(), response.getErrorData().toString(), response.getParams());
				}
			}else {
				WithdrawFaceCheckResponse checkResponse = response.getData();
				UserWithdrawFaceTipResponse tipResponse = new UserWithdrawFaceTipResponse();
				BeanUtils.copyProperties(checkResponse, tipResponse);
				return tipResponse;
			}
		}else {
			log.info("WITHDRAW_CHECK goto iWithdrawSecurityFace service. userId:{}", userId);
			return iWithdrawSecurityFace.checkWithdrawFaceStatus(userId);
		}
	}

	public Boolean checkWithdrawFaceInHours(WithdrawFaceInHoursRequest request) {
		Long userId = request.getUserId();
		if (gray("WITHDRAW_HOUR", userId)) {
			log.info("WITHDRAW_HOUR goto certificate-center userId:{}", userId);
			com.binance.certification.request.WithdrawFaceInHoursRequest inHoursRequest = new com.binance.certification.request.WithdrawFaceInHoursRequest();
			inHoursRequest.setUserId(userId);
			inHoursRequest.setHours(request.getHours());
			APIRequestHeader header = WebUtils.getAPIRequestHeader();
			APIResponse<Boolean> response = withdrawFaceApi.withdrawFaceInHoursCheck(APIRequest.instance(header, inHoursRequest));
			if (response == null || response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
				if (response == null) {
					throw new BusinessException(GeneralCode.COMMON_ERROR);
				}else {
					throw new BusinessException(response.getCode(), response.getErrorData().toString(), response.getParams());
				}
			}else {
				return response.getData();
			}
		}else {
			log.info("WITHDRAW_HOUR goto iWithdrawSecurityFace service. userId:{}", userId);
			return iWithdrawSecurityFace.checkWithdrawFaceInHours(request);
		}
	}
	
	public CertificateCenterDispatcherParam<Void> auditBaseInfo(KycAuditRequest request) {
		Long userId = request.getUserId();

		CertificateCenterDispatcherParam<Void> param = new CertificateCenterDispatcherParam<>();
		log.info("base审核提交转发逻辑判断 userId:{}", userId);
		
		
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		String country = baseInfo == null ? null : baseInfo.getCountry();
		
		if (kycCertificate == null) {
			param.setDispatcher(false);
		} else if(CN_FLOW_DEFINE.equals(kycCertificate.getFlowDefine()) || CN_FLOW_V2_DEFINE.equals(kycCertificate.getFlowDefine())) {
			param.setDispatcher(true);
		} else if(StringUtils.equalsAnyIgnoreCase(country, "AU") && config.isCertificateCenterGraySwitch()) {
			param.setDispatcher(true);
		}else {
			param.setDispatcher(gray("BASIC",request.getUserId()));
		}
		
		if (!param.isDispatcher()) {
			log.info("base审核提交account处理 userId:{}", userId);
			return param;
		}
		
		try {
			com.binance.certification.request.KycAuditRequest apiReq = new com.binance.certification.request.KycAuditRequest();
			BeanUtils.copyProperties(request, apiReq);
			log.info("base审核提交转发certificate_center userId:{}", userId);
			APIResponse<Void> apiResp = kycCertificateAuditApi
					.auditBaseInfo(APIRequest.instance(WebUtils.getAPIRequestHeader(),apiReq));
			if(apiResp == null || apiResp.getStatus() != APIResponse.Status.OK) {
				String message = Objects.toString(apiResp == null ? "" : apiResp.getErrorData(), "");
	            throw new BusinessException(apiResp.getCode(), message, apiResp.getParams());
			}

			return param;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.warn("base审核提交转发异常 userId:{}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}
}
