package com.binance.account.job;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.binance.account.data.entity.certificate.KycExceptionTask;
import com.binance.account.service.certificate.IKycExceptionTask;
import com.binance.account.service.kyc.BaseTest;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.utils.DateUtils;

public class KycExceptionTaskTest extends BaseTest{

	@Resource
	IKycExceptionTask iKycExceptionTask;

	
	@Test
	public void addJumioInitFaceException() {
		iKycExceptionTask.addJumioInitFaceException(12346l, FaceTransType.KYC_USER, "fdsafdsafdsafdsafads");
		iKycExceptionTask.addJumioInitFaceException(12346l, FaceTransType.KYC_USER, "fdsafdsafdsafdsafads");
		iKycExceptionTask.addJumioInitFaceException(12346l, FaceTransType.KYC_USER, "fdsafdsafdsafdsafads");
		Date endTime = DateUtils.getNewUTCDate();
		Date startTime = DateUtils.addDays(endTime, -3);
		List<KycExceptionTask> results = iKycExceptionTask.selectPage(startTime, endTime, 0, 200, "INIT");
		for (KycExceptionTask kycExceptionTask : results) {
			iKycExceptionTask.executeTask(kycExceptionTask);
		}


	}
	
}
