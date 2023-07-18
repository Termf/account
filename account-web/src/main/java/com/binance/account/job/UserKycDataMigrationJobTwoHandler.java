package com.binance.account.job;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.service.certificate.IUserKycDataMigration;
import com.binance.platform.common.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

@Log4j2
@JobHandler(value = "UserKycDataMigrationJobTwoHandler")
@Component
public class UserKycDataMigrationJobTwoHandler extends IJobHandler {

	@Resource
	private IUserKycDataMigration iUserKycDataMigration;

	@Autowired
	private ApolloCommonConfig config;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start();
			TrackingUtils
					.saveTrace("UserKycDataMigrationJobTwoHandler" + UUID.randomUUID().toString().replaceAll("-", ""));

			XxlJobLogger.log("开始执行 UserKycDataMigrationJobTwoHandler 执行参数:" + param);

			log.info("开始执行 UserKycDataMigrationJobTwoHandler 执行参数:{}" + param);

			List<UserKyc> userList = null;
			List<CompanyCertificate> companyList = null;

			if (StringUtils.isBlank(param)) {
				userList = iUserKycDataMigration.selectUserPage(0, config.getKycDataMigrationRunSize());
			} 

			if (CollectionUtils.isEmpty(userList)) {
				log.info("开始执行 UserKycDataMigrationJobTwoHandler. user为空. 开始查询company");
				companyList = iUserKycDataMigration.selectCompanyPage(0, config.getKycDataMigrationRunSize());
				if (CollectionUtils.isEmpty(companyList)) {
					log.info("开始执行 UserKycDataMigrationJobTwoHandler. company为空. ");
					return SUCCESS;
				}
			}
			
			for (UserKyc userKyc : userList) {
				log.info("UserKycDataMigrationJobTwoHandler开始执行数据迁移 个人userId:{}", userKyc.getUserId());
				try {
					iUserKycDataMigration.moveUserKyc(userKyc);
				}catch(Exception e) {
					log.warn("UserKycDataMigrationJobTwoHandler异常 个人userId:{}", userKyc.getUserId(), e);
					iUserKycDataMigration.expiredUser(userKyc, "exception");
				}
			}
			
			if (CollectionUtils.isEmpty(companyList)) {
				log.info("开始执行 UserKycDataMigrationJobTwoHandler. company为空. ");
				return SUCCESS;
			}
			
			for (CompanyCertificate companyCertificate : companyList) {
				log.info("UserKycDataMigrationJobTwoHandler开始执行数据迁移 企业userId:{}", companyCertificate.getUserId());
				try {
					iUserKycDataMigration.moveCompany(companyCertificate);
				}catch(Exception e) {
					log.warn("UserKycDataMigrationJobTwoHandler异常 企业userId:{}", companyCertificate.getUserId(), e);
					iUserKycDataMigration.expiredCompany(companyCertificate, "exception");
				}
			}

			
			return SUCCESS;
		} catch (Exception e) {
			log.error("执行UserKycDataMigrationJobTwoHandler失败 param:{}", param, e);
			return FAIL;
		} finally {
			stopWatch.stop();
			TrackingUtils.clearTrace();
			XxlJobLogger.log("执行UserKycDataMigrationJobTwoHandler完成 use {0}s", stopWatch.getTotalTimeSeconds());
		}
	}

}
