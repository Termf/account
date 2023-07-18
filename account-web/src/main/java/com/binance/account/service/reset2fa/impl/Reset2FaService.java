package com.binance.account.service.reset2fa.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.enums.ResetNextStep;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.common.exception.InitJumioException;
import com.binance.account.common.query.ResetModularQuery;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CertificateAuthResult;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IKycCertificate;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.face.FaceHandlerHelper;
import com.binance.account.service.notification.SecurityNotificationService;
import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.service.reset2fa.IReset2Fa;
import com.binance.account.service.reset2fa.cache.RedisUtils;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.impl.UserSecurityResetHelper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.country.CountryVo;
import com.binance.account.vo.question.CreateQuestionVo;
import com.binance.account.vo.reset.request.Reset2faNextStepRequest;
import com.binance.account.vo.reset.request.Reset2faStartValidatedRequest;
import com.binance.account.vo.reset.request.ResetResendEmailRequest;
import com.binance.account.vo.reset.request.ResetUploadInitRequest;
import com.binance.account.vo.reset.response.Reset2faNextStepResponse;
import com.binance.account.vo.reset.response.Reset2faStartValidatedResponse;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.response.InitJumioResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.master.utils.WebUtils;
import com.binance.messaging.common.utils.UUIDUtils;
import com.binance.notification.api.vo.SecurityNotificationEnum;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Service
public class Reset2FaService implements IReset2Fa {

	private static final String SLASH_CHAR = "/";
	@Autowired
	private UserSecurityResetHelper userSecurityResetHelper;
	@Autowired
	private UserSecurityResetMapper userSecurityResetMapper;
	@Autowired
	private UserSecurityMapper userSecurityMapper;
	@Autowired
	private ApolloCommonConfig commonConfig;
	@Autowired
	private UserCommonBusiness userCommonBusiness;
	@Autowired
	private JumioBusiness jumioBusiness;
	@Autowired
	private ICountry iCountry;
	@Autowired
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Autowired
	private SecurityNotificationService securityNotificationService;
	@Autowired
	private QuestionModuleChecker resetChecker;
	@Autowired
	private FaceHandlerHelper faceHandlerHelper;
	@Resource
	private IQuestion iQuestion;

	@Resource
	private IKycCertificate iKycCertificate;

	@Resource
	private IFace iFace;

