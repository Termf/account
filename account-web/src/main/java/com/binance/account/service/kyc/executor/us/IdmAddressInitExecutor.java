package com.binance.account.service.kyc.executor.us;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
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
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.WebUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class IdmAddressInitExecutor extends AbstractKycFlowCommonExecutor {

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
		Long userId = kycFlowRequest.getUserId();
		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
		KycFillInfo addressFillInfo = KycFlowContext.getContext().getKycFillInfo();
		KycFlowResponse response = KycFlowContext.getContext().getKycFlowResponse();
		if (kycCertificate == null) {
			kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		}

		if (addressFillInfo == null) {
			addressFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.ADDRESS.name());
		}

		KycFillInfo baseFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());

		if (kycCertificate == null || baseFillInfo == null || addressFillInfo == null) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

		BaseInfoKycIdmRequest request = new BaseInfoKycIdmRequest();
		BeanUtils.copyProperties(baseFillInfo, request);

		String email = userIndexMapper.selectEmailById(kycCertificate.getUserId());
		String ip = WebUtils.getRequestIp();
		if (StringUtils.isNotBlank(email)) {
			request.setEmail(email);
		}
		if (StringUtils.isNotBlank(ip)) {
			request.setIp(ip);
		}
		request.setTid(baseFillInfo.getIdmTid());
		request.setCity(addressFillInfo.getCity());
		request.setCountry(addressFillInfo.getCountry());
		request.setRegionState(addressFillInfo.getRegionState());
		request.setAddress(addressFillInfo.getAddress());
		request.setPostalCode(addressFillInfo.getPostalCode());
		
		String requestJson = LogMaskUtils.maskJsonString2(JSON.toJSONString(request), "taxId","email");
		try {
			log.info("地址idm 请求userId:{},request:{}", userId, requestJson);
			idmApi.addressInfoKycIdm(APIRequest.instance(request));
		}catch(Exception e) {
			log.error("地址idm 请求失败转MQ 通知 userId:{},request:{}", userId, requestJson);
			idmNotifyMsgSender.notifyAddress(request);
		}

		return response;
	}

}
