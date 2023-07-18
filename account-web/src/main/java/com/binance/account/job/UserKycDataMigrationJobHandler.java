package com.binance.account.job;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.service.certificate.IUserKycDataMigration;
import com.binance.platform.common.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

@Log4j2
@JobHandler(value = "UserKycDataMigrationJobHandler")
@Component
public class UserKycDataMigrationJobHandler extends IJobHandler {
	@Resource
	private IUserKycDataMigration iUserKycDataMigration;
	
	@Autowired
	private ApolloCommonConfig config;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start();
			TrackingUtils.saveTrace("UserKycDataMigrationJobHandler"+UUID.randomUUID().toString().replaceAll("-", ""));
			
			XxlJobLogger.log("开始执行 UserKycDataMigrationJobHandler 执行参数:" + param);
			
			List<UserKycApprove> results = null;
			
			
			if(StringUtils.isBlank(param)) {
				results = iUserKycDataMigration.selectPage(null, 0, config.getKycDataMigrationRunSize());
			}else if(param.startsWith("code:")) {
				results = iUserKycDataMigration.selectPage(param.replace("code:", ""), 0, config.getKycDataMigrationRunSize());
			}else if(param.startsWith("user:")) {
				iUserKycDataMigration.moveToKycCertificateByUserId(Long.parseLong(param.replace("user:", "")));
				XxlJobLogger.log("执行 UserKycDataMigrationJobHandler完成 userId:" + param.replace("user:", ""));
				return SUCCESS;
			}
			
			if(results == null || results.isEmpty()) {
				return SUCCESS;
			}
			XxlJobLogger.log("开始执行 UserKycDataMigrationJobHandler 记录:" + results.size());
			
			for (UserKycApprove userKycApprove : results) {
				log.info("UserKycDataMigrationJobHandler开始执行数据迁移 userId:{}", userKycApprove.getUserId());
				try {
					iUserKycDataMigration.moveToKycCertificate(userKycApprove);
				}catch(Exception e) {
					log.warn("迁移新流程异常 userId:{}", userKycApprove.getUserId(), e);
					iUserKycDataMigration.addExceptionTag(userKycApprove);
				}
			}
			return SUCCESS;
		} catch (Exception e) {
			log.error("执行UserKycDataMigrationJobHandler失败 param:{}", param, e);
			return FAIL;
		} finally {
			stopWatch.stop();
			TrackingUtils.clearTrace();
			XxlJobLogger.log("执行UserKycDataMigrationJobHandler完成 use {0}s", stopWatch.getTotalTimeSeconds());
		}
	}

}
