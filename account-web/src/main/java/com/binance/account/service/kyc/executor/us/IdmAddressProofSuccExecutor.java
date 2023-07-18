package com.binance.account.service.kyc.executor.us;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
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
/**
 * idm添加proof tag 以及上传prof
 * @author liufeng
 *
 */
@Service
@Log4j2
public class IdmAddressProofSuccExecutor extends AbstractKycFlowCommonExecutor {

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

		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getAddressStatus())) {
			return kycFlowResponse;
		}


		KycFillInfo address = KycFlowContext.getContext().getKycFillInfo();
		if(address  == null) {
			address = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.ADDRESS.name());
		}
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		
		BaseInfoKycIdmRequest request = new BaseInfoKycIdmRequest();
		request.setUserId(userId);
		request.setTid(baseInfo.getIdmTid());
		request.setBillFile(address.getBillFile());
		
		try {
			log.info("address proof tag idm 请求userId: {},request: {}", userId, request);
			idmApi.addressIdmProTag(APIRequest.instance(request));
		}catch(Exception e) {
			log.error("address proof tag idm 请求异常转MQ调用 userId: {},request: {}", userId, request,e);
			idmNotifyMsgSender.notifyAddressTag(request);
		}

		return kycFlowResponse;
	}

}
