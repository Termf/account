package com.binance.account.service.kyc;

import java.util.List;
import java.util.UUID;

import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.utils.TrackingUtils;

/**
 * 组合处理器
 * 
 * @author liufeng
 *
 */
public class ComposeKycFlowProcessor extends AbstractKycFlowProcessor {

	List<AbstractKycFlowCommonExecutor> executors;

	public void initExecutor(List<AbstractKycFlowCommonExecutor> executors) {
		this.executors = executors;
	}

	@Override
	public KycFlowResponse process(KycFlowRequest kycFlowRequest) {
		try {
			KycFlowResponse response = null;

			for (AbstractKycFlowCommonExecutor executor : executors) {
				response = executor.execute(kycFlowRequest);
				KycFlowContext.getContext().setKycFlowResponse(response);
			}
			if (this.syncGlobalEnd) {
				processGlobalEnd(kycFlowRequest);
			} else {
				KycFlowProcessFactory.THREAD_POOL.submit(new Runnable() {
					public void run() {
						TrackingUtils.putTracking("execute_global_end", UUID.randomUUID().toString());
						try {
							processGlobalEnd(kycFlowRequest);
						} finally {
							TrackingUtils.removeTracking();
							TrackingUtils.removeTraceId();
						}
					}
				});
			}
			return response;
		} catch (Exception e) {
			throw e;
		} finally {
			KycFlowContext.clean();
			KycEndContext.clean();
		}
	}

}
