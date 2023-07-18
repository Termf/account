package com.binance.account.job;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.slf4j.Slf4j;

/**
 * FIAT-78:</br>
 * 用户（重置）流程结果由风控后置处理任务</br>
 * 用户重置失败到最大次数后，由风控决策引擎根据决策处理用户的后置状态</br>
 * 此任务延迟调用（这样风控结果才准确）接口，根据结果给用户加保护模式</br>
 */
@Slf4j
@JobHandler(value = "UserResetFlowRiskFeaturePostProcessJob")
@Component
public class UserResetFlowRiskFeaturePostProcessJob  extends IJobHandler {

	@Resource
	private QuestionModuleChecker resetChecker;
	
	@Override
	public ReturnT<String> execute(final String param) throws Exception {
		int clear = 5;
		int undo = 2;
		if (StringUtils.isNotBlank(param)) {
			String[] p = param.split(",");
			if (p.length > 1) {
				clear = NumberUtils.toInt(p[0], 5);
				undo = NumberUtils.toInt(p[1], 2);
			}
		}
		try {
			log.info("风控后置处理任务开始,clear:{},undo:{}", clear, undo);
			XxlJobLogger.log("风控后置处理任务开始,clear:{0},undo:{1}", clear, undo);
			long cur = System.currentTimeMillis();
			resetChecker.postProcessUserByRiskFeatrue(clear, undo);
			long now = System.currentTimeMillis();
			log.info("风控后置处理任务结束,耗时:{}ms", (now - cur));
			XxlJobLogger.log("风控后置处理任务结束,耗时:{0}ms", (now - cur));
			return ReturnT.SUCCESS;
		} catch (Exception e) {
			log.error("风控后置处理任务异常。",e);
			XxlJobLogger.log(e);
			return ReturnT.FAIL;
		}
	}
}
