package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.KycCnIdCard;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.KycCnIdCardMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.inspector.api.FaceIdApi;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.faceid.request.IdCardOcrDirectRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * kyc存量中国用户重做OCR
 * 
 * @author liufeng
 *
 */
@Log4j2
@JobHandler(value = "KycCnUserDoOcrJobHandler")
@Component
public class KycCnUserDoOcrJobHandler extends IJobHandler {
	private static final String TYPE_STATUS = "status:";

	private static final String TYPE_USER = "user:";

	private static final String TYPE_FLAG = "flag:";

	@Resource
	private UserKycApproveMapper userKycApproveMapper;

	@Resource
	private KycCnIdCardMapper kycCnIdCardMapper;

	@Resource
	private JumioMapper jumioMapper;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private FaceIdApi faceIdApi;

	@Value("${kyc.cnIdcard.ocrJob.size:200}")
	private int pageSize;

	private static final String APPROVE_MISS = "APPROVE_MISS";

	private static final String CERTIFICATE_ID_MISS = "CERTIFICATE_ID_MISS";

	private static final String USER_KYC_MISS = "USER_KYC_MISS";

	private static final String USER_KYC_NOT_PASS = "USER_KYC_NOT_PASS";

	private static final String JUMIO_ID_MISS = "JUMIO_ID_MISS";

	private static final String JUMIO_MISS = "JUMIO_MISS";

	private static final String FRONT_OR_BACK_MISS = "FRONT_OR_BACK_MISS";

	private static final String REQUEST_INSPECT_ERROR = "REQUEST_INSPECT_ERROR";

	private static final String JUMIO_NOT_ID_CARD = "JUMIO_NOT_ID_CARD";

	private static final String OCR_NAME_MISS = "OCR_NAME_MISS";

	private Integer flagUser = null;

	private static ExecutorService executorService = Executors.newFixedThreadPool(3);

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		XxlJobLogger.log("开始执行 KycCnUserDoOcrJobHandler 执行参数:" + param);
		if (StringUtils.isBlank(param)) {
			return FAIL;
		}
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start();
			TrackingUtils.putTracking("KycCnUserDoOcrJobHandler", UUID.randomUUID().toString().replaceAll("-", ""));

			if (StringUtils.isBlank(param)) {
				log.warn("执行KycCnUserDoOcrJobHandler失败 执行类型为空 param:{}", param);
				return FAIL;
			}

			if (param.startsWith(TYPE_STATUS)) {
				flagUser = null;
				handle(null);
			}

			if (param.startsWith(TYPE_USER)) {
				String userEx = param.replace(TYPE_USER, "");
				String[] users = userEx.split(",", -1);
				List<Long> userIds = new ArrayList<Long>();
				for (String string : users) {
					userIds.add(Long.parseLong(string));
				}
				handle(userIds);
			}

