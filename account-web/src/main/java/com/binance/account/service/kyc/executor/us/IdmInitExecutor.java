package com.binance.account.service.kyc.executor.us;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.common.enums.KycCertificateStatus;
import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.vo.idm.request.BaseInfoKycIdmRequest;
import com.binance.inspector.vo.idm.response.BaseInfoKycIdmResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.WebUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class IdmInitExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private IdmApi idmApi;

	@Resource
	private UserIndexMapper userIndexMapper;
	
	@Resource
	private UserIpMapper userIpMapper;
	
	@Value("${idm.refuse.process.status:REVIEW}")
    private String idmRefuseProcessStatus;

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
		KycFillInfo kycFillInfo = KycFlowContext.getContext().getKycFillInfo();

		// 企业认证不做IDM.BASE INFO 直接pass
		KycCertificateStatus status = KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())
				? KycCertificateStatus.PASS
				: idmAuth(kycCertificate, kycFillInfo);

		BaseInfoResponse response = (BaseInfoResponse) KycFlowContext.getContext().getKycFlowResponse();

		if (status == null) {
			kycCertificate.setBaseFillStatus(KycCertificateStatus.REVIEW.name());
			kycCertificate.setBaseFillTips("idm process exception need manual review");
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateStatus(kycCertificate);
			response.setBaseFillStatus(KycCertificateStatus.valueOf(kycCertificate.getBaseFillStatus()));
			response.setBaseFillTips(kycCertificate.getBaseFillTips());
			return response;
		}
		kycCertificate.setBaseFillStatus(status.name());
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		kycCertificateMapper.updateStatus(kycCertificate);

		KycFillInfo record = new KycFillInfo();
		record.setStatus("SUCCESS");
		record.setUpdateTime(DateUtils.getNewUTCDate());
		record.setUserId(kycFillInfo.getUserId());
		record.setFillType(kycFillInfo.getFillType());
		record.setIdmTid(kycFillInfo.getIdmTid());
		kycFillInfoMapper.updateByUkSelective(record);

		response.setBaseFillStatus(KycCertificateStatus.valueOf(kycCertificate.getBaseFillStatus()));
		response.setBaseFillTips(MessageMapHelper.getMessage(kycCertificate.getBaseFillTips(), LanguageEnum.findByLang(kycFillInfo.getCountry().toLowerCase())));
		return response;
	}

	private KycCertificateStatus idmAuth(KycCertificate kycCertificate, KycFillInfo kycFillInfo) {
		BaseInfoKycIdmRequest request = new BaseInfoKycIdmRequest();
		BeanUtils.copyProperties(kycFillInfo, request);
		String email = userIndexMapper.selectEmailById(kycCertificate.getUserId());
		String ip = WebUtils.getRequestIp();
		if (StringUtils.isNotBlank(email)) {
			request.setEmail(email);
		}
		if(StringUtils.isNotBlank(ip)) {
			request.setIp(ip);
		}
		if (StringUtils.isNotBlank(kycFillInfo.getIdmTid())) {
			request.setTid(kycFillInfo.getIdmTid());
		}
		
		String requestJson = LogMaskUtils.maskJsonString2(JSON.toJSONString(request), "taxId","email");
		log.info("Idm初始调用inspector userId:{},request:{}", request.getUserId(), requestJson);
		
		APIResponse<BaseInfoKycIdmResponse> response = idmApi.baseInfoKycIdm(APIRequest.instance(request));
		if (response == null || response.getStatus() != Status.OK || response.getData() == null) {
			log.warn("Idm初始调用inspector应答为空 userId:{}", kycFillInfo.getUserId());
			return KycCertificateStatus.REFUSED;
		}
		BaseInfoKycIdmResponse data = response.getData();
		log.info("Idm初始调用inspector返回 userId:{},response:{}", request.getUserId(), data);
		if(StringUtils.isBlank(kycFillInfo.getIdmTid())) {
			kycFillInfo.setIdmTid(data.getMtid());
		}
		switch (data.getRes()) {
		case "ACCEPT":
			kycCertificate.setBaseFillTips("base info pass");
			return KycCertificateStatus.PASS;
		case "DENY":
			kycCertificate.setBaseFillTips(StringUtils.isBlank(data.getErrorCode()) ? "Information is refused" : data.getErrorCode());
			KycCertificateStatus status = KycCertificateStatus.getByName(idmRefuseProcessStatus);
			if(status == null) {
				return KycCertificateStatus.REFUSED;
			}
			return status;
		default:
			kycCertificate.setBaseFillTips("Information under review");
			return KycCertificateStatus.REVIEW;
		}
	}

}
