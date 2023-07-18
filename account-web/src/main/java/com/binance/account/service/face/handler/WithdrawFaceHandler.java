package com.binance.account.service.face.handler;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.face.FaceHandlerHelper;
import com.binance.account.service.face.FaceHandlerType;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.response.FacePcResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.security.enums.SecurityFaceStatusSource;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.vo.faceid.FaceLogVo;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.risk.api.RiskUserFaceApi;
import com.binance.risk.api.RiskWithdrawApi;
import com.binance.risk.api.RiskWithdrawDecisionEngineApi;
import com.binance.risk.vo.withdraw.decision.request.RiskWithdrawUserFaceRequest;
import com.binance.risk.vo.withdraw.request.RiskWithdrawBlackListDeleteRequest;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author liliang1
 * @date 2019-02-28 14:03
 */
@Log4j2
@Component
@FaceHandlerType(values = { FaceTransType.WITHDRAW_FACE })
public class WithdrawFaceHandler extends AbstractFaceHandler {

	@Resource
	private UserSecurityMapper userSecurityMapper;
	@Resource
	private RiskUserFaceApi riskUserFaceApi;
	@Resource
	private RiskWithdrawApi riskWithdrawApi;
	@Resource
	private RiskWithdrawDecisionEngineApi riskWithdrawDecisionEngineApi;
	@Resource
	private ICountry iCountry;
	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private CompanyCertificateMapper companyCertificateMapper;
	@Resource
	private JumioBusiness jumioBusiness;
	@Resource
	private IUserCertificate iUserCertificate;
	@Resource
	private FaceHandlerHelper faceHandlerHelper;

	@Override
	public FaceFlowInitResult initTransFace(String transId, Long userId, FaceTransType transType, boolean needEmail,
			boolean isKycLockOne) {
		// 当前方法先不做逻辑处理，主要通过 initWithdrawFaceByUserId(userId) 方法实现
		throw new BusinessException(GeneralCode.SYS_ERROR);
	}

	public void initWithdrawFaceByUserId(Long userId, String withdrawId) throws Exception {
		initWithdrawFaceByUserId(userId, withdrawId, null);
	}

	/**
	 * 根据 用户ID, 发起一次用户提币人脸识别的流程
	 *
	 * @param userId
	 * @param withdrawId 提现业务标识
	 */
	public void initWithdrawFaceByUserId(Long userId, String withdrawId, SecurityFaceStatusSource source)
			throws Exception {
		Lock lock = RedisCacheUtils.getLock(String.format(AccountConstants.WITHDRAW_FACE_USER_LOCK, userId));
		if (lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
			try {
				User user = getUserByUserId(userId);
				// 查询最后一笔记录是否正在处理中，如果是，不再新加
				TransactionFaceLog transactionFaceLog = generateTransFaceLog(userId, withdrawId,
						FaceTransType.WITHDRAW_FACE, source);
				log.info("开始验证触发用户提币人脸识别流程. userId:{}", userId);
				withdrawFlowHandler(userId, user, transactionFaceLog, !SecurityFaceStatusSource.C2C.equals(source));
			} catch (Exception e) {
				log.error("触发提币人脸识别流程异常. userId:{}", userId, e);
				throw e;
			} finally {
				lock.unlock();
			}
		} else {
			log.info("提币人脸识别流程触发加锁失败. userId:{}", userId);
		}
	}

