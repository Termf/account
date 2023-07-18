package com.binance.account.service.certificate;

import java.util.Date;
import java.util.List;

import com.binance.account.data.entity.certificate.KycExceptionTask;
import com.binance.inspector.common.enums.FaceTransType;

public interface IKycExceptionTask {
	
	void addJumioInitFaceException(Long userId,FaceTransType faceTransType,String bizId);
	
	void executeTask(KycExceptionTask task);
	
	List<KycExceptionTask> selectPage(Date startTime,Date endTime,int start,int rows,String status);

}