			if (param.startsWith(TYPE_FLAG)) {
				String flagEx = param.replace(TYPE_FLAG, "");
				flagUser = Integer.valueOf(flagEx);
				handle(null);
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

	private void handle(List<Long> userIds) throws ParseException {
		long count = kycCnIdCardMapper.countByStatus(userIds);
		if (count < 1) {
			log.info("执行KycCnUserDoOcrJobHandler 当前无处理任务 count:{}", count);
			XxlJobLogger.log("执行KycCnUserDoOcrJobHandler 当前无处理任务 count:{0}", count);
			return;
		}

		log.info("执行KycCnUserDoOcrJobHandler 获取记录数count:{} pageSize : {}", count, pageSize);
		XxlJobLogger.log("执行KycCnUserDoOcrJobHandler 获取记录数count:{0} pageSize : {1}", count, pageSize);

		List<KycCnIdCard> results = kycCnIdCardMapper.selectPageByStatus(0, pageSize, userIds, flagUser);
		List<Future<String>> futures = new ArrayList<Future<String>>();
		for (final KycCnIdCard kycCnIdCard : results) {
			Future<String> result = executorService.submit(new Callable<String>() {
				public String call() throws Exception {
					try {
						process(kycCnIdCard);
						return "SUCCSS";
					}catch(Exception e) {
						return "ERROR";
					}
				}
			});
			futures.add(result);
		}
		
		for(int i = 0 ; i < futures.size() ; i ++) {
			try {
				String result = futures.get(i).get();
			} catch (Exception e) {
				log.error("执行KycCnUserDoOcrJobHandler 异常",e);
			}
		}
	}

	private void process(KycCnIdCard kycCnIdCard) {
		try {
			Long userId = kycCnIdCard.getUserId();
			UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
			if (userKycApprove == null) {
				updateStatus(APPROVE_MISS, "REFUSED", userId);
				return;
			}
			if (userKycApprove.getCertificateId() == null) {
				updateStatus(CERTIFICATE_ID_MISS, "REFUSED", userId);
				return;
			}

			UserKyc userKyc = userKycMapper.getById(userId, userKycApprove.getCertificateId());
			if (userKyc == null) {
				updateStatus(USER_KYC_MISS, "REFUSED", userId);
				return;
			}
			if (!KycStatus.passed.equals(userKyc.getStatus())) {
				updateStatus(USER_KYC_NOT_PASS, "REFUSED", userId);
				return;
			}
			if (StringUtils.isBlank(userKycApprove.getJumioId())) {
				updateStatus(JUMIO_ID_MISS, "REFUSED", userId);
				return;
			}
			Jumio jumio = jumioMapper.selectByPrimaryKey(userId, userKycApprove.getJumioId());
			if (jumio == null) {
				updateStatus(JUMIO_MISS, "REFUSED", userId);
				return;
			}

			if (!"ID_CARD".equals(jumio.getDocumentType())) {
				updateStatus(JUMIO_NOT_ID_CARD, "REFUSED", userId);
				return;
			}

			if (StringUtils.isAnyBlank(jumio.getFront(), jumio.getBack())) {
				updateStatus(FRONT_OR_BACK_MISS, "REFUSED", userId);
				return;
			}

			IdCardOcrDirectRequest request = new IdCardOcrDirectRequest();
			request.setUserId(userId);
			request.setFront(jumio.getFront());
			request.setBack(jumio.getBack());
			request.setFace(jumio.getFace());
			try {
				APIResponse<FaceIdCardOcrVo> response = faceIdApi.verifyIdCardOcrDirect(APIRequest.instance(request));
				if (response == null) {
					log.warn("kyc cn ocr request inspector null {}", userId);
					updateStatus(REQUEST_INSPECT_ERROR, "REFUSED", userId);
					return;
				}
				if (response.getStatus() != Status.OK || response.getData() == null) {
					log.warn("kyc cn ocr request inspector fail userId:{} {}", userId, JSON.toJSONString(response));
					updateStatus(response.getErrorData().toString(), "REFUSED", userId);
					return;
				}
				FaceIdCardOcrVo data = response.getData();
				String name = data.getName();
				if (IdCardOcrStatus.PASS.equals(data.getStatus()) && StringUtils.isNotBlank(name)) {
					KycCnIdCard record = new KycCnIdCard();
					record.setStatus("PASS");
					record.setUserId(userId);
					record.setFirstName(userKycApprove.getBaseInfo().getFirstName());
					record.setMiddleName(userKycApprove.getBaseInfo().getMiddleName());
					record.setLastName(userKycApprove.getBaseInfo().getLastName());
					kycCnIdCardMapper.updateByPrimaryKeySelective(record);

					UserKycApprove.BaseInfo baseInfo = new UserKycApprove.BaseInfo();
					baseInfo.setFirstName(name);
					baseInfo.setLastName(null);
					UserKycApprove approveRecord = new UserKycApprove();
					approveRecord.setUserId(userId);
					approveRecord.setBaseInfo(baseInfo);
					userKycApproveMapper.updateOcrResult(approveRecord);

					UserKyc userKycRecord = new UserKyc();
					UserKyc.BaseInfo userKycBase = new UserKyc.BaseInfo();
					userKycBase.setFirstName(name);
					userKycBase.setLastName(null);
					userKycBase.setMiddleName(null);
					userKycRecord.setBaseInfo(userKycBase);
					userKycRecord.setUserId(userId);
					userKycRecord.setId(userKyc.getId());
					userKycMapper.updateOcrResult(userKycRecord);

					if (StringUtils.isBlank(jumio.getNumber())) {
						Jumio jumioRecord = new Jumio();
						jumioRecord.setId(jumio.getId());
						jumioRecord.setUserId(jumio.getUserId());
						jumioRecord.setNumber(data.getIdcardNumber());
						jumioMapper.updateOcrResult(jumioRecord);
					}
					return;
				}
				if (StringUtils.isNotBlank(data.getFailReason())) {
					updateStatus(data.getFailReason(), "REFUSED", userId);
				} else {
					updateStatus(OCR_NAME_MISS, "REFUSED", userId);
				}
				return;
			} catch (Exception e) {
				log.warn("kyc cn ocr request inspector error userId:{} {}", userId, e.getMessage());
				XxlJobLogger.log("kyc cn ocr request inspector error userId:{0} {1}", userId, e.getMessage());
				updateStatus(REQUEST_INSPECT_ERROR, "REFUSED", userId);
				return;
			}
		} catch (Exception e) {
			log.error("执行KycCnUserDoOcrJobHandler kycCnIdCard执行异常 info: {}", kycCnIdCard);
		}
	}

	private void updateStatus(String failReason, String status, Long userId) {
		KycCnIdCard record = new KycCnIdCard();
		record.setStatus(status);
		record.setUserId(userId);
		record.setFailReason(failReason);
		kycCnIdCardMapper.updateByPrimaryKeySelective(record);
	}

}
