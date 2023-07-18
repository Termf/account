package com.binance.account.service.security.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.ResetConst;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.common.query.ResetModularQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.entity.security.UserSecurityResetAnswerLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.security.UserSecurityResetAnswerLogMapper;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.notification.SecurityNotificationService;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.IUserSecurityReset;
import com.binance.account.service.security.filter.IDecisionBeforeEmail;
import com.binance.account.service.security.filter.IUserPostProcessor;
import com.binance.account.service.security.filter.PostResult;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.utils.DeviceCacheUtils;
import com.binance.account.utils.MessageUtils;
import com.binance.account.vo.reset.ResetAnswerLogVo;
import com.binance.account.vo.reset.UserSecurityResetVo;
import com.binance.account.vo.reset.request.ResetAnswerArg;
import com.binance.account.vo.reset.request.ResetApplyTimesArg;
import com.binance.account.vo.reset.request.ResetAuditArg;
import com.binance.account.vo.reset.request.ResetIdArg;
import com.binance.account.vo.reset.request.ResetLastArg;
import com.binance.account.vo.reset.response.ResetAnswerRet;
import com.binance.account.vo.reset.response.ResetApplyTimesRet;
import com.binance.account.vo.reset.response.ResetIdRet;
import com.binance.account.vo.reset.response.ResetLastRet;
import com.binance.account.vo.security.request.UserLockRequest;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.common.enums.JumioError;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.response.InitJumioResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.models.withdraw.OldWithdrawDailyLimitModify;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.WebUtils;
import com.binance.notification.api.vo.SecurityNotificationEnum;
import com.binance.platform.common.TrackingUtils;
import com.google.common.collect.Maps;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author liliang1
 * @date 2018-08-22 20:39
 */
@Log4j2
@Service
public class UserSecurityResetBusiness implements IUserSecurityReset {

    private static final String REVIEW_EMAIL_AUDITMSG = "auditMsg";
    private static final String REVIEW_EMAIL_TYPE = "type";
    private static final String REVIEW_EMAIL_IP = "ip";

    @Resource
    private UserSecurityResetMapper userSecurityResetMapper;
    @Resource
    private UserSecurityResetAnswerLogMapper answerLogMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;
    @Resource
    private IFace iFace;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserCommonBusiness userCommonBusiness;
    @Resource
    private ApolloCommonConfig commonConfig;
    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private UserSecurityMapper userSecurityMapper;
    @Resource
    private UserSecurityBusiness userSecurityBusiness;
    @Resource
    private MessageUtils messageUtils;
    @Resource
    private UserSecurityResetHelper userSecurityResetHelper;
    @Resource
    private IUserFace iUserFace;
    @Resource
    private IUserCertificate iUserCertificate;
    @Resource
    private SecurityNotificationService securityNotificationService;
    @Resource
    private IQuestion iQuestion;
	@Resource
	private IUserPostProcessor iUserPostProcessor;
	@Resource
	private IDecisionBeforeEmail iDecisionBeforeEmail;