	/**
	 * 生成一次提币人脸识别的流程
	 *
	 * @param faceTransType
	 * @param userId
	 * @param withdrawId
	 * @return
	 */
	private TransactionFaceLog generateTransFaceLog(Long userId, String withdrawId, FaceTransType faceTransType,
			SecurityFaceStatusSource source) {
		if (userId == null || faceTransType != FaceTransType.WITHDRAW_FACE) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		// 查询最后一笔记录是否正在处理中，如果是，不再新加
		TransactionFaceLog oldFaceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
		if (oldFaceLog != null && !TransFaceLogStatus.isEndStatus(oldFaceLog.getStatus())) {
			if (TransFaceLogStatus.REVIEW == oldFaceLog.getStatus()) {
				log.info("用户的提币人脸识别正在进行中，不能再次创建人脸识别流程. userId:{} transId:{}", userId, oldFaceLog.getTransId());
				throw new BusinessException(AccountErrorCode.WITHDRAW_FACE_REVICE_CANNOT_GENERATE);
			} else {
				log.info("用户的提币人脸识别业务正在进行中，不需要再建立一笔业务, userId:{} transId:{}", userId, oldFaceLog.getTransId());
				return oldFaceLog;
			}
		}
		String transId = UUID.randomUUID().toString().replace("-", "") + "_" + RandomStringUtils.randomNumeric(4);
		transId = source == null ? transId : source.name() + ":" + transId;
		TransactionFaceLog transactionFaceLog = new TransactionFaceLog();
		transactionFaceLog.setUserId(userId);
		transactionFaceLog.setTransId(transId);
		transactionFaceLog.setTransType(faceTransType.name());
		transactionFaceLog.setStatus(TransFaceLogStatus.INIT);
		transactionFaceLog.setCreateTime(DateUtils.getNewUTCDate());
		transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
		transactionFaceLog.setWithdrawId(withdrawId);
		transactionFaceLog.setKycLockOne(false);
		transactionFaceLogMapper.insert(transactionFaceLog);
		if (transactionFaceLog.getId() == null || transactionFaceLog.getId() <= 0) {
			log.warn("初始化提币人脸识别流程失败. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		return transactionFaceLog;
	}

	/**
	 * 创建提币人脸识别后需要的验证和通知信息
	 *
	 * @param userId
	 * @param user
	 * @param transactionFaceLog
	 */
	private void withdrawFlowHandler(Long userId, User user, TransactionFaceLog transactionFaceLog,
			boolean withdrawEmail) {
		/*
		 * 先检查先当前用户是否已经满足做人脸识别的条件 1. 先验证是否已经身份认证成功，如果没有通过身份验证，需要先通过身份验证 2.
		 * 如果身份验证正在进行中，直接把当前正在进行中的KYC认证拒绝 3. 检查对照表中是否有对照图片，如果有了，则认为可以做人脸识别了
		 * 如果没有，需要使用通过身份认证的手持照验证是否能做人脸识别，如果能做则返回成功， 如果不能做，需要把原来的身份验证拒绝调让用户重做身份认证
		 */
		String transId = transactionFaceLog.getTransId();
		KycCertificateResult certificateResult = userCommonBusiness.getKycStatues(user);
		log.info("1. 验证用户KYC是否通过信息. userId:{}, certificate:{}", userId, JSON.toJSONString(certificateResult));
		Integer certificateStatus = certificateResult.getCertificateStatus();
		Integer certificateType = certificateResult.getCertificateType();
		Long certificateId = certificateResult.getCertificateId();
		boolean isNewVersion = certificateResult.isNewVersion();
		// 先直接设置对应的认证类型和认证ID
		transactionFaceLog.setCertificateType(certificateType);
		transactionFaceLog.setCertificateId(certificateId);
		if (!faceHandlerHelper.canSkipKycUpload(certificateResult)) {
			log.info("用户没有通过KYC(个人认证或企业认证), 需要用户先进行KYC认证. userId:{} transId:{}", userId, transId);
			updateWithdrawFaceLogStatus(transactionFaceLog, TransFaceLogStatus.FAIL, "未通过个人认证/企业认证");
			if (certificateStatus != null && certificateStatus == KycCertificateResult.STATUS_REVIEW) {
				// 拒绝用户的本次KYC认证，让用户重新做KYC
				log.info("当前用户正在进行KYC认证，直接把当前进行的流程拒绝. userId:{} transId:{}", userId, transId);
				refusedCurrentApplyKyc(userId, transId, certificateType, certificateId);
			} else {
				// 发送通知邮件让用户做身份认证
				sendDoKycNotifyEmail(user, userId, transId);
				// 发送短信通知用户做身份认证
				sendWithdrawFaceSms(userId, transId, AccountConstants.SMS_WITHDRAW_FACE_KYC_NOTIFY);
			}
			return;
		}
		log.info("2. 用户的KYC已经认证通过. 下一步验证是否已经存在有对比照片. userId:{} transId:{}", userId, transId);
		UserFaceReference faceReference = iFace.getUserFaceByMasterBD(userId);
		if (faceReference != null && StringUtils.isNotBlank(faceReference.getRefImage())) {
			log.info("用户已经存在有对比照片, 不再需要检验照片是否能做人脸, 直接发送邮件通知用户做风控人脸识别. userId:{}, transId:{}", userId, transId);
			LanguageEnum language = emailNotifyLanguage(userId);
			if (!withdrawEmail) {
				// c2c 类型的来源不需要发送邮件
				log.info("当前不需要发送邮件信息，直接改状态后退出. userId:{} transId:{}", userId, transId);
				transactionFaceLog.setStatus(TransFaceLogStatus.PENDING);
				transactionFaceLog.setFailReason("跳过邮件通知");
				transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
				transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
				return;
			}
			String link = emailNotifyLink(transId, FaceTransType.WITHDRAW_FACE, language);
			sendFaceNotifyEmail(userId, transId, transactionFaceLog, FaceTransType.WITHDRAW_FACE, language, link);
			return;
		}
		// 不存在人脸对比照片信息时
		validateImageAndNotify(userId, transId, transactionFaceLog, certificateResult, withdrawEmail);
	}

	/**
	 * 当不存在
	 *
	 * @param userId
	 * @param transId
	 * @param transactionFaceLog
	 */
	private void validateImageAndNotify(Long userId, String transId, TransactionFaceLog transactionFaceLog,
			KycCertificateResult certificateResult, boolean withdrawEmail) {
		log.info("3. 需要检验KYC通过的照片是否能做人脸识别. userId:{} transId:{} ", userId, transId);
		// 如果能获取到的时候，去校验这个图片是否能用为做人脸识别
		int count = apolloCommonConfig.getWithdrawFaceImageValidLimit() == null ? 3
				: apolloCommonConfig.getWithdrawFaceImageValidLimit();
		boolean imageCheckResult = faceHandlerHelper.checkCurrentKycCanDoFace(userId, certificateResult, transId, count);
		if (imageCheckResult) {
			log.info("人脸图片验证成功，发送人脸识别通知. userId:{} transId:{}", userId, transId);
			LanguageEnum language = emailNotifyLanguage(userId);
			if (withdrawEmail) {
				String link = emailNotifyLink(transId, FaceTransType.WITHDRAW_FACE, language);
				sendFaceNotifyEmail(userId, transId, transactionFaceLog, FaceTransType.WITHDRAW_FACE, language,
						link);
			}
		}else {
			log.info("获取用户通过KYC认证的人脸照片路径失败, 需要用户重新做KYC认证. userId:{} transId:{}", userId, transId);
			// 获取不到的时候把老的验证拒绝掉，让用户重做KYC, 目前先不自动拒绝
			updateWithdrawFaceLogStatus(transactionFaceLog, TransFaceLogStatus.FAIL, "获取用户KYC认证照片失败，需要用户重做KYC");
			if (apolloCommonConfig.isAutoRefusedKyc()) {
				log.info("获取KYC认证照片做照片检测时获取失败，自动拒绝KYC认证开关开启, 进行自动拒绝用户KYC认证. userId:{} transId:{}", userId, transId);
				refusedKycByWithdrawSecurityFace(userId, transId, certificateResult);
			}
		}
	}

	private void refusedKycByWithdrawSecurityFace(Long userId, String transId, KycCertificateResult certificateResult) {
		String refusedResult = faceHandlerHelper.refusedKycByWithdrawSecurityFace(userId, transId, certificateResult, UserConst.WITHDRAW_FACE_REFUSED_KYC);
		if (StringUtils.isBlank(refusedResult)) {
			// 发送KYC拒绝的短信通知
			sendWithdrawFaceSms(userId, transId, AccountConstants.SMS_WITHDRAW_FACE_KYC_REFUSED);
		}
	}

	/**
	 * 把用户当前正在进行的KYC认证拒绝掉，让用户重新做KYC认证
	 *
	 * @param userId
	 * @param transId
	 * @param certificateType
	 * @param certificateId
	 * @return
	 */
	private void refusedCurrentApplyKyc(Long userId, String transId, Integer certificateType, Long certificateId) {
		if (certificateType == null || certificateId == null) {
			return;
		}
		log.info("用户当前KYC认证正在进行中，需要把当前进行的KYC进行拒绝. userId:{} transId:{} certificateType:{} certificateId:{}", userId,
				transId, certificateType, certificateId);
		switch (certificateType) {
		case KycCertificateResult.TYPE_COMPANY:
			CompanyCertificate certificate = iUserCertificate.getCompanyKycFromMasterDbById(userId, certificateId);
			if (certificate != null && !CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
				log.info("提币风控人脸识别触发拒绝当前企业认证流程. userId:{} transId:{} certificateId:{}", userId, transId, certificateId);
				certificate.setStatus(CompanyCertificateStatus.refused);
				certificate.setInfo(AccountConstants.WITHDRAW_FACE_KYC_APPLY_REFUSED);
				certificate.setUpdateTime(DateUtils.getNewUTCDate());
				companyCertificateMapper.updateByPrimaryKeySelective(certificate);
				log.info("企业认证人脸识别审核拒绝时，直接把整个认证流程拒绝并发送通知邮件: userId:{} transId:{}", userId, certificateId);
				userCommonBusiness.sendJumioCheckEmail(userId, null, AccountConstants.WITHDRAW_FACE_KYC_APPLY_REFUSED,
						Constant.JUMIO_COMPANY_CHECK_FAIL, "发送企业认证邮件");
				// 同步业务状态到INSPECTOR 的JUMIO 数据
				jumioBusiness.syncJumioBizStatus(userId, certificate.getScanReference(), JumioBizStatus.REFUSED);
				// 同步结束人脸识别的流程
				endKycTransFaceFlow(userId, certificate.getId().toString(), FaceTransType.KYC_COMPANY,
						TransFaceLogStatus.FAIL, "提币风控触发终止KYC认证流程");
			}
			break;
		case KycCertificateResult.TYPE_USER:
			log.info("个人识别人脸识别人工审核拒绝, 直接把个人认证流程拒绝. userId:{} transId:{} certificateId:{}", userId, transId,
					certificateId);
			UserKyc userKyc = iUserCertificate.getUserKycFromMasterDbById(userId, certificateId);
			if (userKyc != null && !KycStatus.isEndStatus(userKyc.getStatus())) {
				userKyc.setStatus(KycStatus.refused);
				userKyc.setFailReason(AccountConstants.WITHDRAW_FACE_KYC_APPLY_REFUSED);
				userKyc.setUpdateTime(DateUtils.getNewUTCDate());
				userKycMapper.updateStatus(userKyc);
				log.info("人脸识别人工审核拒绝后终止个人认证流程，发送通知邮件. userId:{} transId:{} certificateId:{}", userId, transId,
						certificateId);
				userCommonBusiness.sendJumioCheckEmail(userId, null, AccountConstants.WITHDRAW_FACE_KYC_APPLY_REFUSED,
						Constant.JUMIO_KYC_CHECK_FAIL, "发送KYC认证邮件");
				// 同步业务状态到 INSPECTOR JUMIO
				jumioBusiness.syncJumioBizStatus(userId, userKyc.getScanReference(), JumioBizStatus.REFUSED);
				//
				endKycTransFaceFlow(userId, userKyc.getId().toString(), FaceTransType.KYC_USER, TransFaceLogStatus.FAIL,
						"提币风控触发终止KYC认证流程");
			}
			break;
		default:
			log.warn("身份认证类型错误. userId:{} transId:{}", userId, transId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	/**
	 * 终止KYC的人脸识别认证流程
	 *
	 * @param userId
	 * @param kycTransId
	 * @param faceTransType
	 * @param status
	 * @param failReason
	 */
	private void endKycTransFaceFlow(Long userId, String kycTransId, FaceTransType faceTransType,
			TransFaceLogStatus status, String failReason) {
		TransactionFaceLog faceLog = getByMasterdb(kycTransId, faceTransType);
		if (faceLog != null && !TransFaceLogStatus.isEndStatus(faceLog.getStatus())) {
			log.info("强制修改KYC认证人脸识别业务流程的状态：userId:{} transId:{} type:{} status:{}", userId, kycTransId, faceTransType,
					status);
			TransactionFaceLog transactionFaceLog = new TransactionFaceLog();
			transactionFaceLog.setId(faceLog.getId());
			transactionFaceLog.setUserId(faceLog.getUserId());
			transactionFaceLog.setStatus(status);
			transactionFaceLog.setFailReason(failReason);
			transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
			transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
		}
	}

	/**
	 * 由于没有做过KYC进行发送做KYC的通知邮件
	 *
	 * @param user
	 * @param userId
	 * @param transId
	 */
	private void sendDoKycNotifyEmail(User user, Long userId, String transId) {
		try {
			LanguageEnum languageEnum = emailNotifyLanguage(userId);
			log.info("发送提币人脸识别通知用户做KYC认证的邮件通知. userId:{} transId:{} language:{}", userId, transId, languageEnum);
			userCommonBusiness.sendEmailWithoutRequest(AccountConstants.EMAIL_WITHDRAW_FACE_KYC_NOTIFY, user, null,
					"提醒用户做KYC认证通知", languageEnum);
		} catch (Exception e) {
			log.error("发送提币人脸识别通知用户做KYC认证的邮件通知异常, userId:{} transId:{}", userId, transId, e);
		}
	}

	/**
	 * 通知提币风控的人脸识别邮件没有连接
	 *
	 * @param transId
	 * @param faceTransType
	 * @param language
	 * @return
	 */
	@Override
	protected String emailNotifyLink(String transId, FaceTransType faceTransType, LanguageEnum language) {
		// 提币风控人脸识别通知邮件没有连接
		return null;
	}

	/**
	 * 发送提币人脸识别的短信信息
	 *
	 * @param userId
	 * @param transId
	 * @param smsTemplate
	 */
	private void sendWithdrawFaceSms(Long userId, String transId, String smsTemplate) {
		try {
			UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
			if (userSecurity == null) {
				throw new BusinessException(GeneralCode.SYS_ERROR, "获取用户安全信息失败. userId:{}");
			}
			String mobile = userSecurity.getMobile();
			String countryCode = userSecurity.getMobileCode();
			if (StringUtils.isAnyBlank(mobile, countryCode)) {
				log.info("用户没有绑定手机号码或者手机号码的国家码不存在不发送短信: userId:{} transId:{}", userId, transId);
				return;
			}
			Country country = iCountry.getCountryByCode(countryCode);
			String mobileCode = country != null ? country.getMobileCode() : null;
			if (StringUtils.isBlank(mobileCode)) {
				log.info("国家短信码获取失败，不需要发送短信通知: userId:{} transId:{}", userId, transId);
				return;
			}
			LanguageEnum languageEnum = LanguageEnum.EN_US;
			if (StringUtils.equalsIgnoreCase("cn", userSecurity.getMobileCode())) {
				languageEnum = LanguageEnum.ZH_CN;
			}
			SendMsgRequest msgRequest = new SendMsgRequest();
			msgRequest.setTplCode(smsTemplate);
			msgRequest.setUserId(String.valueOf(userId));
			msgRequest.setMobileCode(mobileCode);
			msgRequest.setRecipient(mobile);
			msgRequest.setNeedIpCheck(false);
			msgRequest.setNeedSendTimesCheck(false);
			Map<String, Object> params = Maps.newHashMap();
			params.put(Constant.MESSAGE_TEMPLATE_PROP_VERIFYCODE, "123456");
			msgRequest.setData(params);
			log.info("发送提币人脸识别的短信信息: userId:{} transId:{} smsTemplate:{} language:{}", userId, transId, smsTemplate,
					languageEnum);
			userCommonBusiness.sendMsg(msgRequest, languageEnum, TerminalEnum.OTHER);
		} catch (Exception e) {
			log.error("提币人脸短信信息发送出现异常: userId:{} transId:{} smsTemplate:{}", userId, transId, smsTemplate, e);
		}
	}

	/**
	 * 表更提币人脸识别的状态
	 *
	 * @param transactionFaceLog
	 * @param status
	 * @param failReason
	 */
	private void updateWithdrawFaceLogStatus(TransactionFaceLog transactionFaceLog, TransFaceLogStatus status,
			String failReason) {
		transactionFaceLog.setStatus(status);
		transactionFaceLog.setFailReason(failReason);
		transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
		transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
	}

	@Override
	protected String emailNotifyTemplate() {
		return AccountConstants.USER_WITHDRAW_FACE_EMAIL_TEMPLATE;
	}

	/**
	 * 重写父类中获取对比照的方法，只取正式通过的人脸对比照信息
	 *
	 * @param userId
	 * @return
	 */
	@Override
	protected String getFaceCheckImage(Long userId) {
		UserFaceReference faceReference = userFaceReferenceMapper.selectByPrimaryKey(userId);
		if (faceReference == null || StringUtils.isBlank(faceReference.getRefImage())) {
			log.info("获取对比照片信息失败. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.FACE_VERIFICATION_MISS_REF_IMAGE);
		}
		return faceReference.getRefImage();
	}

	@Override
	public FacePcResponse facePcResultHandler(String transId, FaceTransType transType,
			FaceWebResultResponse faceWebResult) {
		FacePcResponse facePcResponse = super.facePcResultHandler(transId, transType, faceWebResult);
		TransFaceLogStatus status = facePcResponse.getStatus();
		if (status == TransFaceLogStatus.REVIEW) {
			log.info("提币风控人脸识别进入了人工审核状态.userId:{} transId:{}", facePcResponse.getUserId(), transId);
			return facePcResponse;
		}
		// 根据结果是否人脸识别通过，进行一些后续逻辑处理, 注意：后续处理逻辑最好处理时间要短，否则会出现用户一直等待结果的情况
		if (apolloCommonConfig.isWithdrawFaceAutoPass() && facePcResponse.isSuccess()
				&& status == TransFaceLogStatus.PASSED) {
			final Long userId = facePcResponse.getUserId();
			log.info("PC端提现风控人脸识别通过后自动变更提现风控人脸识别的标识. userId:{} transId:{}", userId, transId);
			this.changeWithdrawFaceStatus(userId, UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO, null, null, true);
			// 异步删除用户风控黑名单
			deleteRiskBlackListByUserId(userId, transId,transId,null,facePcResponse.getFaceBizNo(),null,null);
		}
		if (!TransFaceLogStatus.REVIEW.equals(status) && apolloCommonConfig.getWithdrawFaceDeleteBlackListSwitch() != 3) {
			// 异步把人脸识别信息推送给风控（业务流程进入审核状态的话不推送消息）
			pullRiskByAuto(transId, facePcResponse.getUserId(), facePcResponse);
		}
		return facePcResponse;
	}

	 @Override
	    public TransactionFaceLog facePcPrivateResultHandler(FaceTransType transType, FacePcPrivateResult result) {
	    	TransactionFaceLog transLog = super.facePcPrivateResultHandler(transType, result);
	    	if(transLog == null) {
	    		return null;
	    	}
	    	TransFaceLogStatus status = transLog.getStatus();
	    	String transId = transLog.getTransId();
			if (status == TransFaceLogStatus.REVIEW) {
				log.info("提币风控人脸识别进入了人工审核状态.userId:{} transId:{}", transLog.getUserId(), transId);
				return transLog;
			}
			// 根据结果是否人脸识别通过，进行一些后续逻辑处理, 注意：后续处理逻辑最好处理时间要短，否则会出现用户一直等待结果的情况
			if (apolloCommonConfig.isWithdrawFaceAutoPass() && result.isSuccess()
					&& status == TransFaceLogStatus.PASSED) {
				final Long userId = transLog.getUserId();
				log.info("PC端提现风控人脸识别通过后自动变更提现风控人脸识别的标识. userId:{} transId:{}", userId, transId);
				this.changeWithdrawFaceStatus(userId, UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO, null, null, true);
				// 异步删除用户风控黑名单
				deleteRiskBlackListByUserId(userId, transId,transId,null,result.getBizNo(),null,null);
			}
			if (!TransFaceLogStatus.REVIEW.equals(status) && apolloCommonConfig.getWithdrawFaceDeleteBlackListSwitch() != 3) {
				// 异步把人脸识别信息推送给风控（业务流程进入审核状态的话不推送消息）
				pullRiskByAuto(transId, transLog.getUserId(), result);
			}
			return transLog;
	    }

	@Override
	public FaceSdkResponse faceSdkResultHandler(String transId, Long userId, FaceTransType faceTransType,
			FaceSdkVerifyRequest request) {
		FaceSdkResponse faceSdkResponse = super.faceSdkResultHandler(transId, userId, faceTransType, request);
		TransFaceLogStatus status = faceSdkResponse.getStatus();
		if (status == TransFaceLogStatus.REVIEW) {
			log.info("提币风控人脸识别进入了人工审核状态.userId:{} transId:{}", faceSdkResponse.getUserId(), transId);
			return faceSdkResponse;
		}
		// 根据结果是否人脸识别通过，进行一些后续逻辑处理, 注意：后续处理逻辑最好处理时间要短，否则会出现用户一直等待结果的情况
		if (apolloCommonConfig.isWithdrawFaceAutoPass() && faceSdkResponse.isSuccess()
				&& status == TransFaceLogStatus.PASSED) {
			log.info("SDK端提现风控人脸识别通过后自动变更提现风控人脸识别的标识. userId:{} transId:{}", userId, transId);
			this.changeWithdrawFaceStatus(userId, UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO, null, null, true);
			// 异步删除用户风控黑名单
			deleteRiskBlackListByUserId(userId, transId, transId, null,faceSdkResponse.getFaceBizNo(),null,null);
		}
		if (!TransFaceLogStatus.REVIEW.equals(status) && apolloCommonConfig.getWithdrawFaceDeleteBlackListSwitch() != 3) {
			// 异步把人脸识别信息推送给风控（业务流程进入审核状态的话不推送消息）
			pullRiskByAuto(transId, userId, faceSdkResponse);
		}
		return faceSdkResponse;
	}
	/**
	 * 人脸识别通过后，如果已经关闭了提币风控人脸识别标识，同时也调用风控删除黑名单
	 *
	 * @param userId
	 * @param transWithdrawId 允许transId为null,因为KYC通过的时候可能没有提币风控的信息
	 */
	public void deleteRiskBlackListByUserId(final Long userId, String transWithdrawId,String transId,FaceTransType transType,String bizNo,String kycStatus,Date kycPassTime) {
		final String track = TrackingUtils.getTrackingChain();
		AsyncTaskExecutor.execute(() -> {
			// 判断删除提币人脸标识开关是否打开，如果未打开，直接不处理
			TrackingUtils.putTracking(track);
			try {
				switch (apolloCommonConfig.getWithdrawFaceDeleteBlackListSwitch()) {
				case 0:
					return;
				case 1:
					RiskWithdrawBlackListDeleteRequest request = new RiskWithdrawBlackListDeleteRequest();
					request.setUserId(userId.toString());
					log.info("人脸识别通过后自动关闭提币人脸识别标识后触发删除风控黑名单. userId:{}", userId);
					riskWithdrawApi.deleteBlackListByUserId(APIRequest.instance(request));
					break;
				case 2:
					RiskWithdrawUserFaceRequest faceRequest = new RiskWithdrawUserFaceRequest();
					faceRequest.setUserId(userId.toString());
					TransactionFaceLog faceLog = null;
					if (StringUtils.isNotBlank(transWithdrawId)) {
						faceLog = transactionFaceLogMapper.findByTransId(transWithdrawId, FaceTransType.WITHDRAW_FACE.name());
					}
					if (faceLog != null) {
						faceRequest.setWithdrawId(faceLog.getWithdrawId());
					}
					APIResponse<Boolean> result = riskWithdrawDecisionEngineApi
							.confirmUserFace(APIRequest.instance(faceRequest));
					log.info("风控确认用户人脸识别通过流程结果：userId:{} transWithdrawId:{} withdrawId:{}", userId, transWithdrawId,
							faceRequest.getWithdrawId(), JSON.toJSONString(result));
					break;
				case 3:
					Map<String,Object> faceRequestV2 = new HashMap<>();
					faceRequestV2.put("userId", userId.toString());
					TransactionFaceLog faceLogV2 = null;
					if (StringUtils.isNotBlank(transWithdrawId)) {
						faceLogV2 = transactionFaceLogMapper.findByTransId(transWithdrawId, FaceTransType.WITHDRAW_FACE.name());
					}
					if (faceLogV2 != null) {
						faceRequestV2.put("withdrawId", faceLogV2.getWithdrawId());
					}
					
					if(StringUtils.isBlank(kycStatus)) {
						KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
						faceRequestV2.put("kycStatus", certificateResult.isForbidPassed() ? KycCertificateStatus.FORBID_PASS.name() : KycCertificateStatus.PASS.name());
						faceRequestV2.put("kycPassTime", certificateResult.getUpdateTime());
					}else {
						faceRequestV2.put("kycStatus", kycStatus);
						faceRequestV2.put("kycPassTime", kycPassTime);
					}
					
					faceRequestV2.put("type", transType);
					faceRequestV2.put("status",FaceStatus.FACE_PASS);
					//构建大数据map信息
					setV2riskData(transWithdrawId, transId, transType, bizNo, faceRequestV2, userId);

					log.info("风控确认用户人脸识别通过流V2：userId:{} transWithdrawId:{} withdrawId:{} request:{}", userId, transWithdrawId,
							faceRequestV2.get("withdrawId"), JSON.toJSONString(faceRequestV2));
					APIResponse<Boolean> resultV2 = riskWithdrawDecisionEngineApi
							.confirmUserFaceV2(APIRequest.instance(faceRequestV2));
					log.info("风控确认用户人脸识别通过流程结果V2：userId:{} transWithdrawId:{} withdrawId:{}", userId, transWithdrawId,
							faceRequestV2.get("withdrawId"), JSON.toJSONString(resultV2));
					break;
				default:
					throw new BusinessException(GeneralCode.SYS_ERROR,
							"系统配置参数:withdraw.face.delete.blackList.switch错误, 允许值0,1,2");
				}
			} catch (Exception e) {
				log.error("删除风控黑名单错误. userId:{} tranId:{}", userId, transWithdrawId, e);
			} finally {
				TrackingUtils.removeTracking();
			}
		});
	}

	/**
	 * 提币风控的人脸识别信息，推送到风控，让风控再发布到KAFKA推送给大数据, 该方法使用异步处理，不关注推送结果
	 */
	private void pullRiskByAuto(final String transId, final Long userId, final Object faceResult) {
		final String track = TrackingUtils.getTrackingChain();
		AsyncTaskExecutor.execute(() -> {
			TrackingUtils.putTracking(track);
			try {
				String bizNo = null;
				FaceLogVo vo = null;
				if (faceResult instanceof FacePcResponse) {
					FacePcResponse face = (FacePcResponse) faceResult;
					// PC 端的认证
					bizNo = face.getFaceBizNo();
					vo = iFace.getFaceLogByBizNo(userId, transId, bizNo);
					if (vo == null) {
						log.warn("获取PC用户提币人脸识别记录信息失败. userId:{} transId:{} bizNo:{}", userId, transId, bizNo);
						return;
					}
				} else if (faceResult instanceof FaceSdkResponse) {
					FaceSdkResponse face = (FaceSdkResponse) faceResult;
					// APP SDK 端的认证
					bizNo = face.getFaceBizNo();
					vo = iFace.getFaceLogByBizNo(userId, transId, bizNo);
					if (vo == null) {
						log.warn("获取SDK用户提币人脸识别记录信息失败. userId:{} transId:{} bizNo:{}", userId, transId, bizNo);
						return;
					}
				}else if (faceResult instanceof FacePcPrivateResult) {
					FacePcPrivateResult face = (FacePcPrivateResult) faceResult;
					// APP SDK 端的认证
					bizNo = face.getBizNo();
					vo = iFace.getFaceLogByBizNo(userId, transId, bizNo);
					if (vo == null) {
						log.warn("获取SDK用户提币人脸识别记录信息失败. userId:{} transId:{} bizNo:{}", userId, transId, bizNo);
						return;
					}
				}
				pullRiskData(userId, transId, bizNo, vo.getFaceStatus(), vo, transId);
			} catch (Exception e) {
				log.error("提币风控人脸识别结果推送风控异常. userId:{} transId:{}", userId, transId, e);
			} finally {
				TrackingUtils.removeTracking();
			}
		});
	}

	/**
	 * 提币风控的人脸识别信息，推送到风控，让风控再发布到KAFKA推送给大数据, 该方法使用异步处理，不关注推送结果
	 *
	 * @param transId
	 * @param userId
	 * @param faceStatus
	 * @param withdrawFaceTransId 用于关联的提币Id
	 */
	public void pullRiskByAudit(final String transId, final Long userId, final FaceStatus faceStatus, final String withdrawFaceTransId) {
		final String track = TrackingUtils.getTrackingChain();
		AsyncTaskExecutor.execute(() -> {
			TrackingUtils.putTracking(track);
			try {
				List<FaceLogVo> faceLogVos = iFace.getFaceLogsByUser(userId, transId, null, FaceStatus.FACE_PASS);
				if (faceLogVos == null || faceLogVos.isEmpty()) {
					return;
				}
				FaceLogVo vo = faceLogVos.get(0);
				pullRiskData(userId, transId, vo.getBizNo(), faceStatus, vo, withdrawFaceTransId);
			} catch (Exception e) {
				log.error("提币风控人脸识别结果推送风控异常. userId:{} transId:{}", userId, transId, e);
			} finally {
				TrackingUtils.removeTracking();
			}
		});
	}
	/**
	 * transId为空不发送大数据，如果bizNo不为空，则更具bizNo查询faceLog，如果transType不为空，则更具transType查询faceLog
	 * @param transWithdrawId
	 * @param transId
	 * @param transType
	 * @param bizNo
	 * @param request
	 * @param userId
	 */
	private void setV2riskData(String transWithdrawId,String transId,FaceTransType transType,String bizNo,Map<String, Object> request,Long userId) {

		if(StringUtils.isBlank(transId)) {
			return;
		}

		if(StringUtils.isNotBlank(bizNo)) {
			FaceLogVo vo = iFace.getFaceLogByBizNo(userId, transId, bizNo);
			buildRiskData(request, userId, transId, vo.getBizNo(), vo.getFaceStatus(), vo, transWithdrawId);
			return;
		}

		if(transType != null) {
			TransactionFaceLog transactionFaceLog = getByMasterdb(transId, transType);

			if (transactionFaceLog != null && FaceStatus.FACE_PASS.name().equalsIgnoreCase(transactionFaceLog.getFaceStatus())) {
				List<FaceLogVo> faceLogVos = iFace.getFaceLogsByUser(userId, transId, transType, FaceStatus.FACE_PASS);
				if (faceLogVos != null && !faceLogVos.isEmpty()) {
					FaceLogVo vo = faceLogVos.get(0);
					buildRiskData(request, userId, transId, vo.getBizNo(), vo.getFaceStatus(), vo, transWithdrawId);
				}
			}
		}
	}

	private Map<String, Object> buildRiskData(Map<String, Object> pullMap,Long userId, String transId, String faceId, FaceStatus faceStatus, FaceLogVo vo, String withdrawFaceTransId){
		JumioInfoVo jumioInfoVo = jumioBusiness.getLastByUserAndBizId(userId, transId, vo.getTransType().getCode());
		if (jumioInfoVo != null) {
			pullMap.put("jumioApplyIp", jumioInfoVo.getApplyIp());
			pullMap.put("jumioClientIp", jumioInfoVo.getClientIp());
		}
		if (StringUtils.isNotBlank(withdrawFaceTransId)) {
			// 如果没有传，进行一次查询
			TransactionFaceLog transactionFaceLog = transactionFaceLogMapper.findByTransId(withdrawFaceTransId, FaceTransType.WITHDRAW_FACE.name());
			if (transactionFaceLog != null && StringUtils.isNotBlank(transactionFaceLog.getWithdrawId())) {
				pullMap.put("withdrawId", transactionFaceLog.getWithdrawId());
			}
		}
		pullMap.put("faceId", faceId);
		pullMap.put("userId", userId);
		pullMap.put("transId", transId);
		pullMap.put("type", vo.getTransType());
		pullMap.put("status", faceStatus == null ? vo.getFaceStatus() : faceStatus);
		pullMap.put("ip", vo.getClientIp());
		pullMap.put("source", vo.getSource());
		pullMap.put("updateTime", vo.getUpdateTime());
		pullMap.put("remark", vo.getFaceRemark());
		// 人脸识别置信度（分数）
		pullMap.put("faceConfidence", vo.getFaceConfidence());
		// 疑似合成脸分数 (默认0.5为阈值)
		pullMap.put("syntheticFaceConfidence", vo.getSyntheticFaceConfidence());
		// 疑似面具分数 (默认0.5为阈值)
		pullMap.put("maskConfidence", vo.getMaskConfidence());
		// 屏幕翻拍分数 (默认0.5为阈值)
		pullMap.put("screenReplayConfidence", vo.getScreenReplayConfidence());
		// 只取值0或1。0表示未检测出换脸攻击
		pullMap.put("faceReplaced", vo.getFaceReplaced());
		return pullMap;
	}

	private void pullRiskData(Long userId, String transId, String faceId, FaceStatus faceStatus, FaceLogVo vo, String withdrawFaceTransId)
			throws Exception {
		Map<String, Object> pullMap = new HashMap<>();
		// 根据人脸识别中获取到到业务编号，查询对应到jumio审核信息，如果能查询到，获取对应到Jumio到一些ip信息
		buildRiskData(pullMap, userId, transId, faceId, faceStatus, vo, withdrawFaceTransId);
		log.info("推送人脸识别数据到大数据. userId:{} transId:{} faceId:{} status:{} type:{}", userId, transId, faceId, faceStatus,
				vo.getTransType());
		riskUserFaceApi.faceRecognitionToBigData(APIRequest.instance(pullMap));
	}

	@Override
	public TransactionFaceLog transFaceAudit(TransFaceAuditRequest auditRequest, FaceTransType faceTransType) {
		TransactionFaceLog faceLog = super.transFaceAudit(auditRequest, faceTransType);
		String transId = faceLog.getTransId();
		Long userId = faceLog.getUserId();
		LanguageEnum languageEnum = emailNotifyLanguage(userId);
		if (faceLog.getStatus() == TransFaceLogStatus.FAIL) {
			log.info("提币风控人脸识别人工审核拒绝. userId:{} transId:{}", userId, transId);
			User user = super.getUserByUserId(userId);
			// 人工拒绝的情况下，直接发送一个消息给大数据和发送一封拒绝提醒邮件
			userCommonBusiness.sendEmailWithoutRequest(AccountConstants.EMAIL_WITHDRAW_FACE_REFUSED_NOTIFY, user, null,
					"提币风控人脸识别拒绝通知", languageEnum);
		} else if (faceLog.getStatus() == TransFaceLogStatus.PASSED) {
			log.info("提现风控人脸识别审核通过后自动变更提现风控人脸识别的标识. userId:{} transId:{}", userId, transId);
			changeWithdrawFaceStatus(userId, UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO, null, languageEnum, true);
			// 异步删除用户风控黑名单
			deleteRiskBlackListByUserId(userId, transId , transId ,faceTransType,null,null,null);
			if(apolloCommonConfig.getWithdrawFaceDeleteBlackListSwitch() != 3) {
				pullRiskByAudit(transId, userId, FaceStatus.FACE_PASS, transId);
			}
		} else {
			log.error("提币风控人脸识别人工审核后状态错误. userId:{} transId:{}", userId, transId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		return faceLog;
	}

	/**
	 * 当提币风控人脸识别从需要做人脸识别变更到不需要做人脸识别下，发送通知邮件
	 *
	 * @param userId
	 * @param language
	 */
	public int changeWithdrawFaceStatus(Long userId, int changeStatus, Integer fromStatus, LanguageEnum language, boolean needEmail) {
		int row = userSecurityMapper.updateWithdrawSecurityFaceStatusByUserId(userId, changeStatus, fromStatus);
		if (needEmail && changeStatus == UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO
				&& fromStatus == UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO  && row > 0) {
			log.info("把提币风控人脸识别认证标识变更为关闭状态成功: userId:{}", userId);
			User user = super.getUserByUserId(userId);
			// 发送通知邮件
			language = language == null ? emailNotifyLanguage(userId) : language;
			userCommonBusiness.sendEmailWithoutRequest(AccountConstants.EMAIL_WITHDRAW_FACE_PASS_NOTIFY, user, null,
					"提币风控人脸识别关闭通知", language);
		}
		return row;
	}

}
