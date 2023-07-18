package com.binance.account.service.certificate.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.KycExceptionTaskType;
import com.binance.account.data.entity.certificate.KycExceptionTask;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.KycExceptionTaskMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.certificate.IKycExceptionTask;
import com.binance.account.service.security.IUserFace;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.utils.DateUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class KycExceptionTaskBusiness implements IKycExceptionTask {

	@Resource
	private KycExceptionTaskMapper kycExceptionTaskMapper;

	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;

	@Resource
	private IUserFace iUserFace;

	@Override
	public void addJumioInitFaceException(Long userId, FaceTransType faceTransType, String bizId) {
		try {
			KycExceptionTask task = kycExceptionTaskMapper.selectByUk(userId,
					KycExceptionTaskType.JUMIO_INIT_FACE.getCode());

			if (task != null && "INIT".equals(task.getExecuteStatus())) {
				log.info("添加jumio结果通知初始化face流程.当前已存在待处理任务 userId:{}", userId);
				return;
			}
			// 如果已经完成新建任务。
			kycExceptionTaskMapper.deleteByUk(userId, KycExceptionTaskType.JUMIO_INIT_FACE.getCode());

			JSONObject executeParam = new JSONObject();
			executeParam.put("faceTransType", faceTransType.getCode());
			executeParam.put("bizId", bizId);

			task = new KycExceptionTask();
			task.setUserId(userId);
			task.setTaskType(KycExceptionTaskType.JUMIO_INIT_FACE.getCode());
			task.setExecuteStatus("INIT");
			task.setExecuteParam(executeParam.toJSONString());
			task.setCreateTime(DateUtils.getNewUTCDate());
			task.setUpdateTime(DateUtils.getNewUTCDate());
			task.setExecuteTime(
					DateUtils.addSeconds(task.getCreateTime(), KycExceptionTaskType.JUMIO_INIT_FACE.getDelay()));

			kycExceptionTaskMapper.insert(task);
		} catch (Exception e) {
			log.warn("添加jumio结果通知初始化face流程异常. userId:{}", userId, e);
		}

	}

	@Override
	public void executeTask(KycExceptionTask task) {
		KycExceptionTaskType type = KycExceptionTaskType.valueOf(task.getTaskType());
		try {
			switch (type) {
			case JUMIO_INIT_FACE:
				log.info("jumio结果通知初始化face流程异常进行重试 userId:{} task:{}", task.getUserId(), task);
				JSONObject executeParam = JSONObject.parseObject(task.getExecuteParam());
				initFaceFlow(task.getUserId(), executeParam.getString("faceTransType"),
						executeParam.getString("bizId"));
				break;

			default:
				break;
			}
			task.setExecuteStatus("SUCCESS");
			task.setUpdateTime(DateUtils.getNewUTCDate());
			task.setTaskMemo("FINISH");
			kycExceptionTaskMapper.updateByPrimaryKeySelective(task);
		} catch (Exception e) {
			long createTime = task.getCreateTime().getTime();
			long currentTime = DateUtils.getNewUTCDate().getTime();
			//2小时内重试不通过则直接fail
			if((currentTime - createTime )>(1000 * 60 * 60 * 2)) {
				task.setExecuteStatus("FAIL");
				task.setUpdateTime(DateUtils.getNewUTCDate());
				kycExceptionTaskMapper.updateByPrimaryKeySelective(task);
			}else {
				task.setUpdateTime(DateUtils.getNewUTCDate());
				task.setExecuteTime(DateUtils.addSeconds(task.getUpdateTime(), type.getDelay()));
				task.setTaskMemo(e.getMessage());
				kycExceptionTaskMapper.updateByPrimaryKeySelective(task);
			}
			
		}

	}

	private void initFaceFlow(Long userId, String faceTransTypeCode, String bizId) {
		FaceTransType faceTransType = FaceTransType.getByCode(faceTransTypeCode);
		TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(bizId, faceTransType.name());
		if (faceLog == null) {
			iUserFace.initFaceFlowByTransId(bizId, userId, faceTransType, true, true);
		}
	}

	@Override
	public List<KycExceptionTask> selectPage(Date startTime, Date endTime, int start, int rows,String status) {
		return kycExceptionTaskMapper.selectPage(startTime, endTime, status, start, rows);
	}

}
