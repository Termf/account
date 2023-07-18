package com.binance.account.service.kyc.executor.us;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.mq.IdmNotifyMsgSender;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.vo.idm.request.BaseInfoKycIdmRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.WebUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class JumioIdmExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private UserIndexMapper userIndexMapper;

	@Resource
	private UserIpMapper userIpMapper;

	@Resource
	private IdmApi idmApi;
	
	@Resource
	private IdmNotifyMsgSender idmNotifyMsgSender;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		
		log.info("jumio触发idm上送 userId:{}",kycFlowRequest.getUserId());
		
		KycFlowResponse kycFlowResponse = KycFlowContext.getContext().getKycFlowResponse();

		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
		Long userId = kycFlowRequest.getUserId();

		if (kycCertificate == null) {
			kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		}
		// 企业用户不发送IDM
		if (!KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			log.info("jumio触发idm上送.企业用户不发送IDM userId:{}",userId);
			return kycFlowResponse;
		}

//		KycFillInfo address = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.ADDRESS.name());
		KycFillInfo base = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());

//		if (address == null || base == null) {
		if (base == null) {
			log.info("jumio触发idm上送.地址/基础信息为空 userId:{}",userId);
			return kycFlowResponse;
		}

		if (StringUtils.isBlank(kycCertificate.getJumioStatus())) {
			log.info("jumio触发idm上送.jumio状态为空 userId:{}",userId);
			return kycFlowResponse;
		}
		// kyc jumio status 不为 PASS 或者 REFUSED 
		if (!StringUtils.equalsAny(kycCertificate.getJumioStatus(), KycCertificateStatus.PASS.name(),
				KycCertificateStatus.REFUSED.name())) {
			log.info("jumio触发idm上送.jumio状态不为PASS/REFUSED userId:{}",userId);
			return kycFlowResponse;
		}

		BaseInfoKycIdmRequest request = new BaseInfoKycIdmRequest();
		BeanUtils.copyProperties(base, request);

		String email = userIndexMapper.selectEmailById(kycCertificate.getUserId());
		String ip = WebUtils.getRequestIp();
		if (StringUtils.isNotBlank(email)) {
			request.setEmail(email);
		}
		if (StringUtils.isNotBlank(ip)) {
			request.setIp(ip);
		}
		request.setTid(base.getIdmTid());
//		request.setCity(address.getCity());
//		request.setCountry(address.getCountry());
//		request.setRegionState(address.getRegionState());
//		request.setAddress(address.getAddress());
//		request.setPostalCode(address.getPostalCode());
//		request.setBillFile(address.getBillFile());
//		request.setMobile(kycCertificate.getBindMobile());
//		request.setMobileCode(kycCertificate.getMobileCode());

		String requestJson = LogMaskUtils.maskJsonString2(JSON.toJSONString(request), "taxId","email");
		try {
			log.info("jumio idm 请求userId:{},request:{}", userId, requestJson);
			idmApi.jumioKycIdm(APIRequest.instance(request));
		}catch(Exception e) {
			log.error("jumio idm 请求失败转MQ 请求userId:{},request:{}", userId, requestJson, e);
			idmNotifyMsgSender.notifyJumioIdm(request);
		}
		

		return kycFlowResponse;
	}

}