	/**
	 * 验证是否符合初始化重置2FA的条件，如果不符合，直接抛出对应的错误信息
	 *
	 * @return 返回保护模式下到剩余次数
	 */
	private int preCheckInitResetValidate(User user, UserSecurityResetType type, Map<String, String> device) {
		// step1: 先获取用户的状态信息
		Long userId = user.getUserId();
		UserStatusEx userStatusEx = new UserStatusEx(user.getStatus());
		if (userStatusEx.getIsSubUser()) {
			log.warn("子账户禁止使用重置2FA流程. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.SUB_USER_FEATURE_FORBIDDEN);
		}
		UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
		if (userSecurity == null) {
			log.error("获取用户安全信息失败. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// 手机号登录问题，先判断用户是否绑定了邮箱，如果未绑定，先不能做重置/解禁
		if (userStatusEx.getIsUserNotBindEmail()) {
			log.warn("重置2fa/解禁账户但未绑定邮箱. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.USER_EMAIL_NOT_BIND);
		}
		// step2: 检查用户重置的类型是否正常
		switch (type) {
		case google:
			if (StringUtils.isBlank(userSecurity.getAuthKey())) {
				log.warn("重置GOOGLE但是缺失谷歌认证KEY. userId:{}", userId);
				throw new BusinessException(AccountErrorCode.USER_NOT_ENABLE_GOOGLE);
			}
			break;
		case mobile:
			if (!userStatusEx.getIsUserMobile() || StringUtils.isBlank(userSecurity.getMobile())) {
				log.warn("重置MOBILE但是未绑定手机信息. userId:{}", userId);
				throw new BusinessException(AccountErrorCode.USER_NOT_ENABLE_MOBILE);
			}
			break;
		case enable:
			if (!userStatusEx.getIsUserDisabled()) {
				log.warn("用户是否禁用标识表明用户未禁用. userId:{}", userId);
				throw new BusinessException(AccountErrorCode.USER_NOT_DISABLE);
			}
			// 锁定时间在两小时内的，不能做解禁操作
			Date disableTime = userSecurity.getDisableTime();
			int disableLockHour = commonConfig.getResetEnableDisableLockHour();
			if (disableTime != null
					&& DateUtils.addHours(disableTime, disableLockHour).compareTo(DateUtils.getNewUTCDate()) > 0) {
				log.warn("用户解禁流程不能在禁用两小时内发起解禁流程. userId:{}", userId);
				throw new BusinessException(AccountErrorCode.USER_CAN_NOT_ENABLE_IN_2HOUR);
			}
			break;
		default:
			log.error("重置流程类型错误. userId:{} type:{}", userId, type);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		/*
		 * step3: 检查是否处于安全模式下下标识信息 新老设备需要按不同的锁定次数来判断
		 */
		String resetId = null;
		UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, type.ordinal());
		if (reset != null && Objects.equals(UserSecurityResetStatus.unverified, reset.getStatus())) {
			resetId = reset.getId();
		}
		int count = resetChecker.remainingTimes(userId, resetId);
		if (count <= 0 && StringUtils.isNotBlank(resetId)) {
			log.info("上一次答题已经没有剩余次数，并且处于答题环节，重新获取答题次数. userId:{} resetId:{}", userId, resetId);
			count = resetChecker.remainingTimes(userId, null);
		}
		log.info("当前验证用户进入保护模式到剩余次数: userId:{}, count:{}", userId, count);
		return count;
	}

	@Override
	public Reset2faStartValidatedResponse reset2faStartValidated(Reset2faStartValidatedRequest body) {
		Long userId = body.getUserId();
		UserSecurityResetType type = body.getType();
		User user = userSecurityResetHelper.getUserByUserId(userId);
		// step1: 做前置检查
		int count = preCheckInitResetValidate(user, type, body.getDeviceInfo());
		// step2: 如果前置检查能通过，生成一个 requestId，用于下一步的校验
		final String requestId = type.name() + UUIDUtils.getId();

		// step3:
		// 另起一个线程，预先检查用户的KYC是否能做人脸识别，如果能，则保存对应的人脸识别对比照信息，在创建重置流程的时候，直接根据该预检查信息判断是否能做人脸识别
		KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
		if (Objects.equals(KycCertificateResult.STATUS_PASS, certificateResult.getCertificateStatus())) {
			// 用户KYC已经通过，异步发起一次检查用户是否能做人脸识别的检查
			String tradeId = TrackingUtils.getTrackingChain();
			AsyncTaskExecutor.execute(() -> {
				TrackingUtils.putTracking(tradeId);
				try {
					boolean checkResult = faceHandlerHelper.checkCurrentKycCanDoFace(userId, certificateResult, requestId, 2);
					log.info("预检查用户KYC照片是否能做人脸识别的结果：userId:{} requestId:{} result:{}", userId, requestId, checkResult);
				} catch (Exception e) {
					log.info("预检查用户KYC照片是否能做人脸识别异常. userId:{} requestId:{}", userId, requestId);
				} finally {
					TrackingUtils.removeTracking();
				}
			});
		}
		return new Reset2faStartValidatedResponse(requestId, count);
	}

	@Override
	public Reset2faNextStepResponse reset2faNextStepFlow(Reset2faNextStepRequest request) {
		Long userId = request.getUserId();
		UserSecurityResetType type = request.getType();
		User user = userSecurityResetHelper.getUserByUserId(userId);
		// step 1: 再次验证前置检查是否允许
		int count = preCheckInitResetValidate(user, type, request.getDeviceInfo());
		// step 2: 如果能初步检查完成，开始尝试创建重置流程记录
		log.info("重置验证初步完成, 开始尝试初始化重置流程. userId:{} type:{}", userId, type);
		UserSecurityReset reset = tryGenerateInitResetFlow(userId, type);
		// step 3: 检查返回的重置流程的下一步
		// 是否需要回答问题
		boolean needAnswer = iQuestion.needToAnswerQuestion(userId, reset.getType().name(), request.getDeviceInfo());
		Reset2faNextStepResponse nextResult = getNextStepAndChangeStatus(user, reset, false, count, needAnswer);
		// 如果是upload 的状态，不能返回upload url
		if (nextResult.getNextStep() == ResetNextStep.UPLOAD) {
			nextResult.setUploadUrl(null);
		}
		log.info("重置下一步流程信息: userId:{} resetId:{} next:{}", userId, reset.getId(), JSON.toJSONString(nextResult));
		// RM-426 缓存设备pk
		resetChecker.cacheDevicePK(userId, reset.getId(), request.getDeviceInfo());
		return nextResult;
	}

	/**
	 * 尝试初始化重置流程对象信息
	 *
	 * @param userId
	 * @param type
	 * @return
	 */
	private UserSecurityReset tryGenerateInitResetFlow(Long userId, UserSecurityResetType type) {
		// step1: 检查当前是否存在有申请正在处理中的流程
		UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, type.ordinal());
		if (reset != null && UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
			if (Objects.equals(UserSecurityResetStatus.unverified, reset.getStatus())) {
				// 需要重新检查是否已经答题次数用完，
				int count = resetChecker.remainingTimes(userId, reset.getId());
				if (count <= 0) {
					log.info("由于上一次答题次数用完了且没有剩余次数了，需要先取消旧的流程后重新创建一笔流程。userId:{}, resetId:{}", userId, reset.getId());
					reset.setFailReason("答题次数用完但是还处于答题流程时做取消操作");
					resetChecker.cancelReset(reset); // 取消旧的流程，创建新的流程
					log.info("创建新的重置流程. userId:{} type:{}", userId, type);
					return generateNewResetFlow(userId, type);
				}
			}
			return reset;
		} else {
			// step3: 没有做过重置记录或者老的已经结束流程，则生成新的重置流程(不带状态值)
			log.info("创建新的重置流程. userId:{} type:{}", userId, type);
			return generateNewResetFlow(userId, type);
		}
	}

	private UserSecurityReset generateNewResetFlow(Long userId, UserSecurityResetType type) {
		UserSecurityReset reset = new UserSecurityReset();
		String id = UUIDUtils.getId();
		reset.setId(id);
		reset.setUserId(userId);
		reset.setCreateTime(DateUtils.getNewUTCDate());
		reset.setUpdateTime(DateUtils.getNewUTCDate());
		reset.setType(type);
		reset.setApplyIp(WebUtils.getRequestIp());
		// 设置标识当前用户KYC的状态信息
//		KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
//		if (Objects.equals(KycCertificateResult.STATUS_PASS, certificateResult.getCertificateStatus())) {
//			setResetCertificateInfo(reset, certificateResult);
//			reset.setCertificateIsForbidPassed(false);
//		} else if (Objects.equals(KycCertificateResult.STATUS_REFUSED, certificateResult.getCertificateStatus())
//				&& certificateResult.isForbidPassed()) {
//			log.info("用户kyc认证由于在不合规国籍通过类型，允许认为是通过的情况，可以让用户直接做人脸识别的。userId:{} certificateType:{} certificateId:{}",
//					userId, certificateResult.getCertificateType(), certificateResult.getCertificateId());
//			setResetCertificateInfo(reset, certificateResult);
//			reset.setCertificateIsForbidPassed(true);
//		} else {
//			// KYC 未验证
//			reset.setCertificateType(0);
//			reset.setCertificateIsForbidPassed(false);
//		}

		CertificateAuthResult certificateAuthResult = iKycCertificate.getCertificateAuth(userId);
		setResetCertificateInfo(reset, certificateAuthResult);

		reset.setStatus(null);
		// 入库
		userSecurityResetMapper.insertSelective(reset);
		return reset;
	}

	private void setResetCertificateInfo(UserSecurityReset reset, KycCertificateResult certificateResult) {
		// kyc 已经认证
		if (Objects.equals(KycCertificateResult.TYPE_USER, certificateResult.getCertificateType())) {
			// KYC 个人认证
			reset.setCertificateType(KycCertificateResult.TYPE_USER);
			reset.setCertificateId(certificateResult.getCertificateId());
		} else if (Objects.equals(KycCertificateResult.TYPE_COMPANY, certificateResult.getCertificateType())) {
			// KYC 企业认证
			reset.setCertificateType(KycCertificateResult.TYPE_COMPANY);
			reset.setCertificateId(certificateResult.getCertificateId());
		} else {
			// KYC 未验证
			reset.setCertificateType(0);
		}
	}

	private void setResetCertificateInfo(UserSecurityReset reset, CertificateAuthResult certificateResult) {
		if (certificateResult == null) {
			reset.setCertificateType(0);
			return;
		}
		// kyc 已经认证
		if (Objects.equals(KycCertificateResult.TYPE_USER, certificateResult.getCertificateType())) {
			// KYC 个人认证
			reset.setCertificateType(KycCertificateResult.TYPE_USER);
		} else if (Objects.equals(KycCertificateResult.TYPE_COMPANY, certificateResult.getCertificateType())) {
			// KYC 企业认证
			reset.setCertificateType(KycCertificateResult.TYPE_COMPANY);
		} else {
			// KYC 未验证
			reset.setCertificateType(0);
		}
		reset.setNewVersion(certificateResult.isNewVersion());
		reset.setCertificateStatus(certificateResult.getStatus());
		reset.setCertificateIsForbidPassed(certificateResult.isForbidPassed());
		reset.setCertificateSource(certificateResult.getSoucre());
	}

	/**
	 * 根据当前的重置流程信息，获取当前流程的下一步
	 *
	 * @param reset
	 * @param needInitUpload
	 * @param protectCount   如果在答题情况下，需要表面剩余的次数
	 * @return
	 */
	private Reset2faNextStepResponse getNextStepAndChangeStatus(User user, UserSecurityReset reset,
			boolean needInitUpload, Integer protectCount, boolean needAnswer) {
		Long userId = reset.getUserId();
		String resetId = reset.getId();
		UserSecurityResetStatus status = reset.getStatus();
		if (status == null) {
			log.info("当前流程是初建，需要检查下一步流程和变更到对应状态. userId:{} resetId:{}", userId, resetId);
			// step1.1: 刚刚初始化出来的流程，需要检查KYC信息
//			if (reset.getCertificateId() != null && (Objects.equals(1, reset.getCertificateType())
//					|| Objects.equals(2, reset.getCertificateType()))) {
			if (CertificateAuthResult.STATUS_PASS.equals(reset.getCertificateStatus())
					|| CertificateAuthResult.STATUS_REVIEW.equals(reset.getCertificateStatus())) {
				// step1.3 用户存在有KYC认证信息，检查是否能直接做人脸识别，如果可以，则直接进行人脸识别，如果不能做人脸识别，再走正常全流程
				log.info(
						"重置流程中，用户已经通过KYC认证，进行KYC认证检查. userId:{} resetId:{} certificateType:{} certificateId:{} isForbidPassed:{}",
						userId, resetId, reset.getCertificateType(), reset.getCertificateId(),
						reset.isCertificateIsForbidPassed());
				// 直接尝试初始化人脸识别流程
				try {
					TransactionFaceLog faceLog = userSecurityResetHelper.directInitResetFaceFlow(userId, reset);
					if (faceLog != null) {
						return setResetSkipUploadToFaceInfo(reset);
					} else {
						return checkSetNextStepIsQuestionOrUpload(user, resetId, reset, protectCount, needAnswer);
					}
				}catch(BusinessException e) {
					if(AccountErrorCode.AUTH_FACE_REFERENCE_PROCESSING.equals(e.getErrorCode()) && CertificateAuthResult.STATUS_PASS.equals(reset.getCertificateStatus())) {
						log.info("重置流程中，用户已经通过KYC认证，但是faceReference为空 userId:{}", userId);
						return checkSetNextStepIsQuestionOrUpload(user, resetId, reset, protectCount, needAnswer);
					}else{
						throw e;
					}
				}
			} else {
				// step1.2 用户没有通过KYC认证，判断是否需要进行答题环节的流程，
				return checkSetNextStepIsQuestionOrUpload(user, resetId, reset, protectCount, needAnswer);
			}
		} else {
			// step2.1 如果时之前已经存在的流程，需要检查对应状态信息,
			log.info("当前已经存在重置流程，判断当前流程处于的状态下一步: userId:{} resetId:{}, currentStatus:{}", userId, resetId, status);
			Reset2faNextStepResponse response;
			switch (status) {
			case unverified:
				// 当前已经处于回答问题环节，接直接返回问题环节信息
				if (needAnswer) {
					response = Reset2faNextStepResponse.Builder.buildQuestionStep(resetId, reset.getType().name(),
							protectCount);
					// v2问答模块，回答问题创建问答流程.同一个流程可以多次答题，多次创建flow(有超时时间，maybe上次已经过期了)
					createQuestionFlow(user, reset, resetId);
					log.info("需要回答问题，userId:{} resetId:{}", userId, resetId);
				} else {
					reset.setStatus(UserSecurityResetStatus.unsubmitted);
					userSecurityResetMapper.updateByPrimaryKeySelective(reset);
					// 判断当前的上传状态
					response = checkNextStepIsUploadOrFace(reset, needInitUpload);
					log.info("不需要回答问题，直接上传人脸，userId:{} resetId:{}", userId, resetId);
				}
				break;
			case unsubmitted:
				// 判断当前的上传状态
				response = checkNextStepIsUploadOrFace(reset, needInitUpload);
				break;
			case jumioPassed:
			case jumioPending:
				// 检查是否已经做完人脸识别，如果做完，需要等待，如果未做完，提示去做人脸识别
				response = checkNextStepIsFaceOrReview(reset);
				break;
			default:
				response = Reset2faNextStepResponse.Builder.buildReviewStep(false, reset.getType().name());
			}
			return response;
		}
	}

	private void createQuestionFlow(User user, UserSecurityReset reset, String resetId) {
		try {
			String successPath = commonConfig.getResetRedirectSuccessPath();
			String failPath = commonConfig.getResetRedirectFailPath();
			if (StringUtils.isAnyBlank(successPath, failPath)) {
				log.error("when create question flow,Must config successPath and failPath,resetId:" + resetId);
				return;
			}
			String resetType = reset.getType().name();
			LanguageEnum languageEnum = WebUtils.getAPIRequestHeader().getLanguage();
			String lang = SLASH_CHAR + languageEnum.getLang();
			if (!successPath.startsWith(SLASH_CHAR)) {
				successPath = SLASH_CHAR + successPath;
			}
			if (!failPath.startsWith(SLASH_CHAR)) {
				failPath = SLASH_CHAR + failPath;
			}
			successPath = lang + successPath;
			failPath = lang + failPath;
			CreateQuestionVo createQuestionVo = CreateQuestionVo.builder().userId(user.getUserId()).flowId(resetId)
					.flowType(resetType).timeout(commonConfig.getResetFlowTimeOut()).successCallback(successPath)
					.failCallback(failPath).build();
			log.info("create question flow,createQuestionVo:{}", createQuestionVo);
			iQuestion.createQuestionFlow(createQuestionVo);
		} catch (Exception e) {
			log.warn("create question flow. userId:{}", user.getUserId(), e);
		}
	}

	private Reset2faNextStepResponse setResetSkipUploadToFaceInfo(UserSecurityReset reset) {
		log.info("当前重置流程跳过上传直接做人脸识别: userId:{} resetId:{}", reset.getUserId(), reset.getId());
		// 跳过JUMIO直接去做人脸识别
		if(CertificateAuthResult.STATUS_PASS.equals(reset.getCertificateStatus())) {
			reset.setStatus(UserSecurityResetStatus.jumioPassed);
			setResetSkipJumioInfo(reset);
		}
		if(CertificateAuthResult.STATUS_REVIEW.equals(reset.getCertificateStatus())){
			reset.setStatus(UserSecurityResetStatus.jumioPending);
			reset.setJumioStatus(UserSecurityResetStatus.jumioPending.name());
		}
		reset.setFaceStatus(FaceStatus.FACE_PENDING.name());
		reset.setFaceRemark("KYC已通过,跳过上传直接进入人脸识别.");
		reset.setUpdateTime(DateUtils.getNewUTCDate());
		userSecurityResetMapper.updateByPrimaryKeySelective(reset);
		return Reset2faNextStepResponse.Builder.buildFaceStep(reset.getId(), reset.getType().name());
	}

	private void setResetSkipJumioInfo(UserSecurityReset reset) {
		reset.setJumioStatus(UserSecurityResetStatus.jumioPassed.name());
		StringBuilder sb = new StringBuilder("skip by kyc ");
		sb.append(reset.getCertificateType() == 1 ? "个人认证" : "企业认证");
		sb.append(reset.getCertificateId());
		reset.setJumioRemark(sb.toString());
		if (reset.getCertificateType() == null) {
			return;
		}

		if(CertificateAuthResult.JUMIO.equals(reset.getCertificateSource())) {
			JumioInfoVo jumioInfoVo = jumioBusiness.getLastByUserId(reset.getUserId());

			if (jumioInfoVo != null) {
				reset.setScanReference(jumioInfoVo.getScanReference());
				reset.setIdNumber(jumioInfoVo.getNumber());
				reset.setDocumentType(jumioInfoVo.getDocumentType());
				CountryVo country = iCountry.getCountryByAlpha3(jumioInfoVo.getIssuingCountry());
				reset.setIssuingCountry(country != null ? country.getCode() : null);
				reset.setFront(jumioInfoVo.getFront());
				reset.setBack(jumioInfoVo.getBack());
				reset.setHand(jumioInfoVo.getFace());
			}
		}

		if(CertificateAuthResult.FACE_OCR.equals(reset.getCertificateSource())) {
			FaceIdCardOcrVo vo = iFace.getFaceIdCardOcr(reset.getUserId());
			if(vo != null) {
				reset.setIdNumber(vo.getIdcardNumber());
				reset.setDocumentType("ID_CARD");
				reset.setIssuingCountry("CN");
				reset.setFront(vo.getFront());
				reset.setBack(vo.getBack());
				reset.setHand(vo.getFace());
			}
		}

	}

	private Reset2faNextStepResponse checkSetNextStepIsQuestionOrUpload(User user, String resetId,
			UserSecurityReset reset, Integer protectCount, boolean needAnswer) {
		Long userId = user.getUserId();
		Reset2faNextStepResponse response;
		if (!isNeedCheckQuestion(needAnswer)) {
			log.info("reset流程不需要答题, userId:{} resetId:{} type:{}", userId, resetId, reset.getType());
			response = toUploadStatus(user, reset);
		} else {
			// 进入答题环节
			reset.setStatus(UserSecurityResetStatus.unverified);
			reset.setUpdateTime(DateUtils.getNewUTCDate());
			userSecurityResetMapper.updateByPrimaryKeySelective(reset);
			response = Reset2faNextStepResponse.Builder.buildQuestionStep(resetId, reset.getType().name(),
					protectCount);
			// v2问答模块，回答问题创建问答流程
			createQuestionFlow(user, reset, resetId);
		}
		return response;
	}

	private boolean isNeedCheckQuestion(boolean needAnswer) {
		int needQuestionPercent = commonConfig.getResetQuestionPercent();
		boolean result = false;
		if (needQuestionPercent <= 0) {
			result = false;
		} else if (needQuestionPercent >= 100) {
			result = true;
		} else {
			int randNumber = RandomUtils.nextInt(0, 100);
			result = randNumber < needQuestionPercent;
		}
		if (result) { // 答题开关打开后，按照是否答题为准
			return needAnswer;
		}
		return result;// 开关关闭了
	}

	/**
	 * 变更到做上传对状态
	 *
	 * @param user
	 * @param reset
	 * @return
	 */
	public Reset2faNextStepResponse toUploadStatus(User user, UserSecurityReset reset) {
		// 新创建的reset流程status是null
		if (reset.getStatus() != null && !Objects.equals(UserSecurityResetStatus.unverified, reset.getStatus())) {
			log.warn("不用答题直接人脸，状态异常, userId:{} resetId:{}", user.getUserId(), reset.getId());
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		reset.setStatus(UserSecurityResetStatus.unsubmitted);
		reset.setUpdateTime(DateUtils.getNewUTCDate());
		userSecurityResetMapper.updateByPrimaryKeySelective(reset);
		Reset2faNextStepResponse response = Reset2faNextStepResponse.Builder.buildUploadStep(reset.getId(),
				reset.getType().name(), null);
		// 发邮件之前缓存requstId
		log.info("发邮件之前缓存requstId,userId:{},resetId:{}", user.getUserId(), reset.getId());
		cacheNextStepInfo(response);
		// 发送上传邮件
		log.info("发送上传邮件中,userId:{},resetId:{}", user.getUserId(), reset.getId());
		sendInitResetUploadEmail(user, reset, response.getRequestId());
		log.info("发送上传邮件结束,userId:{},resetId:{}", user.getUserId(), reset.getId());
		return Reset2faNextStepResponse.Builder.buildUploadStep(null, null, null);
	}

	/**
	 * 发送上传的通知邮件
	 *
	 * @param user
	 * @param reset
	 * @param requestId
	 */
	private void sendInitResetUploadEmail(User user, UserSecurityReset reset, String requestId) {
		Long userId = user.getUserId();
		UserSecurityResetType type = reset.getType();
		// 从缓存中查询当前域名
		String baseUrl = getBaseuUrl(userId, reset.getId());
		if (StringUtils.isBlank(baseUrl)) {
			log.error("获取基础域名失败. userId:{} type:{}", userId, type);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		baseUrl = removeLastChar(baseUrl, SLASH_CHAR);

		String uri = commonConfig.getResetUploadEmailPath();
		if (StringUtils.isBlank(uri)) {
			log.error("Reset upload email path config miss. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// 从缓存中查询当前语言
		LanguageEnum languageEnum = getLang(userId, reset.getId());
		// 通过notification发送通知
		securityNotificationService.saveSecurityNotification(userId, SecurityNotificationEnum.RESET_2FA, languageEnum);
		uri = removeLastChar(uri, SLASH_CHAR);
		uri = removeFirstChar(uri, SLASH_CHAR);

		StringBuilder sb = new StringBuilder(baseUrl);
		sb.append(SLASH_CHAR);
		sb.append(languageEnum.getLang());
		sb.append(SLASH_CHAR);
		sb.append(uri);
		sb.append("?id=");
		sb.append(reset.getId());
		sb.append("&type=");
		sb.append(type.name());
		sb.append("&requestId=");
		sb.append(requestId);
		String link = sb.toString();
		log.info("2fa重置->正在发邮件,userId:{},type:{},URL:{},", userId, type, link);
		// 生成发送邮件信息
		Map<String, Object> emailParams = Maps.newHashMap();
		emailParams.put("link", link);
		String tplCode = "";
		String remark = "";
		switch (type) {
		case google:
			tplCode = AccountConstants.RESET_INIT_2FA_EMAIL_TEMPLATE;
			remark = "重置谷歌认证邮件";
			if (languageEnum == LanguageEnum.ZH_CN) {
				emailParams.put("type", "谷歌验证");
			} else {
				emailParams.put("type", "Google Authenticator");
			}
			break;
		case mobile:
			tplCode = AccountConstants.RESET_INIT_2FA_EMAIL_TEMPLATE;
			remark = "重置手机认证邮件";
			if (languageEnum == LanguageEnum.ZH_CN) {
				emailParams.put("type", "手机验证");
			} else {
				emailParams.put("type", "SMS Authenticator");
			}
			break;
		case enable:
			tplCode = AccountConstants.RESET_INIT_ENABLE_EMAIL_TEMPLATE;
			remark = "用户解禁认证邮件";
			break;
		default:
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		userCommonBusiness.sendDisableTokenEmail(tplCode, user, emailParams, remark, null);
		log.info("发送初始化重置流程邮件成功. userId:{} type:{}", user.getUserId(), type);
	}

	private LanguageEnum getLang(Long userId, String flowId) {
		return WebUtils.getAPIRequestHeader().getLanguage();
	}

	private String getBaseuUrl(Long userId, String flowId) {
		return WebUtils.getHeader(Constant.BASE_URL);
	}

	private String removeLastChar(String input, String lastChar) {
		while (input.endsWith(lastChar)) {
			int endIndex = input.length() - 1;
			input = input.substring(0, endIndex);
		}
		return input;
	}

	private String removeFirstChar(String input, String firstChar) {
		while (input.startsWith(firstChar)) {
			input = input.substring(1);
		}
		return input;
	}

	/**
	 * 判断是否处于人脸识别状态还是需要等待状态
	 *
	 * @param reset
	 * @return
	 */
	private Reset2faNextStepResponse checkNextStepIsFaceOrReview(UserSecurityReset reset) {
		// 检查下当前的人脸识别流程状态，如果处于正在做人脸识别的流程，则返回做人脸识别的状态，否则返回等待审核的状态
		FaceTransType faceTransType = FaceTransType.getByCode(reset.getType().name());
		TransactionFaceLog transactionFaceLog = transactionFaceLogMapper.findByUserIdTransId(reset.getUserId(),
				reset.getId(), faceTransType.name());
		// 检查下当前是否能做人脸识别，如果不能做，返回给用户说等待，但存在有下一步
		log.info("重置流程当前人脸识别流程状态信息：userId:{} resetId:{} faceStatus:{}", reset.getUserId(), reset.getId(),
				transactionFaceLog == null ? null : transactionFaceLog.getStatus());
		Reset2faNextStepResponse nextStepResponse;
		if (transactionFaceLog != null) {
			if (TransFaceLogStatus.isEndStatus(transactionFaceLog.getStatus())
					|| TransFaceLogStatus.REVIEW == transactionFaceLog.getStatus()) {
				// 人脸识别已经结束，只能等待
				nextStepResponse = Reset2faNextStepResponse.Builder.buildReviewStep(false, reset.getType().name());
			} else {
				// 需要用户去做人脸识别
				nextStepResponse = Reset2faNextStepResponse.Builder.buildFaceStep(transactionFaceLog.getTransId(),
						faceTransType.getCode());
			}
		} else {
			// 当前还未初始化好人脸识别的流程，只能让用户等待人脸识别的初始化
			nextStepResponse = Reset2faNextStepResponse.Builder.buildReviewStep(true, reset.getType().name());
		}
		return nextStepResponse;
	}

	/**
	 * 判断在unsubmitted 状态时对下一笔操作
	 *
	 * @param reset
	 * @param needInitUpload 是否需要初始化上传JUMIO的信息
	 * @return
	 */
	private Reset2faNextStepResponse checkNextStepIsUploadOrFace(UserSecurityReset reset, boolean needInitUpload) {
		// 注意：只处理状态为 unsubmitted 对状态
		String resetId = reset.getId();
		String typeCode = reset.getType().name();
		Long userId = reset.getUserId();
		Reset2faNextStepResponse response;
		if (StringUtils.isAnyBlank(reset.getScanReference(), reset.getJumioToken())) {
			// 还未做上传，需要做上传操作
			log.info("重置流程还未上传，初始上传的信息后返回上传内容, userId:{}, resetId:{} needInitUpload:{}", userId, resetId,
					needInitUpload);
			String uploadUrl = null;
			if (needInitUpload) {
				uploadUrl = resetInitJumio(userId, resetId, reset);
			}
			response = Reset2faNextStepResponse.Builder.buildUploadStep(resetId, typeCode, uploadUrl);
		} else {
			// 检查是否已经上传过，
			JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, reset.getScanReference(), typeCode);
			if (jumioInfoVo == null || JumioStatus.INIT == jumioInfoVo.getStatus()) {
				log.info("未初始化过上传逻辑或者已经初始化未上传的情况下进行上传. userId:{} resetId:{},needInitUpload:{}", userId, resetId,
						needInitUpload);
				String uploadUrl = JumioStatus.INIT == jumioInfoVo.getStatus() ? jumioInfoVo.getRedirectUrl() : null;// 前端靠这个判断邮件是否受检
				if (needInitUpload) {
					if (jumioInfoVo != null && StringUtils.isNotBlank(jumioInfoVo.getRedirectUrl())) {
						uploadUrl = jumioInfoVo.getRedirectUrl();
					} else {
						uploadUrl = resetInitJumio(userId, resetId, reset);
					}
				}
				response = Reset2faNextStepResponse.Builder.buildUploadStep(resetId, typeCode, uploadUrl);
			} else {
				log.info("重置流程已经上传过，下一步检查是否到做人脸识别: userId:{} resetId:{}", userId, resetId);
				// 已经上传过了，判断是否能做人脸识别了，如果不能做则进入review 等待
				response = checkNextStepIsFaceOrReview(reset);
			}
		}
		return response;
	}

	/**
	 * 初始化重置流程的JUMIO信息
	 *
	 * @param userId
	 * @param id
	 * @param reset
	 * @return
	 */
	private String resetInitJumio(Long userId, String id, UserSecurityReset reset) {
		JumioHandlerType jumioHandlerType = null;
		switch (reset.getType()) {
		case google:
			jumioHandlerType = JumioHandlerType.RESET_GOOGLE;
			break;
		case mobile:
			jumioHandlerType = JumioHandlerType.RESET_MOBILE;
			break;
		case enable:
			jumioHandlerType = JumioHandlerType.RESET_ENABLE;
			break;
		default:
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		try {
			// 验证下单日限制次数
			long dailyCount = jumioBusiness.getDailyJumioTimes(userId, jumioHandlerType);
			long configCount = commonConfig.getResetJumioDailyCount();
			if (configCount <= dailyCount) {
				log.warn("重置流程Jumio认证24小时使用次数达到限制值{} userId:{}", configCount, userId);
				throw new BusinessException(AccountErrorCode.RESET_JUMIO_DAILY_COUNT, new Object[] { configCount });
			}
			InitJumioResponse initJumio = jumioBusiness.initWebJumioWithoutSave(userId, jumioHandlerType, id, false);
			String scanRef = initJumio.getTransactionReference();
			String redirectUrl = initJumio.getRedirectUrl();
			if (StringUtils.isAnyBlank(scanRef, redirectUrl)) {
				throw new InitJumioException("JUMIO初始化失败");
			}
			log.info("重置流程初始化JUMIO信息成功，userId:{} id:{} scanRef:{}", userId, id, scanRef);
			// 保存初始化的信息
			reset.setScanReference(scanRef);
			reset.setJumioToken(redirectUrl);
			reset.setJumioIp(WebUtils.getRequestIp());
			userSecurityResetMapper.updateByPrimaryKeySelective(reset);
			return redirectUrl;
		} catch (InitJumioException e) {
			log.error("初始化JUMIO失败. userId:{} id:{}", userId, id, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	/**
	 * 缓存下一步操作对内容
	 *
	 * @param nextStepResponse
	 */
	private void cacheNextStepInfo(Reset2faNextStepResponse nextStepResponse) {
		long cacheTime = commonConfig.getResetNextStepExpiredTime() * 60L;
		RedisUtils.cacheNextStepInfo(nextStepResponse, cacheTime);
	}

	/**
	 * 获取缓存下一步对内容
	 *
	 * @param requestId
	 * @return
	 */
	private Reset2faNextStepResponse getNextStepCacheInfo(String requestId) {
		return RedisUtils.getNextStepCacheInfo(requestId);
	}

	@Override
	public Reset2faNextStepResponse reset2faUploadEmailOpen(ResetUploadInitRequest body) {
		UserSecurityResetType type = body.getType();
		String requestId = body.getRequestId();
		String resetId = body.getTransId();
		Reset2faNextStepResponse nextStepResponse = getNextStepCacheInfo(requestId);

		// 邮件超时校验
		if (nextStepResponse == null) {
			log.warn("点击邮件->邮件的链接已经失效，需要重新发送邮件. requestId:{} reesetId:{}", requestId, resetId);
			throw new BusinessException(GeneralCode.AC_RESET_EMAIL_EXPIRED);
		}

		// 参数安全性校验
		if (nextStepResponse != null && !StringUtils.equals(resetId, nextStepResponse.getTransId())) {
			log.error("点击邮件->reset参数校验失败. requestId:{} reesetId:{}", requestId, resetId);
			throw new BusinessException(GeneralCode.AC_RESET_EMAIL_EXPIRED);
		}

		// 重置流程信息校验
		UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(resetId);
		if (isTypeNotSame(type, reset)) {
			log.error("点击邮件->获取到到2fa重置流程信息不正确，邮件失效. requestId:{} reesetId:{}", requestId, resetId);
			throw new BusinessException(GeneralCode.AC_RESET_EMAIL_EXPIRED);
		}

		Long userId = reset.getUserId();

		if(body.getUserId() != null) {
			if(!userId.equals(body.getUserId())) {
				log.error("点击邮件->获取到到2fa重置流程信息不正确，邮件用户和登陆用户不同. requestId:{} mailUserId:{} loginUserId:{}", requestId, userId, body.getUserId());
				throw new BusinessException(GeneralCode.AC_RESET_EMAIL_EXPIRED);
			}
		}


		if (!Objects.equals(UserSecurityResetStatus.unsubmitted, reset.getStatus())) {
			log.warn("点击邮件->reset不是unsubmitted或者已经jumio. userId:{} resetId:{},status:{}", userId, resetId,
					reset.getStatus());
			throw new BusinessException(GeneralCode.AC_RESET_EMAIL_EXPIRED);
		}

		User user = userSecurityResetHelper.getUserByUserId(userId);
		Reset2faNextStepResponse nextStep = getNextStepAndChangeStatus(user, reset, true, null, false);
		if (log.isInfoEnabled()) {
			log.info("点击邮件->校验结束,继续后续流程. userId:{} resetId:{} nextStep:{}", userId, resetId,
					JSON.toJSONString(nextStep));
		}
		return nextStep;
	}

	private boolean isTypeNotSame(UserSecurityResetType type, UserSecurityReset reset) {
		return reset == null || !Objects.equals(reset.getType(), type);
	}

	@Override
	public Reset2faNextStepResponse sendEmailAgain(final ResetResendEmailRequest body) {
		/**
		 * 1，邮件失效后，整体流程不失效 2，重发邮件后，原邮箱失 3，重发邮件加入没5分钟一次的频率限制
		 */
		int resendEmailTimeOut = commonConfig.getResendEmailTimeOut();
		long seconds = resendEmailTimeOut * 60L;
		Long userId = body.getUserId();
		String type = body.getType();
		UserSecurityResetType resetType = UserSecurityResetType.getByName(type);
		if (!RedisUtils.canResendEmail(userId, seconds)) {
			log.warn("重发邮件->userId:{},{}分钟内不能重发。", userId, resendEmailTimeOut);
			throw new BusinessException(AccountErrorCode.RESET_USER_RESEND_EMAIL_BUSY);
		}
		log.info("request resend reset2fa email again. userId:{} type:{}", userId, resetType);
		UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, resetType == null ? null : resetType.ordinal());
		if (reset == null) {
			log.warn("重发邮件->userId:{},重置流程不存在。", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

		if (!Objects.equals(UserSecurityResetStatus.unsubmitted, reset.getStatus())) {
			log.warn("重发邮件->userId:{},重置流程已经不是unsubmitted状态。", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		log.info("重发邮件->userId:{},开始重发邮件。", userId);
		Reset2faNextStepResponse response = Reset2faNextStepResponse.Builder.buildUploadStep(reset.getId(),
				reset.getType().name(), null);
		// 发邮件之前缓存requstId
		cacheNextStepInfo(response);
		// 发送上传邮件
		sendInitResetUploadEmail(resetChecker.userExistValidate(userId), reset, response.getRequestId());
		log.info("重发邮件->userId:{},邮件重发完毕。", userId);
		return Reset2faNextStepResponse.Builder.buildUploadStep(null, null, null);
	}

	/**
	 * 查询jumio 中间态记录
	 * @param userId
	 * @return
	 */
	public List<UserSecurityReset> findJumioPendingResets(Long userId){
		ResetModularQuery query=new ResetModularQuery();
		query.setUserId(userId);
		query.setStatus(UserSecurityResetStatus.jumioPending.name());
		List<UserSecurityReset> result = userSecurityResetMapper.getResetList(query);
		return result;
	}
}