    @Override
    public Boolean securityResetIsPending(Long userId, String type) {
        UserSecurityResetType resetType = UserSecurityResetType.getByName(type);
        Integer typeOrdinal = resetType == null ? null : resetType.ordinal();
        UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, typeOrdinal);
        if (reset == null) {
            return Boolean.FALSE;
        }
        if (reset.getStatus() == null) {
            return Boolean.FALSE;
        }
        if (UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
	 * 重置流程的审核描述语国际化语言
	 *
	 * @param message   当msgLocal为true时为i18配置中的key，否则为文本本身返回
	 * @param msgLocal  true获取配置的文本，false直接返回message
	 * @param language
	 * @param msgParams
	 * @return
	 */
    private String getResetAuthMessage(final String message, boolean msgLocal, LanguageEnum language, String... msgParams) {
		String key = StringUtils.isBlank(message) ? "" : message;
        if (msgLocal) {
            // 先从JumioError中取，如果取不到再从ResetConst中取
//            JumioError jumioError = JumioError.getByName(key);
//            if(jumioError != null) {
//            	return LanguageEnum.ZH_CN == language ? jumioError.getCnDesc() : jumioError.getEnDesc();
//            }

        	String result = MessageMapHelper.getMessage(key, language);
        	if(!result.equals(key)) {
        		return result;
        	}
			// 从i18配置文件查询多语言支持
			String msg = messageUtils.getMessage(key, language);
			if (StringUtils.isNotBlank(msg)) {
				return msg;
			}
            //向前兼容，查询硬编码的缓存
            //如果需要对message转换, 则搜索转换信息
            ResetConst.ConverLanguage converLanguage = LanguageEnum.ZH_CN == language ? ResetConst.ConverLanguage.CN : ResetConst.ConverLanguage.EN;
            msg = ResetConst.converResetMessag(key, converLanguage, msgParams);
			if (StringUtils.isNotBlank(msg)) {
				return msg;
			}
			return message;
        }else {
        	return MessageMapHelper.getMessage(key, language);
        }
    }

    /**
     * 发送重置审核通过或拒绝邮件
     *
     * @param status    审核通过或拒绝状态
     * @param reset     重置信息
     * @param message   邮件提示内容
     * @param msgLocal  消息是否需要转换
     * @param msgParams 消息需要转换的替换值
     */
    @Override
    public void sendResetAuthEmail(UserSecurityResetStatus status, UserSecurityReset reset, String message,
                                   boolean msgLocal, String... msgParams) {
        if (UserSecurityResetStatus.passed != status && UserSecurityResetStatus.refused != status) {
            log.info("发送的邮件状态必须是通过或者拒绝的状态.");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (reset == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }

        Long userId = reset.getUserId();
        String transId = reset.getId();
        final User user = userSecurityResetHelper.getUserByUserId(userId);
        LanguageEnum language = LanguageEnum.EN_US;
        String ip = "";
        // 获取用户最后一次登录的IP, 如果是中国登录地址，语言设置为中文
        UserSecurityLog userSecurityLog = userSecurityLogMapper.getLastLoginLogByUserId(userId);
        if (userSecurityLog != null && StringUtils.isNotBlank(userSecurityLog.getIpLocation())) {
            ip = userSecurityLog.getIp();
            String ipLocation = userSecurityLog.getIpLocation();
            if (StringUtils.endsWithIgnoreCase(ipLocation, "China")) {
                language = LanguageEnum.ZH_CN;
            }
        }
        String authMsg = getResetAuthMessage(message, msgLocal, language, msgParams);
        log.info("send reset auth email userId:{} transId:{} authMsg:{} ", userId, transId, authMsg);
        String template = "";
        Map<String, Object> data = Maps.newHashMap();
        data.put(REVIEW_EMAIL_IP, ip);
        switch (reset.getType()) {
            case google:
                data.put(REVIEW_EMAIL_TYPE, LanguageEnum.ZH_CN == language ? "谷歌验证" : "Google Authenticator");
                if (status == UserSecurityResetStatus.refused) {
                    template = Constant.RESET_EMAIL_2FA_FAIL;
                    data.put(REVIEW_EMAIL_AUDITMSG, authMsg);
                } else {
                    template = Constant.RESET_EMAIL_2FA_SUCCESS;
                }
                break;
            case mobile:
                data.put(REVIEW_EMAIL_TYPE, LanguageEnum.ZH_CN == language ? "手机验证" : "SMS Authenticator");
                if (status == UserSecurityResetStatus.refused) {
                    template = Constant.RESET_EMAIL_2FA_FAIL;
                    data.put(REVIEW_EMAIL_AUDITMSG, authMsg);
                } else {
                    template = Constant.RESET_EMAIL_2FA_SUCCESS;
                }
                break;
            case enable:
                if (status == UserSecurityResetStatus.refused) {
                    template = Constant.RESET_EMAIL_ENABLE_FAIL;
                    data.put(REVIEW_EMAIL_AUDITMSG, authMsg);
                } else {
                    template = Constant.RESET_EMAIL_ENABLE_SUCCESS;
                }
                break;
            default:
                throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        userCommonBusiness.sendEmailWithoutRequest(template, user, data, "重置审核:" + reset.getId(), language);
        log.info("重置审核通过或拒绝邮件发送成功. userId:{} transId:{}", userId, transId);
    }

    @Override
    public APIResponse sendResetEndStatusNotifyEmail(String transId, Long userId) {
        if (StringUtils.isBlank(transId)) {
            log.info("重置流程的ID不能为空.");
            return APIResponse.getErrorJsonResult(GeneralCode.ILLEGAL_PARAM);
        }
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (reset == null || !userId.equals(reset.getUserId())) {
            log.info("获取重置记录失败. transId:{} userId:{}", transId, userId);
            return APIResponse.getErrorJsonResult(GeneralCode.ILLEGAL_PARAM);
        }
		sendResetEndNotifyEmail(userId, transId, reset, false);
        return APIResponse.getOKJsonResult();
    }

    /**
     * 重置流程处理结束后，进行发送通知邮件
     *
     * @param userId
     * @param transId
     * @param reset
     */
    private void sendResetEndNotifyEmail(Long userId, String transId, UserSecurityReset reset,boolean hit) {
        if (UserSecurityResetStatus.passed == reset.getStatus()) {
            //通过
            // 同步一下人脸对比照片中把检查照片保存到业务通过的对比照片中
            iFace.saveFaceReferenceRefImage(userId);
			if (!hit) {
				sendResetAuthEmail(UserSecurityResetStatus.passed, reset, null, false);
				log.info("未命中提现规则->发送重置通过通知邮件. userId:{} transId:{}", userId, transId);
			} else {
				sendDecisionEmail(reset);
				log.info("命中提现规则->发送不禁止提币规则时的邮件. userId:{} transId:{}", userId, transId);
			}
        } else if (UserSecurityResetStatus.refused == reset.getStatus()) {
            //拒绝
            String message = "";
            if (StringUtils.isNotBlank(reset.getAuditMsg())) {
                message = reset.getAuditMsg();
            } else {
                message = reset.getFailReason();
            }
            log.info("发送重置拒绝通知邮件. userId:{} transId:{} message:{}", userId, transId, message);
            sendResetAuthEmail(UserSecurityResetStatus.refused, reset, message, true);
        } else {
            log.info("重置流程不是拒绝或者通过状态，不能发送通知邮件. transId:{} userId:{}", transId, userId);
            throw new BusinessException("当前状态不允许发送通知邮件.");
        }
    }

    private void sendDecisionEmail(UserSecurityReset reset) {
        Long userId = reset.getUserId();
        String transId = reset.getId();
        LanguageEnum language = LanguageEnum.EN_US;
        String ip = "";
        // 获取用户最后一次登录的IP, 如果是中国登录地址，语言设置为中文
        UserSecurityLog userSecurityLog = userSecurityLogMapper.getLastLoginLogByUserId(userId);
        if (userSecurityLog != null && StringUtils.isNotBlank(userSecurityLog.getIpLocation())) {
            ip = userSecurityLog.getIp();
            String ipLocation = userSecurityLog.getIpLocation();
            if (StringUtils.endsWithIgnoreCase(ipLocation, "China")) {
                language = LanguageEnum.ZH_CN;
            }
        }
        log.info("未命中禁止提币规则邮件,userId:{},transId:{}", userId, transId);
        Map<String, Object> data = Maps.newHashMap();
        String template = IDecisionBeforeEmail.EMAIL_TEMPLATE_NAME_RESET2FA;
        data.put(REVIEW_EMAIL_IP, ip);
        data.put(REVIEW_EMAIL_AUDITMSG, "");
		switch (reset.getType()) {
		case google:
			data.put(REVIEW_EMAIL_TYPE, LanguageEnum.ZH_CN == language ? "谷歌验证" : "Google Authenticator");
			break;
		case mobile:
			data.put(REVIEW_EMAIL_TYPE, LanguageEnum.ZH_CN == language ? "手机验证" : "SMS Authenticator");
			break;
		case enable:
			template = IDecisionBeforeEmail.EMAIL_TEMPLATE_NAME_ENABLE;
			data.put(REVIEW_EMAIL_TYPE, LanguageEnum.ZH_CN == language ? "解禁账户" : "Unblock Authenticator");
			break;
		default:
			break;
		}
        final User user = userSecurityResetHelper.getUserByUserId(userId);
        userCommonBusiness.sendEmailWithoutRequest(template, user, data, "未命中禁止提币规则邮件:" + reset.getId(), language);
        log.info("未命中禁止提币规则邮件发送成功. userId:{} transId:{}", userId, transId);
	}

	/**
     * 轻质从主库读取
     * @param resetId
     * @return
     */
    @Override
    public UserSecurityReset getFromMasterDbById(String resetId) {
        UserSecurityReset reset = null;
        if (StringUtils.isBlank(resetId)) {
            return null;
        }
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            reset = userSecurityResetMapper.selectByPrimaryKey(resetId);
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
        return reset;
    }

    /**
     * 重置流程自动通过处理
     *
     * @param userId
     * @param transId
     */
    @Override
    public void autoPassResetHandler(final Long userId, final String transId, final String ip, final TerminalEnum terminal) {
        final String track = StringUtils.isNotBlank(TrackingUtils.getTrace()) ? TrackingUtils.getTrace() : TrackingUtils.generateUUID();
        AsyncTaskExecutor.execute(() -> {
            TrackingUtils.saveTrace(track);
            try {
                if (!commonConfig.isResetPassAutoSwitch()) {
                    log.info("人脸识别通过后自动通过重置流程的开关关闭: userId:{} transId:{}", userId, transId);
                    return;
                }
                // 先等待5秒中再处理 强制从主库查询
                Thread.sleep(5000);
                UserSecurityReset reset = getFromMasterDbById(transId);
                // 检查当前记录是否能处于通过状态，JUMIO审核通过且人脸通过
                if(!isCurrentResetCanAutoPass(userId, transId, reset)) {
                    log.info("当前重置流程不能自动审核通过. userId:{} transId:{}", userId, transId);
                    return;
                }
                // 检查证件号是否已经被别的用户占用了，如果被占用了不能通过，直接自动拒绝
                if (StringUtils.isNotBlank(reset.getIdNumber()) && iUserCertificate.isIDNumberOccupied(reset.getIdNumber(), reset.getIssuingCountry(), reset.getDocumentType(), userId)) {
                    log.info("证件号已经被别的用户占用，不能通过，userId:{} transId:{}", userId, transId);
                    resetAuditHandler(userId, transId, UserSecurityResetStatus.refused, JumioError.ID_NUMBER_USED.name(), reset, ip, terminal);
                }else {
                    resetAuditHandler(userId, transId, UserSecurityResetStatus.passed, "自动审核通过", reset, ip, terminal);
                }
            } catch (Exception e) {
                log.warn("人脸识别通过后操作自动通过逻辑失败. userId:{} transId:{}", userId, transId, e);
            } finally {
                TrackingUtils.clearTrace();
            }
        });
    }

    private boolean isCurrentResetCanAutoPass(Long userId, String transId, UserSecurityReset reset) {
        log.info("人脸识别通过后检查是否能自动通过. userId:{} transId:{}", userId, transId);
        if (reset == null || !UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
            return false;
        }
        // 检查JUMIO是否已经通过并且人脸识别是否已经通过
        if (!StringUtils.equalsIgnoreCase(UserSecurityResetStatus.jumioPassed.name(), reset.getJumioStatus())
                || !StringUtils.equalsIgnoreCase(FaceStatus.FACE_PASS.name(), reset.getFaceStatus())) {
            log.info("当前记录的JUMIO 和 人脸是否存在未通过的情况，不能自动通过. userId:{} transId:{}", userId, transId);
            return false;
        }
        // 检查提现备注信息是否为空，如果不为空不能自动通过
        OldWithdrawDailyLimitModify limitModify = userSecurityResetHelper.getOldWithdrawDailyLimitModify(userId);
        if (limitModify != null && StringUtils.isNotBlank(limitModify.getModifyCause())) {
            log.info("当前用户提现备注信息不为空，不能自动通过. userId:{} transId:{}", userId, transId);
            return false;
        }
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfo == null) {
            log.info("获取用户信息失败. userId:{} transId:{}", userId, transId);
            return false;
        }
        if (StringUtils.isNotBlank(userInfo.getRemark())) {
            log.info("用户备注信息不为空，不能自动通过. userId:{} transId:{}", userId, transId);
            return false;
        }
        log.info("用户提现备注和用户备注都为空值，可以自动通过。userId:{} transId:{}", userId, transId);
        return true;
    }

    @Override
    public APIResponse<ResetLastRet> getLastSecurityReset(APIRequest<ResetLastArg> request) {
        ResetLastArg lastArg = request.getBody();
        String email = lastArg.getEmail();
        UserSecurityResetType type = lastArg.getType();
        Long userId = userSecurityResetHelper.getUserIdByEmail(email);
        if (userId == null) {
            log.warn("根据Email查询用户ID失败.");
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, type.ordinal());
        if (reset == null) {
            // 没有申请过重置流程
            return APIResponse.getOKJsonResult();
        } else {
            //只有处于正在处理中的数据才返回，已经到终态的数据不再返回
            if (UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
                ResetLastRet lastRet = new ResetLastRet();
                lastRet.setType(reset.getType());
                // status 的问题：已经上传后但是JUMIO未审核通过，已经开始做人脸识别的情况需要前端确认
                lastRet.setStatus(currentResetViewStatus(reset));
                lastRet.setAuditMsg(reset.getAuditMsg());
                lastRet.setAuditTime(reset.getAuditTime());
                return APIResponse.getOKJsonResult(lastRet);
            } else {
                return APIResponse.getOKJsonResult();
            }
        }
    }

    /**
     * 为了屏蔽后端中把人脸的状态去除的问题，先考虑把正在做人脸识别并且已经上传了JUMIO后的状态并归到人脸正在进行的状态
     * @param reset
     * @return
     */
    private UserSecurityResetStatus currentResetViewStatus(UserSecurityReset reset) {
        if (reset == null) {
            return null;
        }
        if (!UserSecurityResetStatus.isReviewPending(reset.getStatus()) || StringUtils.isAnyBlank(reset.getScanReference(), reset.getFaceStatus())) {
            // 没有开启人脸识别的情况下也直接返回原状态
            return reset.getStatus();
        }
        // 如果已经做过人脸识别的，根据人脸识别的状态返回对应的值
        if (StringUtils.equalsIgnoreCase(FaceStatus.FACE_PASS.name(), reset.getFaceStatus())) {
            return UserSecurityResetStatus.JPFP;
        }else {
            return UserSecurityResetStatus.facePending;
        }
    }

    @Override
    public APIResponse<ResetIdRet> getResetById(APIRequest<ResetIdArg> request) {
        String id = request.getBody().getId();
        log.info("根据重置流程的ID信息获取基础信息: id:{}", id);
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(id);
        if (reset == null) {
            log.info("根据重置流程ID获取不到重置信息. id:{}", id);
            throw new BusinessException(GeneralCode.AC_RESET_EMAIL_EXPIRED);
        }
        Long userId = reset.getUserId();
        ResetIdRet resetIdRet = new ResetIdRet();
        resetIdRet.setId(reset.getId());
        resetIdRet.setQuestionSeq(reset.getQuestionSeq());
        resetIdRet.setType(reset.getType());
        // status 的问题：已经上传后但是JUMIO未审核通过，已经开始做人脸识别的情况需要前端确认
        resetIdRet.setStatus(currentResetViewStatus(reset));
        resetIdRet.setAuditMsg(reset.getAuditMsg());
        resetIdRet.setAuditTime(reset.getAuditTime());
        if (reset.getStatus() == UserSecurityResetStatus.unverified) {
            log.info("重置流程未答题下不需要考虑初始化JUMIO信息. userId:{} id:{}", userId, id);
            return APIResponse.getOKJsonResult(resetIdRet);
        }
        if (!StringUtils.isBlank(reset.getJumioToken())) {
            // 已经初始化过JUMIO的信息，不需要再次进行初始化
            resetIdRet.setJumioUrl(reset.getJumioToken());
            return APIResponse.getOKJsonResult(resetIdRet);
        }
        log.info("未初始化JUMIO信息下需要对JUMIO请求初始化: userId:{} id:{}", userId, id);
        String jumioUrl = resetInitJumio(userId, id, reset);
        resetIdRet.setJumioUrl(jumioUrl);
        return APIResponse.getOKJsonResult(resetIdRet);
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
        InitJumioResponse initJumio = jumioBusiness.initWebJumioWithoutSave(userId, jumioHandlerType, id, false);
        String scanRef = initJumio.getTransactionReference();
        String redirectUrl = initJumio.getRedirectUrl();
        if (StringUtils.isAnyBlank(scanRef, redirectUrl)) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "JUMIO初始化失败");
        }
        log.info("重置流程初始化JUMIO信息成功，userId:{} id:{} scanRef:{}", userId, id, scanRef);
        // 保存初始化的信息
        reset.setScanReference(scanRef);
        reset.setJumioToken(redirectUrl);
        reset.setJumioIp(WebUtils.getRequestIp());
        userSecurityResetMapper.updateByPrimaryKeySelective(reset);
        return redirectUrl;
    }


