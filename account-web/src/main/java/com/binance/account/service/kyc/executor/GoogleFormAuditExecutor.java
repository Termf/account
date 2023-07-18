package com.binance.account.service.kyc.executor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.validator.GoogleFormAuditValidator;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.utils.DateUtils;

@Service
public class GoogleFormAuditExecutor extends AbstractKycFlowCommonExecutor {
	@Autowired
	private GoogleFormAuditValidator validator;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		KycAuditRequest req = (KycAuditRequest) kycFlowRequest;
		
		validator.validateApiRequest(req);
		
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(req.getUserId());
		
		validator.validateKycCertificateStatus(kycCertificate, req.getKycCertificateStatus());
		
		KycCertificate record = new KycCertificate();
		record.setUserId(kycCertificate.getUserId());
		record.setGoogleFormStatus(req.getKycCertificateStatus().name());
		record.setGoogleFormTips(StringUtils.isBlank(req.getTips())?"":req.getTips());
		record.setUpdateTime(DateUtils.getNewUTCDate());
		kycCertificateMapper.updateByPrimaryKeySelective(record);
		
		return null;
	}

}
