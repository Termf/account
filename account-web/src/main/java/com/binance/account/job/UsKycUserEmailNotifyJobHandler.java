package com.binance.account.job;

import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.binance.account.service.user.IUserKycEmailNotify;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;
/**
 * 美国站定时任务.
 * 1.用户注册后24小时/72小时/30天内未通过basic 邮件通知用户
 * 2.用户通过basic后72小时内未发起交易 邮件通知用户
 * @author liufeng
 *
 */

@Log4j2
@JobHandler(value = "UsKycUserEmailNotifyJobHandler")
@Component
public class UsKycUserEmailNotifyJobHandler extends IJobHandler {
	@Resource
	IUserKycEmailNotify iUserKycEmailNotify;

	@Override
	public ReturnT<String> execute(String param) throws Exception {

		XxlJobLogger.log("开始执行 UsKycUserEmailNotifyJobHandler 执行参数:" + param);
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start();
			TrackingUtils.putTracking("UsKycUserEmailNotifyJobHandler",
					UUID.randomUUID().toString().replaceAll("-", ""));
			iUserKycEmailNotify.doTask();
			return SUCCESS;
		} catch (Exception e) {
			log.error("执行UsKycUserEmailNotifyJobHandler失败 param:{}", param, e);
			return FAIL;
		} finally {
			stopWatch.stop();
			TrackingUtils.removeTracking();
			TrackingUtils.removeTraceId();
			XxlJobLogger.log("执行KycCnUserDoOcrJobHandler完成 use {0}s", stopWatch.getTotalTimeSeconds());
		}
	}

}
