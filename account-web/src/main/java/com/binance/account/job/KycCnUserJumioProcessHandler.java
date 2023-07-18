package com.binance.account.job;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.KycCnIdCard;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.KycCnIdCardMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.vo.user.request.KycAuditRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

/**
 * 重置存量用户jumio 信息
 * 
 * @author liufeng
 *
 */
@Log4j2
@JobHandler(value = "KycCnUserJumioProcessHandler")
@Component
public class KycCnUserJumioProcessHandler extends IJobHandler {

	private static final String TYPE_FLAG = "flag:";
	
	private static final String FAIL_REASON = "failReason:";

	private static final String PAGE_SIZE = "pageSize:";

	private int pageSize=100;
	
	private String failReason="OCR_RESET";

	@Resource
	private KycCnIdCardMapper kycCnIdCardMapper;

	@Resource
	private UserKycApproveMapper userKycApproveMapper;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private IUserKyc kyc;

	private static final String APPROVE_MISS = "APPROVE_MISS";

	private static final String NOT_USER_KYC = "NOT_USER_KYC";

	private static final String KYC_RESET_FAIL = "KYC_RESET_FAIL";

	private static final String KYC_REVIEW_FAIL = "KYC_REVIEW_FAIL";

	public ReturnT<String> execute(String param) throws Exception {
		XxlJobLogger.log("开始执行 KycCnUserJumioProcessHandler 执行参数:" + param);
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start();
			TrackingUtils.putTracking("KycCnUserJumioProcessHandler", UUID.randomUUID().toString().replaceAll("-", ""));
			if (StringUtils.isBlank(param)) {
				return SUCCESS;
			}
			if (param.startsWith(TYPE_FLAG)) {
				processFlag(param.replace(TYPE_FLAG, ""));
			}

			if (param.startsWith(PAGE_SIZE)) {
				this.pageSize = Integer.parseInt(param.replace(PAGE_SIZE, ""));
				XxlJobLogger.log("开始执行 KycCnUserJumioProcessHandler 修改pagesize.当前pageSize :" + pageSize);
			}
			
			if (param.startsWith(FAIL_REASON)) {
				this.failReason = param.replace(FAIL_REASON, "");
				XxlJobLogger.log("开始执行 KycCnUserJumioProcessHandler 修改pagesize.当前pageSize :" + pageSize);
			}

			return SUCCESS;

		} catch (Exception e) {
			log.error("执行KycCnUserDoOcrJobHandler失败 param:{}", param, e);
			return FAIL;
		} finally {
			stopWatch.stop();
			TrackingUtils.removeTracking();
			TrackingUtils.removeTraceId();
			XxlJobLogger.log("执行KycCnUserDoOcrJobHandler完成 use {0}s", stopWatch.getTotalTimeSeconds());
		}
	}

	private void processFlag(String flag) {
		List<KycCnIdCard> results = kycCnIdCardMapper.selectPageResetkyc(0, this.pageSize, null,
				Integer.parseInt(flag),"WAITREVIEW");

		if (results == null || results.isEmpty()) {
			XxlJobLogger.log("执行KycCnUserDoOcrJobHandler完成 无记录可执行");
			return;
		}

		List<UserKycApprove> waitRefused = new ArrayList<UserKycApprove>();
		for (KycCnIdCard kycCnIdCard : results) {
			Long userId = kycCnIdCard.getUserId();
			UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
			if (userKycApprove == null) {
				updateStatus(APPROVE_MISS, "REVIEWFAIL", userId);
				continue;
			}
			if (!new Integer(1).equals(userKycApprove.getCertificateType())) {
				updateStatus(NOT_USER_KYC, "REVIEWFAIL", userId);
				continue;
			}
			try {
				boolean b = processUserJumioPass(userId, userKycApprove.getCertificateId());
				if (b) {
					waitRefused.add(userKycApprove);
					updateStatus(userKycApprove.getCertificateId()+"", "WAITRESET", userId);
				}
			} catch (Exception e) {
				updateStatus(KYC_REVIEW_FAIL + "_" + e.getMessage(), "REVIEWFAIL", userId);
			}
		}
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e1) {
			log.error("thread sleep has error",e1);
		}
		
		for (UserKycApprove userKycApprove : waitRefused) {
			Long userId = userKycApprove.getUserId();
			try {
				boolean b = processUserKycRefused(userId, userKycApprove.getCertificateId());
				if (b) {
					updateStatus("", "RESETSUCC", userId);
				}
			} catch (Exception e) {
				updateStatus(KYC_RESET_FAIL + "_" + e.getMessage(), "RESETFAIL", userId);
			}
		}
	}

	private boolean processUserJumioPass(Long userId, Long id) throws Exception {
		KycAuditRequest kycAuditRequest = new KycAuditRequest();
		kycAuditRequest.setId(id);
		kycAuditRequest.setUserId(userId);
		kycAuditRequest.setStatus(KycStatus.jumioPassed);
		APIResponse<?> response = kyc.audit(APIRequest.instance(kycAuditRequest));
		if ( response == null ||response.getStatus() != Status.OK ) {
			updateStatus(KYC_REVIEW_FAIL, "REVIEWFAIL", userId);
			return false;
		}
		return true;
	}
	
	private boolean processUserKycRefused(Long userId, Long id) throws Exception {
		KycAuditRequest kycAuditRequest = new KycAuditRequest();
		kycAuditRequest.setId(id);
		kycAuditRequest.setUserId(userId);
		kycAuditRequest.setStatus(KycStatus.refused);
		kycAuditRequest.setFailReason(failReason);
		APIResponse<?> response = kyc.audit(APIRequest.instance(kycAuditRequest));
		if ( response == null ||response.getStatus() != Status.OK ) {
			updateStatus(KYC_RESET_FAIL, "RESETFAIL", userId);
			return false;
		}
		return true;
	}

	private void updateStatus(String failReason, String status, Long userId) {
		KycCnIdCard record = new KycCnIdCard();
		record.setStatus(status);
		record.setUserId(userId);
		record.setFailReason(failReason);
		kycCnIdCardMapper.updateByPrimaryKeySelective(record);
	}
}
