package com.binance.account.service.kyc;

import java.util.UUID;

import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.utils.TrackingUtils;

/**
 * 原子处理器
 * 
 * @author liufeng
 *
 */
public class AtomKycFlowProcessor extends AbstractKycFlowProcessor {

	AbstractKycFlowCommonExecutor executor;

	public void initExecutor(AbstractKycFlowCommonExecutor executor) {
		this.executor = executor;
	}

	@Override
	public KycFlowResponse process(KycFlowRequest kycFlowRequest) {
		try {
			KycFlowResponse response = executor.execute(kycFlowRequest);
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
		} finally {
			KycFlowContext.clean();
			KycEndContext.clean();
		}

	}

}