    @Override
    public APIResponse<Long> sendInitResetEmail(APIRequest<ResetLastArg> request) {
        ResetLastArg arg = request.getBody();
        if (arg == null || arg.getType() == null || StringUtils.isBlank(arg.getEmail())) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String email = arg.getEmail();
        UserSecurityResetType type = arg.getType();
        User user = userMapper.queryByEmail(email);
        if (user == null || user.getUserId() == null) {
            log.warn("根据Email查询用户失败. email:{}", email);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userId = user.getUserId();
        // 验证是否能符合初始化重置2FA的条件
        preCheckInitResetValidate(user, type);
        //通过notification发送通知
        securityNotificationService.saveSecurityNotification(userId, SecurityNotificationEnum.RESET_2FA, request.getLanguage());
        log.info("验证成功后开始初始化重置流程. userId:{} type:{}", userId, type);
        UserSecurityReset reset = createInitResetEntry(userId, type);
        String link = generatorResetInitEmailLinkAndInitStatus(userId, type, reset);
        reset.setUpdateTime(DateUtils.getNewUTCDate());
        userSecurityResetMapper.updateByPrimaryKeySelective(reset);


        // 生成发送邮件信息
        Map<String, Object> emailParams = Maps.newHashMap();
        emailParams.put("link", link);
        LanguageEnum languageEnum = WebUtils.getAPIRequestHeader().getLanguage();
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
        log.info("发送初始化重置流程邮件成功. userId:{} type:{}", userId, type);
        return APIResponse.getOKJsonResult(userId);
    }

    /**
     * 生成初始化重置流程的邮件连接
     *
     * @param userId
     * @param reset
     * @return
     */
    private String generatorResetInitEmailLinkAndInitStatus(Long userId, UserSecurityResetType type, UserSecurityReset reset) {
        log.info("开始生成初始化重置流程的邮件连接. userId:{}, type:{}", userId, type);
        String baseUrl = WebUtils.getHeader(Constant.BASE_URL);
        if (StringUtils.isBlank(baseUrl)) {
            log.info("获取基础域名失败. userId:{} type:{}", userId, type);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (reset.getCertificateType() != null && reset.getCertificateType() > 0) {
            log.info("用户已经KYC认证通过，不需要答题环节. userId:{} type:{}", userId, type);
            // 直接设置到未上传状态，跳过答题环节
            reset.setStatus(UserSecurityResetStatus.unsubmitted);
            return String.format("%suserSecurityReset.html?id=%s&type=%s", baseUrl, reset.getId(), type);
        } else {
            String link = "";
            log.info("用户未KYC认证通过, 检查是否有充值记录来判断是否进入答题环节. userId:{}, type:{}", userId, type);
            // 加一个判断，如果不是解禁账户，根据百分比判断是否可以把整个回答问题环节跳过
            if (!UserSecurityResetType.enable.equals(reset.getType()) && !isNeedCheckQuestion()) {
                log.info("答题环节开关关闭. userId:{} id:{}", userId, reset.getId());
                link = String.format("%suserSecurityReset.html?id=%s&type=%s", baseUrl, reset.getId(), type);
                reset.setStatus(UserSecurityResetStatus.unsubmitted);
            } else {
                String coin = userSecurityResetHelper.getLastUserChargeCoin(userId);
                if (StringUtils.isBlank(coin)) {
                    // 获取不到最后一次的充值记录的币种时直接跳过答题环节
                    link = String.format("%suserSecurityReset.html?id=%s&type=%s", baseUrl, reset.getId(), type);
                    reset.setStatus(UserSecurityResetStatus.unsubmitted);
                } else {
                    // 判断下最后一次充币地址和入金地址是否相同
                    if (userSecurityResetHelper.isAssetSameAddress(userId, coin)) {
                        log.info("存在充值地址和入金地址相同的信息, 进行答题设置. userId:{} type:{} coin:{}", userId, type, coin);
                        // 跳过第一题，直接从第二题开始答题
                        reset.setQuestionSeq(1);
                    }
                    link = String.format("%suserSecurityReset.html?id=%s&coin=%s&type=%s",
                            baseUrl, reset.getId(), coin, type);
                    if (reset.getStatus() == null) {
                        // 如果还没有设置过状态的，直接设置到未答题状态
                        reset.setStatus(UserSecurityResetStatus.unverified);
                    }
                }
            }
            return link;
        }
    }

    private boolean isNeedCheckQuestion() {
        int needQuestionPercent = commonConfig.getResetQuestionPercent();
        if (needQuestionPercent <= 0) {
            return false;
        }
        if (needQuestionPercent >= 100) {
            return true;
        }
        int randNumber = RandomUtils.nextInt(0,100);
        return randNumber < needQuestionPercent;
    }

    /**
     * 初始化重置流程对象信息
     *
     * @param userId
     * @param type
     * @return
     */
    private UserSecurityReset createInitResetEntry(Long userId, UserSecurityResetType type) {
        UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, type.ordinal());
        // 看下是否有老的申请正在处理中的
        if (reset != null && UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
            // 如果已经存在重置2FA记录，状态不是 unverified 和 unsubmitted, 返回提示说：请勿重复提交(上次任务还在处理中)
            if (UserSecurityResetStatus.unverified != reset.getStatus() && UserSecurityResetStatus.unsubmitted != reset.getStatus()) {
                log.info("重置流程上次的申请还在处理中，不能重复提交申请. userId:{} type:{}", userId, type);
                throw new BusinessException(AccountErrorCode.RESET_SUBMIT_RECORD_EXIST);
            }
            // 如果修改时间不为空，且(修改时间+10分钟 > 当前时间)：提示邮件已发送，要30分钟后才能重试
            Date updateTime = reset.getUpdateTime();
            if (updateTime != null && DateUtils.addMinutes(updateTime, 10).compareTo(DateUtils.getNewUTCDate()) > 0) {
                log.info("重置流程最后的变更时间加10分钟大于当前时间，相当于邮件不能发送太频繁. userId:{} type:{}", userId, type);
                throw new BusinessException(AccountErrorCode.RESET_SEND_EMAIL_MINUTE_TIMES);
            }
            // 检查是否已经上传JUMIO结果了，如果已经上传则不能再次申请
            if (StringUtils.isNotBlank(reset.getScanReference())) {
                JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, reset.getScanReference(), reset.getType().name());
                if (jumioInfoVo != null && jumioInfoVo.getStatus() != JumioStatus.INIT) {
                    log.info("重置流程已经再申请处理中，不能同步提交. userId:{} type:{} resetId:{}", userId, reset.getId(), type);
                    throw new BusinessException(AccountErrorCode.RESET_SUBMIT_RECORD_EXIST);
                }
            }
            // 需要验证下，同一个用户同一个操作类型，一天只能发起3次存在JUMIO的重置2FA操作
            Date endTime = DateUtils.getNewUTCDate();
            Date startTime = DateUtils.addDays(endTime, -1);
            Long count = userSecurityResetMapper.getDailyResetWithScanRefTimes(userId, type.ordinal(), startTime, endTime);
            if (count != null && count >= 3) {
                log.info("重置流程同一类型在24小时内只能发起三次. userId:{} type:{}");
                throw new BusinessException(AccountErrorCode.RESET_SUBMIT_DAILY_TIMES);
            }
            //如果老的能通过这些交易，可以重复使用
        } else {
            // 如果待处理的记录不存在的话，直接新加一笔记录
            reset = new UserSecurityReset();
            String id = UUID.randomUUID().toString().replaceAll("-", "");
            reset.setId(id);
            reset.setUserId(userId);
            reset.setCreateTime(DateUtils.getNewUTCDate());
            reset.setUpdateTime(DateUtils.getNewUTCDate());
            reset.setType(type);
            reset.setApplyIp(WebUtils.getRequestIp());

            // 设置KYC的状态信息
            KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
            if (certificateResult != null && certificateResult.getCertificateStatus() != null
                    && KycCertificateResult.STATUS_PASS == certificateResult.getCertificateStatus()) {
                // kyc 已经认证
                if (certificateResult.getCertificateType() != null && KycCertificateResult.TYPE_USER == certificateResult.getCertificateType()) {
                    reset.setCertificateType(KycCertificateResult.TYPE_USER);
                } else if (certificateResult.getCertificateType() != null && KycCertificateResult.TYPE_COMPANY == certificateResult.getCertificateType()) {
                    reset.setCertificateType(KycCertificateResult.TYPE_COMPANY);
                } else {
                    // KYC 未验证
                    reset.setCertificateType(0);
                }
            } else {
                // KYC 未验证
                reset.setCertificateType(0);
            }
            userSecurityResetMapper.insertSelective(reset);
        }
        return reset;
    }

