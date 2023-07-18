package com.binance.account.mq;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.kyc.CertificateCenterDispatcher;
import com.binance.account.service.kyc.CertificateCenterDispatcherParam;
import com.binance.account.service.kyc.KycFlowProcessFactory;
import com.binance.account.service.kyc.KycFlowProcessor;
import com.binance.account.service.reset2fa.IReset2Fa;
import com.binance.account.vo.kyc.request.JumioAuthRequest;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.constant.MQConstant;
import com.binance.messaging.common.utils.UUIDUtils;
import com.binance.platform.common.TrackingUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统一通过MQ消息监听JUMIO的信息后进行处理
 */
@Log4j2
@Component
public class JumioInfoMsgListener {

	@Resource
	private KycJumioInfoMsgExecutor kycJumioInfoMsgExecutor;
	@Resource
	private ResetJumioInfoMsgExecutor resetJumioInfoMsgExecutor;
	@Resource
	private KycFlowProcessFactory kycFlowProcessFactory;
	@Resource
	private JumioBusiness jumioBusiness;
	@Resource
	private IReset2Fa iReset2Fa;

	@Resource
	private KycCertificateMapper kycCertificateMapper;

	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	
	@Resource
	private CertificateCenterDispatcher certificateCenterDispatcher;

	@RabbitListener(queues = MQConstant.JUMIO_RESET_KYC_QUEUE)
	public void onMessage(Message message, Channel channel) {
		String trackId = StringUtils.isBlank(TrackingUtils.getTrace()) ? "MQ_JUMIO." + TrackingUtils.generateUUID() : TrackingUtils.getTrace();
		TrackingUtils.saveTrace(trackId);
		String body = null;
		final long deliveryTag = message.getMessageProperties().getDeliveryTag();
		try {
			body = new String(message.getBody(), "UTF-8");
			log.info("JUMIO MSG => 收到JUMIO变更消息.");
			if (StringUtils.isBlank(body)) {
				log.warn("JUMIO MSG => JUMIO变更消息错误.");
				return;
			}
			JumioInfoVo jumioInfoVo = JSON.parseObject(body, JumioInfoVo.class);
			if (jumioInfoVo == null || jumioInfoVo.getUserId() == null || StringUtils.isBlank(jumioInfoVo.getBizId())
					|| jumioInfoVo.getHandlerType() == null) {
				log.info("JUMIO MSG => JUMIO 消息解析后数据缺失.");
				return;
			}
			execute(jumioInfoVo);
		} catch (Exception e) {
			log.error("JUMIO MSG => 处理JUMIO结果信息异常. body:{}", body, e);
		} finally {
			try {
				channel.basicAck(deliveryTag, false);
			} catch (Exception e) {
				log.error("JUMIO MSG => 消息ACK失败. ", e);
			}
			TrackingUtils.clearTrace();
		}
	}

	/**
	 * 处理消息
	 *
	 * @param jumioInfoVo
	 */
	public String execute(JumioInfoVo jumioInfoVo) {
		Long userId = jumioInfoVo.getUserId();
		String bizId = jumioInfoVo.getBizId();
		if (jumioInfoVo.isLockOne()) {
			return lockOneNotify(jumioInfoVo, userId, bizId);
		}

		return unLockOneNotify(jumioInfoVo, userId, bizId);
	}

	private String lockOneNotify(JumioInfoVo jumioInfoVo, Long userId, String bizId) {
		log.info("JUMIO MSG => KYC用户绑定模式. userId:{}", jumioInfoVo.getUserId());
		try {
			JumioInfoVo lastJumioInfo = jumioBusiness.getLastByUserId(jumioInfoVo.getUserId());
			if (lastJumioInfo != null && !lastJumioInfo.getId().equals(jumioInfoVo.getId())) {
				log.info("JUMIO MSG =>当前jumio通知非在最后一笔通知. userId:{},jumioId:{},lastJumioId:{}", userId,
						jumioInfoVo.getId(), lastJumioInfo.getId());
				return null;
			}
		} catch (Exception e) {
			log.warn("JUMIO MSG => 获取最后一条jumio信息异常. userId:{}", userId, e);
		}

		try {
			
			CertificateCenterDispatcherParam<Void> param = certificateCenterDispatcher.jumioAuditResult(jumioInfoVo,jumioInfoVo.getBizId());
			if(param.isDispatcher()) {
				syncResult2Reset(jumioInfoVo, userId, null);
				return null;
			}
			
			// 新版本按用户处理的逻辑
			JumioAuthRequest jumioAuthRequest = new JumioAuthRequest();
			jumioAuthRequest.setJumioStatus(jumioInfoVo.getStatus().name());
			jumioAuthRequest.setMessage(jumioInfoVo.getFailReason());
			jumioAuthRequest.setUserId(jumioInfoVo.getUserId());
			jumioAuthRequest.setBizId(jumioInfoVo.getBizId());
			jumioAuthRequest.setJumioInfoVo(JSON.toJSON(jumioInfoVo));
			
			
			kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_JUMIO_AUTH_RESULT).process(jumioAuthRequest);
		} catch (Exception e) {
			log.warn("JUMIO MSG => 按用户认证处理逻辑处理异常. userId:{} bizId:{}", userId, bizId, e);
		}

