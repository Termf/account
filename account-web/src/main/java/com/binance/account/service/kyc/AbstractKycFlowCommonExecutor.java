package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoHistoryMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;

/**
 * 抽象执行器
 * @author liufeng
 *
 */
public abstract class AbstractKycFlowCommonExecutor {
	
	@Resource
	protected KycCertificateMapper kycCertificateMapper;

	@Resource
	protected KycFillInfoMapper kycFillInfoMapper;
	
	@Resource
	protected KycFillInfoHistoryMapper kycFillInfoHistoryMapper;
	
	@Autowired
	protected ApolloCommonConfig config;
	
	@Resource
    protected ApplicationEventPublisher applicationEventPublisher;
	
	
	
	public abstract KycFlowResponse execute(KycFlowRequest kycFlowRequest);
	
	public boolean doubleWrite() {
		return config.isKycFlowDoubleWrite();
	}
	
}