    /**
     * 验证是否符合初始化重置2FA的条件，如果不符合，直接抛出对应的错误信息
     */
    private void preCheckInitResetValidate(User user, UserSecurityResetType type) {
        // 先获取用户的状态信息
        Long userId = user.getUserId();
        UserStatusEx userStatusEx = new UserStatusEx(user.getStatus());
        if (userStatusEx.getIsSubUser()) {
            log.info("子账户禁止使用重置2FA流程. userId:{}", userId);
            throw new BusinessException(AccountErrorCode.SUB_USER_FEATURE_FORBIDDEN);
        }
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            log.info("获取用户安全信息失败. userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        switch (type) {
            case google:
                if (StringUtils.isBlank(userSecurity.getAuthKey())) {
                    log.info("重置GOOGLE但是缺失谷歌认证KEY. userId:{}", userId);
                    throw new BusinessException(AccountErrorCode.USER_NOT_ENABLE_GOOGLE);
                }
                break;
            case mobile:
                if (!userStatusEx.getIsUserMobile() || StringUtils.isBlank(userSecurity.getMobile())) {
                    log.info("重置MOBILE但是未绑定手机信息. userId:{}", userId);
                    throw new BusinessException(AccountErrorCode.USER_NOT_ENABLE_MOBILE);
                }
                break;
            case enable:
                if (!userStatusEx.getIsUserDisabled()) {
                    log.info("用户是否禁用标识表明用户未禁用. userId:{}", userId);
                    throw new BusinessException(AccountErrorCode.USER_NOT_DISABLE);
                }
                // 锁定时间在两小时内的，不能做解禁操作
                Date disableTime = userSecurity.getDisableTime();
                int disableLockHour = commonConfig.getResetEnableDisableLockHour();
                if (disableTime != null && DateUtils.addHours(disableTime, disableLockHour).compareTo(DateUtils.getNewUTCDate()) > 0) {
                    log.info("用户解禁流程不能在禁用两小时内发起解禁流程. userId:{}", userId);
                    throw new BusinessException(AccountErrorCode.USER_CAN_NOT_ENABLE_IN_2HOUR);
                }
                break;
            default:
                log.info("重置流程类型错误. userId:{} type:{}", userId, type);
                throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    @Override
    public APIResponse<ResetAnswerRet> answerQuestion(APIRequest<ResetAnswerArg> request) {
        ResetAnswerArg answerArg = request.getBody();
        ResetAnswerRet answerRet = new ResetAnswerRet();
        String id = answerArg.getId();
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(id);
        if (reset == null || UserSecurityResetStatus.unverified != reset.getStatus()) {
            log.info("获取重置流程信息失败或者当前状态已经不在答题环节. id:{}", id);
            answerRet.setMessage(messageUtils.getMessage(GeneralCode.AC_RESET_EMAIL_EXPIRED));
            answerRet.setStatus("expired");
            answerRet.setSuccess(false);
            answerRet.setUserId(reset == null ? null : reset.getUserId());
            return APIResponse.getOKJsonResult(answerRet);
        }
        Long userId = reset.getUserId();
        //直接加上userId返回
        answerRet.setUserId(userId);
        // 如果用户答题错误次数达到3次，需要验证下当前锁定的时间
        if (reset.getQuestionFailTimes() != null && reset.getQuestionFailTimes() >= 3) {
            log.info("用户答题错误次数大于等于3次，需要验证锁定时间. userId:{} id:{}", userId, id);
            UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            Date lockTime = userSecurity.getLockEndTime();
            if (lockTime != null && lockTime.compareTo(DateUtils.getNewUTCDate()) >= 0) {
                log.info("用户当前锁定时间范围内，答题错误达3次. userId:{} id:{}", userId, id);
                answerRet.setMessage(messageUtils.getMessage(AccountErrorCode.RESET_ANSWER_MANY_TRY_LOCK));
                //返回对应状态方便前端跳转对应错误页
                answerRet.setStatus("manyTryLock");
                answerRet.setSuccess(false);
                return APIResponse.getOKJsonResult(answerRet);
            } else {
                // 设置错误次数未0次
                reset.setQuestionFailTimes(0);
            }
        }
        Integer question = answerArg.getQuestion();
        // 验证下答题顺序是否正确
        if (question == null || !question.equals(reset.getQuestionSeq())) {
            log.info("请求的答题顺序错误. userId:{} id:{} currentSeq:{} requestSeq:{}", userId, id, reset.getQuestionSeq(), question);
            answerRet.setMessage(messageUtils.getMessage(AccountErrorCode.RESET_ANSWER_QUESTION_SEQ_ERROR));
            answerRet.setStatus("questionNotMatch");
            answerRet.setSuccess(false);
            return APIResponse.getOKJsonResult(answerRet);
        }
        answerRet.setStatus(reset.getStatus().name());
        log.info("开始验证答题结果信息。userId:{} id:{} question:{}", userId, id, question);
        if (reset.getQuestionScore() == null || reset.getQuestionScore() == 0) {
            //如果没有得到过分数，计算是否为常用IP进行加分
            userSecurityResetHelper.answerIpScore(userId, id, reset);
        }
        boolean answerResult = false;
        String ignore = answerArg.getIgnore();
        if (StringUtils.isBlank(ignore) || !StringUtils.equals("1", ignore)) {
            // 验证重置流程答题结果
            answerResult = userSecurityResetHelper.answerValidate(userId, id, reset, answerArg);
        }
        log.info("答题判断结果. userId:{} id:{} answerResult:{}", userId, id, answerResult);
        return processAnswerResult(userId, id, question, reset, answerRet, answerResult);
    }

    private APIResponse<ResetAnswerRet> processAnswerResult(Long userId, String id, Integer question, UserSecurityReset reset, ResetAnswerRet answerRet, boolean answerResult) {
        if (answerResult) {
            // 答题正确，得分
            if (reset.getQuestionScore() == null) {
                reset.setQuestionScore(0);
            }
            reset.setQuestionScore(reset.getQuestionScore() + UserSecurityResetHelper.SCORES[question]);
            if (reset.getQuestionScore() >= UserSecurityResetHelper.APPROVE_SCORE) {
                //已经达到需求分数，不再需要答题
                reset.setStatus(UserSecurityResetStatus.unsubmitted);
                answerRet.setPass(true);
            }
            reset.setQuestionSeq(question + 1);
            reset.setUpdateTime(DateUtils.getNewUTCDate());
            userSecurityResetMapper.updateByPrimaryKeySelective(reset);
            answerRet.setSuccess(true);
            return APIResponse.getOKJsonResult(answerRet);
        }
        // 如果答题错误
        log.info("重置流程答题错误. userId:{} id:{} question:{}", userId, id, question);
        answerRet.setSuccess(true);
        reset.setQuestionSeq(question + 1);
        reset.setUpdateTime(DateUtils.getNewUTCDate());
        int resetScore = 0;
        for (int i = question + 1; i < UserSecurityResetHelper.SCORES.length; i++) {
            resetScore += UserSecurityResetHelper.SCORES[i];
        }
        if (reset.getQuestionScore() == null) {
            reset.setQuestionScore(0);
        }
        if (reset.getQuestionScore() + resetScore < UserSecurityResetHelper.APPROVE_SCORE) {
            log.info("就算后续答题全对也没法达到预定分数, 答题失败. userId:{} id:{}", userId, id);
            if (reset.getQuestionFailTimes() == null) {
                reset.setQuestionFailTimes(0);
            }
            int times = reset.getQuestionFailTimes() + 1;
            String coin = userSecurityResetHelper.getLastUserChargeCoin(userId);
            if (StringUtils.isBlank(coin) && userSecurityResetHelper.isAssetSameAddress(userId, coin)) {
                // 跳过第一题，直接从第二题开始答题
                reset.setQuestionSeq(1);
            } else {
                reset.setQuestionSeq(0);
            }
            reset.setQuestionScore(0);
            reset.setQuestionFailTimes(times);
            if (times < 3) {
                answerRet.setMessage(messageUtils.getMessage(AccountErrorCode.RESET_QUESTION_VERIFY_FAIL_TIMES, times));
            } else {
                // 如果错误达到3次，需要锁定2小时不能登录，并且发送通知邮件
                answerManyTimesLockUser(userId, id, reset);
                answerRet.setLock(true);
                answerRet.setMessage(messageUtils.getMessage(AccountErrorCode.RESET_QUESTION_VERIFY_FAIL));
                answerRet.setSuccess(false);
            }
            answerRet.setPass(false);
        }
        userSecurityResetMapper.updateByPrimaryKeySelective(reset);
        return APIResponse.getOKJsonResult(answerRet);
    }

    private void answerManyTimesLockUser(Long userId, String id, UserSecurityReset reset) {
        log.info("用户安全答题错误达到3次进行锁定用户2小时. userId:{} id:{}", userId, id);
        int answerLockHour = commonConfig.getResetAnswerLockHour();
        Date endLock = DateUtils.addHours(DateUtils.getNewUTCDate(), answerLockHour);
        UserLockRequest lockRequest = new UserLockRequest();
        lockRequest.setUserId(userId);
        lockRequest.setLockEndTime(endLock);
        userSecurityBusiness.lockUser(APIRequest.instance(lockRequest));

        // 发送通知邮件
        LanguageEnum languageEnum = WebUtils.getAPIRequestHeader().getLanguage();
        User user = userSecurityResetHelper.getUserByUserId(userId);
        String tplCode = null;
        String remark = null;
        Map<String, Object> params = Maps.newHashMap();
        switch (reset.getType()) {
            case google:
                tplCode = AccountConstants.RESET_QUESTION_VERIFY_FAIL_2FA;
                remark = "重置谷歌认证答题错误邮件";
                if (languageEnum == LanguageEnum.ZH_CN) {
                    params.put("type", "谷歌验证");
                } else {
                    params.put("type", "Google Authenticator");
                }
                break;
            case mobile:
                tplCode = AccountConstants.RESET_QUESTION_VERIFY_FAIL_2FA;
                remark = "重置谷歌认证答题错误邮件";
                if (languageEnum == LanguageEnum.ZH_CN) {
                    params.put("type", "手机验证");
                } else {
                    params.put("type", "SMS Authenticator");
                }
                break;
            case enable:
                tplCode = AccountConstants.RESET_QUESTION_VERIFY_FAIL_ENABLE;
                remark = "用户解禁认证答题错误邮件";
                break;
            default:
                break;
        }
        userCommonBusiness.sendDisableTokenEmail(tplCode, user, params, remark, null);
        log.info("发送重置流程安全答题失败邮件成功. userId:{} id:{}", userId, id);
    }

    @Override
    public APIResponse<?> cancelSecurityReset(APIRequest<ResetLastArg> request) {
        ResetLastArg arg = request.getBody();
        String email = arg.getEmail();
        UserSecurityResetType type = arg.getType();
        if (StringUtils.isBlank(email) || type == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        Long userId = userSecurityResetHelper.getUserIdByEmail(email);
        if (userId == null) {
            log.info("根据Email查询用户ID失败. email:{}", email);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, type.ordinal());
        if (reset == null || !UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
            log.info("根据用户ID和类型获取重置流程信息失败. userId:{}, type:{}", userId, type);
            throw new BusinessException(GeneralCode.AC_RESET_EMAIL_EXPIRED);
        }
        log.info("请求取消重置申请流程. userId:{} id:{}", userId, reset.getId());
        reset.setStatus(UserSecurityResetStatus.cancelled);
        reset.setUpdateTime(DateUtils.getNewUTCDate());
        userSecurityResetMapper.updateByPrimaryKeySelective(reset);
        FaceTransType transType = FaceTransType.getByCode(reset.getType().name());
        iUserFace.endTransFaceLogStatus(userId, reset.getId(), transType, TransFaceLogStatus.FAIL, "用户主动取消重置流程");
        return APIResponse.getOKJsonResult();
    }

    @Override
    public List<ResetAnswerLogVo> getResetAnswerLogs(String resetId) {
        if (StringUtils.isBlank(resetId)) {
            return Collections.emptyList();
        }
        List<UserSecurityResetAnswerLog> answerLogs = answerLogMapper.getByResetId(resetId);
        if (answerLogs == null || answerLogs.isEmpty()) {
            return Collections.emptyList();
        }
        return answerLogs.stream().map(item -> {
            ResetAnswerLogVo vo = new ResetAnswerLogVo();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ResetApplyTimesRet getResetApplyTimes(ResetApplyTimesArg arg) {
        ResetApplyTimesRet ret = new ResetApplyTimesRet();
        if (arg == null || arg.getUserId() == null || arg.getType() == null) {
            return ret;
        }
        Map<String, Long> result = userSecurityResetMapper.getResetApplyTimes(arg.getUserId(), arg.getType().ordinal());
        ;
        if (result == null) {
            return ret;
        }
        long applyTimes = result.get("applyTimes") == null ? 0L : result.get("applyTimes");
        long refuseTimes = result.get("refuseTimes") == null ? 0L : result.get("refuseTimes");
        long successTimes = result.get("successTimes") == null ? 0L : result.get("successTimes");
        ret.setApplyTimes(applyTimes);
        ret.setRefuseTimes(refuseTimes);
        ret.setSuccessTimes(successTimes);
        return ret;
    }

    @Override
    public UserSecurityResetVo getVoById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(id);
        if (reset == null) {
            return null;
        }
        UserSecurityResetVo vo = new UserSecurityResetVo();
        BeanUtils.copyProperties(reset, vo);
        vo.setStatus(reset.getStatus());
        vo.setType(reset.getType());
        return vo;
    }

    @Override
    public APIResponse<?> resetAudit(ResetAuditArg auditArg) {
        log.info("请求审核重置流程信息: auditArg:{}", JSON.toJSONString(auditArg));
        if (auditArg == null || StringUtils.isAnyBlank(auditArg.getId(), auditArg.getAuditMsg())) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserSecurityResetStatus auditStatus = auditArg.getStatus();
        if (auditStatus == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String resetId = auditArg.getId();
        String auditMsg = auditArg.getAuditMsg();
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(resetId);
        if (reset == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        Long userId = reset.getUserId();
        if (auditStatus == UserSecurityResetStatus.passed
                && StringUtils.isNotBlank(reset.getIdNumber()) && iUserCertificate.isIDNumberOccupied(reset.getIdNumber(), reset.getIssuingCountry(), reset.getDocumentType(), userId)) {
            log.info("用户证件号已经被别的用户占用了，不能审核通过. userId:{} transId:{}", userId, resetId);
            throw new BusinessException(GeneralCode.SYS_ERROR, JumioError.ID_NUMBER_USED.getMessage());
        }
        String ip = WebUtils.getRequestIp();
        TerminalEnum terminal = WebUtils.getAPIRequestHeader().getTerminal();
        resetAuditHandler(userId, resetId, auditStatus, auditMsg, reset, ip, terminal);
        return APIResponse.getOKJsonResult();
    }

    /**
     * 重置流程通过或者拒绝的审核处理逻辑
     *
     * @param userId
     * @param reset
     */
    private void resetAuditHandler(Long userId, String resetId, UserSecurityResetStatus auditStatus, String auditMsg, UserSecurityReset reset,
                                   String ip, TerminalEnum terminal) {
        log.info("开始处理重置流程通过或者拒绝的后续处理信息: userId:{} resetId:{} auditStatus:{} auditMsg:{}", userId, resetId, auditStatus, auditMsg);
        if (UserSecurityResetStatus.passed != auditStatus && UserSecurityResetStatus.refused != auditStatus) {
            log.info("审核重置流程的状态是能是审核通过或者拒绝两种状态, resetId:{}", resetId);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (reset == null || !UserSecurityResetStatus.isCanAudit(reset.getStatus())) {
            log.info("获取审核的重置流程失败或者当前流程已经不再审核状态. resetId:{} currentStatus:{}", resetId, reset == null ? null : reset.getStatus());
            throw new BusinessException(GeneralCode.SYS_ERROR, "获取审核记录失败或状态不能审核.");
        }
		// 最终提交reset状态之前最后一次调整用户的reset状态
		PostResult postResult = iUserPostProcessor.postProcess(userId, reset.getType(), auditStatus);
		if (postResult.getStatus() != auditStatus) {
			auditMsg = postResult.getMsgKey();
			auditStatus = postResult.getStatus();
		}
		boolean hit = false;
        if (UserSecurityResetStatus.passed == auditStatus) {
            //审核通过，先校验下当前证件号是否被使用过
            log.info("重置流程审核通过，进行后续信息处理. userId:{} resetId:{}", userId, resetId);
            try {
                userSecurityResetHelper.resetPassHandler(userId, resetId, reset, ip, terminal);
				// RM-426
                hit = iDecisionBeforeEmail.beforeSuccessEmail(userId, resetId);
				log.info("决策系统判断是否命中禁止提现规则. userId:{} resetId:{},hit:{}", userId, resetId, hit);
				if (!hit) {
					userSecurityResetHelper.resetDisableTrading(userId);
					log.info("决策系统判断->未命中时,禁止用户提币，userId:{} resetId:{}", userId, resetId);
				}
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("重置流程审核处理出现未知异常. userId:{} resetId:{}", userId, resetId, e);
                throw new BusinessException(GeneralCode.SYS_ERROR, e.getMessage());
            }
        }
        int statusUpdate = saveResetWhenChanged(auditStatus, auditMsg, reset);
        log.info("重置流程审核保存状态和审核信息：userId{} resetId:{} result:{}", userId, resetId, statusUpdate);
        //发送通知邮件
		sendResetEndNotifyEmail(userId, resetId, reset, hit);
        // 通过或者拒绝后的情况下，同步最终结果到INSPECTOR的JUMIO数据中
        JumioBizStatus bizStatus = reset.getStatus() == UserSecurityResetStatus.passed ? JumioBizStatus.PASSED : JumioBizStatus.REFUSED;
        jumioBusiness.syncJumioBizStatus(userId, reset.getScanReference(), bizStatus);
        // 如果人脸识别流程状态不是终态，则进行修改到终态
        FaceTransType transType = FaceTransType.getByCode(reset.getType().name());
        TransFaceLogStatus faceLogStatus = UserSecurityResetStatus.passed == auditStatus ? TransFaceLogStatus.PASSED : TransFaceLogStatus.FAIL;
        iUserFace.endTransFaceLogStatus(userId, resetId, transType, faceLogStatus, "重置流程审核结束");
        // 删除流程开始缓存的devicepk
        DeviceCacheUtils.delDevicePK(resetId);
        log.info("重置流程审核,最后删除devicepk缓存：userId{} resetId:{} result:{}", userId, resetId, statusUpdate);
    }

	private int saveResetWhenChanged(UserSecurityResetStatus auditStatus, String auditMsg, UserSecurityReset reset) {
		reset.setStatus(auditStatus);
        reset.setAuditMsg(auditMsg);
        reset.setAuditTime(DateUtils.getNewUTCDate());
		if (auditStatus == UserSecurityResetStatus.refused) {
			String failResean = getResetAuthMessage(auditMsg, true, LanguageEnum.ZH_CN);
			if (StringUtils.isNotBlank(failResean) && failResean.length() > 250) {
				failResean = failResean.substring(0, 250);
			}
			reset.setFailReason(failResean);
		}
        reset.setUpdateTime(DateUtils.getNewUTCDate());
        return userSecurityResetMapper.updateByPrimaryKeySelective(reset);
	}

    @Override
    public SearchResult<UserSecurityResetVo> getResetList(ResetModularQuery query) {
        if (query == null) {
            return new SearchResult<>(Collections.emptyList(), 0);
        }
        String email = query.getEmail();
        if (StringUtils.isNotBlank(email)) {
            User user = userMapper.queryByEmail(email);
            if (user == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            } else {
                query.setUserId(user.getUserId());
            }
        }
        List<UserSecurityReset> resetList = userSecurityResetMapper.getResetList(query);
        if (resetList == null || resetList.isEmpty()) {
            return new SearchResult<>(Collections.emptyList(), 0);
        }
        // 转换到VO
        List<UserSecurityResetVo> voList = resetList.stream()
                .map(item -> {
                    UserSecurityResetVo vo = new UserSecurityResetVo();
                    BeanUtils.copyProperties(item, vo);
                    vo.setStatus(item.getStatus());
                    vo.setType(item.getType());
                    List<ResetAnswerLogVo> logVos = this.getResetAnswerLogs(vo.getId());
                    vo.setAnswerLogs(logVos);
                    vo.setAnswerCount(iQuestion.getFlowCurrentAnswerTimes(vo.getUserId(), vo.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
        Set<Long> userIdSet = resetList.stream().map(item -> item.getUserId()).collect(Collectors.toSet());
        List<Long> userIds = new ArrayList<>(userIdSet);
        // 获取email 信息
        List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
        final Map<Long, String> emailMap = Maps.newHashMap();
        userIndices.stream().forEach(item -> emailMap.put(item.getUserId(), item.getEmail()));
        // 用户的备注信息
        List<UserInfo> userInfos = userInfoMapper.selectUserInfoList(userIds);
        final Map<Long, String> userRemarkMap = Maps.newHashMap();
        userInfos.stream().forEach(item -> userRemarkMap.put(item.getUserId(), item.getRemark()));
        // 提现备注信息
        final Map<Long, String> modifyCauseMap = userSecurityResetHelper.oldWithdrawDailyLimitModifyCause(userIds);
        voList.stream().forEach(item -> {
            item.setEmail(emailMap.get(item.getUserId()));
            item.setUserRemark(userRemarkMap.get(item.getUserId()));
            item.setWithdrawModifyCause(modifyCauseMap.get(item.getUserId()));
        });
        SearchResult<UserSecurityResetVo> result = new SearchResult<>();
        result.setRows(voList);
        result.setTotal(userSecurityResetMapper.getResetListCount(query));
        return result;
    }

    @Override
    public List<UserSecurityResetVo> getUserAllReset(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<UserSecurityReset> resetList = userSecurityResetMapper.getUserAllReset(userId);
        if (resetList == null || resetList.isEmpty()) {
            return Collections.emptyList();
        }
        return resetList.stream()
                .map(item -> {
                    UserSecurityResetVo resetVo = new UserSecurityResetVo();
                    BeanUtils.copyProperties(item, resetVo);
                    resetVo.setStatus(item.getStatus());
                    resetVo.setType(item.getType());
                    return resetVo;
                })
                .collect(Collectors.toList());
    }

}
