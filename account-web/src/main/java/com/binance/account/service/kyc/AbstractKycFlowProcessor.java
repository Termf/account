package com.binance.account.service.kyc;

import java.util.List;

import com.binance.account.service.kyc.endHandler.AbstractEndHandler;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;

public abstract class AbstractKycFlowProcessor implements KycFlowProcessor{

	List<AbstractEndHandler> globalEnds;
	
	boolean syncGlobalEnd = true;
	
	@Override
	public abstract KycFlowResponse process(KycFlowRequest kycFlowRequest);
	
	public void initGlobalEnd(List<AbstractEndHandler> globalEnds,boolean syncGlobalEnd) {
		this.globalEnds = globalEnds;
		this.syncGlobalEnd = syncGlobalEnd;
	}
	
	@Override
	public void processGlobalEnd(KycFlowRequest kycFlowRequest) {
		if(globalEnds == null) {
			return;
		}
		for (AbstractEndHandler endExecutor : globalEnds) {
			endExecutor.initContext(kycFlowRequest.getUserId());
			if(endExecutor.isDoHandler()) {
				endExecutor.handler();
			}
		}
	}
	
	

}