		syncResult2Reset(jumioInfoVo, userId, null);
		return null;
	}

	private String unLockOneNotify(JumioInfoVo jumioInfoVo, Long userId, String bizId) {
		JumioHandlerType handlerType = jumioInfoVo.getHandlerType();
		String result = null;
		try {
			// 老版本按流程处理的逻辑
			switch (handlerType) {
			case USER_KYC:
			case COMPANY_KYC:
				KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
				if(kycCertificate == null) {
					// kyc 逻辑
					result = kycJumioInfoMsgExecutor.executor(userId, bizId, handlerType, jumioInfoVo);
				}else {
					jumioInfoVo.setLockOne(true);
					lockOneNotify(jumioInfoVo, userId, bizId);
				}

				syncResult2Reset(jumioInfoVo, userId, null);
				break;
			case RESET_ENABLE:
			case RESET_GOOGLE:
			case RESET_MOBILE:
				// reset
				result = resetJumioInfoMsgExecutor.executor(userId, bizId, handlerType, jumioInfoVo);
				syncResult2Reset(jumioInfoVo, userId, bizId);
				syncResult2Kyc(jumioInfoVo, userId);
				break;
			default:
				log.warn("JUMIO MSG => 当前类型不处理.");
				result = "当前类型不处理";
			}
		} catch (Exception e) {
			log.warn("JUMIO MSG => 按流程业务逻辑处理异常. userId:{} bizId:{} ", userId, bizId, e);
			result = e.getMessage();
		}
		return result;
	}

	private void syncResult2Reset(JumioInfoVo jumioInfoVo, Long userId, String resetId) {
		List<UserSecurityReset> resets = iReset2Fa.findJumioPendingResets(userId);
		if (resets == null || resets.isEmpty()) {
			log.info("jumio异步通知kyc结果,开始同步处理reset记录.当前无reset流程 userId:{}", userId);
			return;
		}
		log.info("jumio异步通知kyc结果,开始同步处理reset记录.当前reset流程 userId:{} , count:{}", userId, resets.size());
		final String track = StringUtils.isNotBlank(TrackingUtils.getTrace()) ? TrackingUtils.getTrace()
				: TrackingUtils.generateUUID();

		for (UserSecurityReset reset : resets) {
			if (StringUtils.isNotBlank(resetId) && reset.getId().equals(resetId)) {
				continue;
			}
			AsyncTaskExecutor.execute(() -> {
				try {
					TrackingUtils.saveTrace(track);
					log.info("jumio异步通知kyc结果,开始同步处理reset记录 userId:{} resetId:{} type:{}", userId, reset.getId(),
							jumioInfoVo.getHandlerType());
					String result = resetJumioInfoMsgExecutor.executorWithLock(reset, jumioInfoVo);
					log.info("jumio异步通知kyc结果,同步reset处理成功 userId:{} resetId:{} type:{} result:{}", userId, reset.getId(),
							jumioInfoVo.getHandlerType(), result);
				} catch (Exception e) {
					log.error("jumio异步通知kyc结果，处理reset异常. userId:{}", userId, e);
				} finally {
					TrackingUtils.clearTrace();
				}
			});

		}
	}

	private void syncResult2Kyc(JumioInfoVo jumioInfoVo, Long userId) {
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		if (kycCertificate == null) {
			return;
		}
		final String track = StringUtils.isNotBlank(TrackingUtils.getTrace()) ? TrackingUtils.getTrace()
				: TrackingUtils.generateUUID();
		AsyncTaskExecutor.execute(() -> {
			TrackingUtils.saveTrace(track);
			log.info("JUMIO MSG => KYC用户绑定模式. userId:{}", jumioInfoVo.getUserId());
			try {
				JumioInfoVo lastJumioInfo = jumioBusiness.getLastByUserId(jumioInfoVo.getUserId());
				if (lastJumioInfo != null && !lastJumioInfo.getId().equals(jumioInfoVo.getId())) {
					log.info("JUMIO MSG =>当前jumio通知非在最后一笔通知. userId:{},jumioId:{},lastJumioId:{}", userId,
							jumioInfoVo.getId(), lastJumioInfo.getId());
					return;
				}
			} catch (Exception e) {
				log.warn("JUMIO MSG => 获取最后一条jumio信息异常. userId:{}", userId, e);
			}

			try {
				// 获取kyc的face流程记录，如果存在则证明已经初始化过，直接拿transId
				// 作为bizId适用，如果没有记录则证明face流程未初始化，创建一个uuid作为新face流程
				KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
				FaceTransType faceTransType = kycType == KycCertificateKycType.USER ? FaceTransType.KYC_USER
						: FaceTransType.KYC_COMPANY;
				TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(),
						null);
				String bizId = faceLog == null ? UUIDUtils.getId() : faceLog.getTransId();

				CertificateCenterDispatcherParam<Void> param = certificateCenterDispatcher.jumioAuditResult(jumioInfoVo,bizId);
				if(param.isDispatcher()) {
					return;
				}
				
				
				// 新版本按用户处理的逻辑
				JumioAuthRequest jumioAuthRequest = new JumioAuthRequest();
				jumioAuthRequest.setJumioStatus(jumioInfoVo.getStatus().name());
				jumioAuthRequest.setMessage(jumioInfoVo.getFailReason());
				jumioAuthRequest.setUserId(jumioInfoVo.getUserId());
				jumioAuthRequest.setBizId(bizId);
				jumioAuthRequest.setJumioInfoVo(JSON.toJSON(jumioInfoVo));
				kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_JUMIO_AUTH_RESULT)
						.process(jumioAuthRequest);
			} catch (Exception e) {
				log.warn("JUMIO MSG => 按用户认证处理逻辑处理异常. userId:{} ", userId, e);
			} finally {
				TrackingUtils.clearTrace();
			}
		});
	}
}
