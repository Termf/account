package com.binance.account.service.kyc.executor.us;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
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

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FaceIdmExecutor extends AbstractKycFlowCommonExecutor {

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

		KycFlowResponse kycFlowResponse = KycFlowContext.getContext().getKycFlowResponse();

		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
		Long userId = kycFlowRequest.getUserId();

		if (kycCertificate == null) {
			kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		}

		if (kycCertificate == null) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

		if (!KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			return kycFlowResponse;
		}
		
		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getFaceStatus())) {
			return kycFlowResponse;
		}

		BaseInfoKycIdmRequest request = new BaseInfoKycIdmRequest();
		request.setUserId(userId);
		try {
			log.info("face idm 请求userId:{},request:{}", userId, request);
			idmApi.faceKycIdm(APIRequest.instance(request));
		}catch(Exception e) {
			log.error("face idm 请求异常转MQ通知 userId:{},request:{}", userId, request,e);
			idmNotifyMsgSender.notifyFace(request);
			
		}

		return kycFlowResponse;
	}
}