package com.binance.account.job;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.binance.account.data.entity.certificate.KycExceptionTask;
import com.binance.account.service.certificate.IKycExceptionTask;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

/**
 * kyc 异常差错补偿
 * 
 * @author liufeng
 *
 */
@Log4j2
@JobHandler(value = "KycExceptionTaskJobHandler")
@Component
public class KycExceptionTaskJobHandler extends IJobHandler {

	@Resource
	private IKycExceptionTask iKycExceptionTask;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start();
			TrackingUtils.putTracking("KycExceptionTaskJobHandler", UUID.randomUUID().toString().replaceAll("-", ""));
			Date endTime = DateUtils.getNewUTCDate();
			Date startTime = DateUtils.addDays(endTime, -3);
			XxlJobLogger.log("开始执行 KycExceptionTaskJobHandler 执行参数:" + param);
			List<KycExceptionTask> results = iKycExceptionTask.selectPage(startTime, endTime, 0, 200, "INIT");
			XxlJobLogger.log("开始执行 KycExceptionTaskJobHandler 记录:" + results.size());

			if (results == null || results.size() < 1) {
				return SUCCESS;
			}

			for (KycExceptionTask task : results) {
				iKycExceptionTask.executeTask(task);
			}
			return SUCCESS;
		} catch (Exception e) {
			log.error("执行KycCnUserDoOcrJobHandler失败 param:{}", param, e);
			return FAIL;
		} finally {
			stopWatch.stop();
			XxlJobLogger.log("开始执行 KycExceptionTaskJobHandler完成 use {0}s", stopWatch.getTotalTimeSeconds());
			TrackingUtils.removeTracking();
			TrackingUtils.removeTraceId();
		}
	}

}
