package com.binance.account.config;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.binance.account.common.constant.UserConst;
import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValue;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * account自身在apollo上的配置
 *
 * @author libing
 *
 */
@Log4j2
@Configuration
@Getter
public class ApolloCommonConfig {
    @Value("${mainsite.wck.backend.switch:false}")
    private boolean mainsiteWckBackendSwitch;

    // KycWckScreeningJobHandler 线程数目
    @Value("${mainsite.wck.threads.num:2}")
    private Integer wckThreadsNum;
    // user_wck_audit中init状态的数据量大于migrateDataLimit，则MigrateUserKYCApprove2WCKJobHandler不进行迁移操作
    @Value("${wck.migrate.data.init.limit:10000}")
    private Integer migrateDataLimit;

    @Value("${wck.migrate.data.filter.county:}")
    private String filterCountry;
    /**
     * WorldCheck是否使用强规则，如果关闭的话某些规则命中可以跳过
     */
    @Value("${wck.force.strict.rule:false}")
    private boolean wckForceStrictRule;

    /**
     * 当前提现风控需要做人脸功能开关, OFF-关闭 ON-打开
     */
    @Value("${withdraw.face.switch:OFF}")
    private String withdrawFaceSwitch;
    /**
     * 提币人脸标识在人脸识别通过后是否自动打开
     */
    @Value("${withdraw.face.autoPassed.switch:OFF}")
    private String withdrawFaceAutoPassed;
    /**
     * 提币人脸识别通过后是否删除用户风控黑名单开关
     * 0-不调用风控的删除黑名单逻辑
     * 1-走老的风控删除黑名单接口
     * 2-走新的风控按类型删除黑名单接口并且需要带上提币人脸的withdrawID方便风控继续提币流程
     */
    @Value("${withdraw.face.delete.blackList.switch:0}")
    private int withdrawFaceDeleteBlackListSwitch;

    @Value("${withdraw.face.compare.last.hours:-1}")
    private int withdrawFaceLastCompareHours;

    /**
     * 当KYC的检查照片不能操作人脸识别时，是否直接自动拒绝用户的KYC认证
     */
    @Value("${withdraw.face.refImage.autoRefusedKyc.switch:OFF}")
    private String refImageFailAutoRefusedKyc;

    /**
     * 提币人脸识别照片校验次数
     */
    @Value("${withdraw.face.image.valid.limit:3}")
    private Integer withdrawFaceImageValidLimit;

    /**
     * 人脸识别WEB端结果跳转地址
     */
    @Value("${face.web.redirect.path}")
    private String faceWebRedirectPath;
    /**
     * 人脸识别WEB端如果是KYC认证的的需要跳转到KYC的页面地址
     */
    @Value("${face.web.redirect.kyc.path}")
    private String faceWebRedirectKycPath;
    @Value("${face.web.resetEmail.path}")
    private String faceWebRedirectResetEmail;
    /** 中文下的请求域名 */
    @Value("${face.domain.cn}")
    private String faceDomainCn;
    /** 非中文下的请求域名 */
    @Value("${face.domain.en}")
    private String faceDomainEn;
    /** WEB端人脸识别结果回调地址 */
    @Value("${face.web.verify.notify.api}")
    private String faceVerifyNotifyApi;
    /** WEB端人脸识别结果验证跳转地址 */
    @Value("${face.web.verify.return.api}")
    private String faceVerifyReturnApi;
    /** 人脸识别邮件跳转连接API */
    @Value("${face.email.link.api}")
    private String faceEmailLinkApi;
    /** 人脸识别24小时内能操作的次数 */
    @Value("${face.verification.daily.limit}")
    private int faceDailyLimit;
    /** 人脸识别由于某种原因错误次数达到多次次时进入人工审核 */
    @Value("${face.verification.review.limit}")
    private int faceReviewLimit;
    /** 人脸识别在多少小时内未完成验证进入人工审核 */
    @Value("${face.verification.review.hours}")
    private int faceReviewHours;
    /**
     * 重置流程验证保存图片错误重试次数
     */
    @Value("${face.reset.retry.limit:2}")
    private Integer faceResetRetryLimit;
    /** 用户解禁/重置2FA操作人脸识别错误总次数达到时进行拒绝 */
    @Value("${face.reset.fail.count:20}")
    private Integer faceResetFailCount;

    /** JUMIO-KYC认证是否JUMIO审核通过直接通过开关 */
    @Value("${jumio.pass.direct.switch:false}")
    private Boolean jumioPassDirectSwitch;


    /** JUMIO-KYC认证是否JUMIO审核通过直接通过开关 */
    @Value("${face.pass.ocr.switch:true}")
    private boolean facePassOcrSwtich;

    /** KYC 认证是否需要做人脸识别 */
    @Value("${kyc.face.switch:false}")
    private boolean kycFaceSwitch;
    @Value("${kyc.jumio.face.switch:false}")
    private boolean kycJumioFaceSwitch;
    /** KYC 认证如果是OCR流程，是否需要人脸识别 */
    @Value("${kyc.orc.face.switch:false}")
    private boolean kycOrcFaceSwitch;

    /** KYC SDK 的jumio 认证，是否能自动通过开关 */
    @Value("${kyc.sdk.auto.pass.switch:false}")
    private boolean kycSdkAutoPassSwitch;
    /** KYC 认证通过时需要判断的是否属于禁止类国籍，如果时禁止类的，则不能变更用户等级且不能再次提交，但允许做人脸等一些流程 */
    @Value("${kyc.pass.forbid.countries:}")
    private String kycPassForbidCountries;
    /**
     * kyc 通过时是否开启c2c法币账户
     */
    @Value("${kyc.pass.createFiatAccount.switch:false}")
    private boolean kycPassCreateFiatAccount;


    /**
     * 用于配置某些国籍的某些证件类型不需要校验证件的过期时间
     * 规定格式如：CN:ID_CARD,PASSPORT;US:VISA,DRIVING_LICENSE
     */
    @Value("${kyc.jumio.expired.verify.ignore:null}")
    private String kycJumioExpiredIgnore;

    /**
     * KYC 认证是否开启SDK端的验证
     */
    @Value("${kyc.sdk.enable.switch:false}")
    private boolean kycSdkEnableSwitch;

    /**
     * kyc basic age limit, 小于等于0 认为不校验
     */
    @Value("${kyc.base.minAge:-1}")
    private int kycBasicMinAge;

    /**
     * kyc base info 允许提交的次数
     */
    @Value("${kyc.base.submit.count:5}")
    private int kycBaseSubmitCount;
    /**
     * kyc base info 允许提交的次数计算时长，单位分钟
     */
    @Value("${kyc.base.submit.time:5}")
    private int kycBaseSubmitTime;
    /**
     * kyc Address info 允许提交的次数
     */
    @Value("${kyc.address.submit.count:3}")
    private int kycAddressSubmitCount;
    /**
     * kyc Address info 允许提交的次数计算时长，单位分钟
     */
    @Value("${kyc.address.submit.time:5}")
    private int kycAddressSubmitTime;
    /**
     * kyc jumio 24小时内允许的提交次数
     */
    @Value("${kyc.jumio.daily.count:3}")
    private int kycJumioDailyCount;

    /**
     * kyc face_ocr 允许提交的次数
     */
    @Value("${kyc.faceOcr.submit.count:3}")
    private int kycFaceOcrSubmitCount;
    /**
     * kyc face_ocr 允许提交的次数计算时长，单位分钟
     */
    @Value("${kyc.faceOcr.submit.time:10}")
    private int kycFaceOcrSubmitTime;

    /**
     * kyc 基础信息缓存时间，单位秒
     */
    @Value("${kyc.country.cache.times:120}")
    private int kycCountryCacheTimes;



    /** 重置2FA/解禁账户 人脸识别通过后是否能自动 */
    @Value("${reset.pass.auto.switch:false}")
    private boolean resetPassAutoSwitch;
    /** 解禁账户-锁定时间在两小时内的不能做解禁操作 */
    @Value("${reset.enable.disable.hour:2}")
    private int resetEnableDisableLockHour;
    /** 重置2FA/解禁账户 答题错误达到3次后锁定2小时不能再答题 */
    @Value("${reset.answer.lock.hour:2}")
    private int resetAnswerLockHour;
    /** 重置2FA/解禁账户 通过后交易禁用48小时 */
    @Value("${reset.trade.forbid.hour:48}")
    private int resetTradeForbidHour;
    /** 重置2FA/解禁账户是否需要答题环节的百分比，100按原来逻辑跑，0-全部跳过问题环节，中间的按百分比算 */
    @Value("${reset.question.percent:100}")
    private int resetQuestionPercent;
    /**
     * 初始化重置流程的人脸识别时等待预检查的次数
     */
    @Value("${reset.wait.face.check.count:3}")
    private int resetWaitFaceCheckCount;
	/**
	 * 2fa重置回答问题超时阈值,10 mins
	 */
	@Value("${reset.answer.resetTimeOut:10}")
	private int resetAnswerTimeOut;
	/**
	 * 2fa重置创建问题流超时阈值,30 mins
	 */
	@Value("${reset.flow.timeOut:30}")
	private int resetFlowTimeOut;
	/**
	 * 重置2FA请求链超时时间，单位分钟，默认30分钟
	 */
	@Value("${reset.nextStep.expired.time:30}")
	private int resetNextStepExpiredTime;
	/**
	 * 重置上传信息邮件的跳转地址，不能带参数
	 */
	@Value("${reset.upload.email.path:}")
	private String resetUploadEmailPath;

	/**
	 * 2FA重置用户问题选项缓存超时，时间长度，默认30天
	 */
	@Value("${reset.question.options.timeout:30}")
	private int resetQuestionOptionsTimeOut;

	@Value("${reset.enable.auto.refused:false}")
    private boolean resetEnableAutoRefused;

	/**
	 * reset2fa 成功的重定向落地页路径
	 */
	@Value("${reset.redirect.success.path:/question/verify-email}")
    private String resetRedirectSuccessPath;

	/**
	 * reset2fa 失败的重定向落地页路径
	 */
	@Value("${reset.redirect.fail.path:/fail}")
    private String resetRedirectFailPath;

	@Value("${reset.jumio.daily.count:10}")
    private long resetJumioDailyCount;

    /**
	 * 用户答题失败最大限制次数
	 */
    @Value("${protected.reset.device.times:3}")
	private int protectedTimes;

    /**
     * reset2fa 用户重发邮件时间最小间隔，默认5mins
     */
    @Value("${reset.resend.email.timeOut:5}")
	private int resendEmailTimeOut;

	/**
	 * 只要密码和2fa正确即可登录的user id list，不检查设备是否授权和登录ip 格式：“,”分割
	 */
	@Value("${login.user.id.while.list:null}")
	private String loginUserIdWhiteList;

    @Value("${login.add.withdraw.blacklist.switch:null}")
    private String loginAddWithdrawBlackListSwitch;

    /**
     * 通过企业认证,设置为母账户(开启子母账户功能)
     */
    @Value("${auto.enable.subuser.function}")
    private boolean autoEnableSubUserFunction;

    /**
     * 将tradeLevel>=1的普通用户自动设置为母账户
     */
    @Value("${autoEnable.subUserFunction.tradeLevel:false}")
    private boolean autoEnableSubUserFunctionDueToTradeLevel;

    @Value("${secret.code.salt}")
    private String secretCodeSalt;

    @Value("${face.review.switch:true}")
    private boolean faceReviewSwitch;

    //notification推送消息开关，法币暂时不需要notification
    @Value("${notification.push.switch:true}")
    private Boolean nofificationSwitch;

    /**
     * 用户更换邮箱是否需要开启审核中
     */
    @Value("${change.email.review.switch:true}")
    private Boolean  changeEmailReviewSwitch;

    /**
     * 用户更换邮箱需要扫描多少小时之前的流程
     */
    @Value("${change.email.review.hour:24}")
    private int changeEmailReviewHour;


    @Value("${change.email.link.hour:1}")
    private int changeEmailLinkHour;

    @Value("${bigdata.kafka.topic.blacklist:bnbWhiteAndBlackListOperation}")
    private String bigDataKafkaTopicBlackList;

    @Value("${kyc.flow.define.xml:}")
	private String kycFlowFileName;

    /**
     * 是否使能发送 交易相关信息 至第三方渠道。
     *
     * 默认不使能。
     */
    @Value("${account.capital.check.report.enable:0}")
    private String enableReport;

    @Value("${account.face.ocr.switch:false}")
    private boolean faceOcrSwitch;

    /**
     * 用户更换邮箱，点击老邮箱链接开关
     */
    @Value("${email.change.link.old.switch:false}")
    private boolean linkOldEmailLinkSwitch;

    /**
     * 用户更换邮箱，点击新邮箱链接开关
     */
    @Value("${email.change.link.new.switch:false}")
    private boolean linkNewEmailLinkSwitch;

    /**
     * 用户更换邮箱，是否在init开启限制次数开关
     */
    @Value("${email.change.success.count.switch:false}")
    private boolean emailSuccessCountSwitch;


    /**
     * 用户更换邮箱，是否发验证码开关
     */
    @Value("${send.email.captcha.switch:false}")
    private boolean sendEmailCaptchaSwitch;


    /**
     * 用户更换邮箱，在确认新邮箱的新逻辑开关
     */
    @Value("${email.change.confirm.switch:false}")
    private boolean emailChangeConfirmSwitch;


    /**
     * 24h registration no L1 KYC attempt （一次性发送）
     * 72h L1 KYC pass no trading（一次性发送）
     * 开关
     */
    @Value("${kyc.email.notify.user:false}")
    private boolean kycEmailNotifyUser;

    /**
     * 使用新kyc流程
     */
    @Value("${kyc.use.new.flow.switch:false}")
    private boolean kycUseNewFlowSwitch;

    /**
     * 新老流程 db双写
     */
    @Value("${Kyc.flow.double.write:false}")
    private boolean kycFlowDoubleWrite;

    @Value("${us.enable.trade.fee.zero:false}")
    private boolean usEnableTradeFeeZero;

    @Value("${kyc.use.new.flow.threshold:-1}")
    private int kycUseNewFlowThreshold;

    @Value("${kyc.face.sdk.qrcode.valid.second:300}")
    private int qrCodeValidSecond;

    @Value("${kyc.data.migration.run:false}")
    private boolean kycDataMigrationRun;

    @Value("${kyc.data.migration.run.size:5}")
    private int kycDataMigrationRunSize;

    @Value("${kyc.need.2fa.switch:true}")
    private boolean kycNeed2faSwitch;

    /** KYC 认证通过时需要判断的是否属于禁止类国籍，如果时禁止类的，则不能变更用户等级且不能再次提交，但允许做人脸等一些流程 */
    @Value("${checkout.forbid.countries:}")
    private String checkOutForbidCountries;
    @Value("${standardBank.forbid.countries:}")
    private String standardBankForbidCountries;
    @Value("${clearJunction.forbid.countries:}")
    private String clearJunctionForbidCountries;
    @Value("${fourBill.forbid.countries:}")
    private String fourBillForbidCountries;
    @Value("${bcb.forbid.countries:}")
    private String bcbForbidCountries;
    @Value("${qiwi.forbid.countries:}")
    private String qiwiForbidCountries;
    @Value("${worldpay.forbid.countries:}")
    private String worldpayForbidCountries;
    @Value("${flutterwave.ngn.forbid.countries:}")
    private String flutterwaveNGNForbidCountries;
    @Value("${flutterwave.ugx.forbid.countries:}")
    private String flutterwaveUGXForbidCountries;
    @Value("${bau.poli.forbid.countries:}")
    private String bauPoliForbidCountries;
    @Value("${bau.payid.forbid.countries:}")
    private String bauPayIdForbidCountries;
    @Value("${bau.poli.forbid.issue.countries:}")
    private String bauPoliForbidIssueCountries;
    @Value("${bau.payid.forbid.issue.countries:}")
    private String bauPayIdForbidIssueCountries;

    @ApolloJsonValue("${certificate.center.gray:}")
    private Map<String, Integer> certificateCenterGray;
    
    @Value("${certificate.center.gray.users:}")
    private String certificateCenterGrayUsers;
    
    @Value("${certificate.center.gray.switch:false}")
    private boolean certificateCenterGraySwitch;
    
    /**
     * 判断当前提现风控人脸识别开关是否开启
     * @return
     */
    public boolean isWithdrawFaceSwitchOn() {
        return StringUtils.equalsIgnoreCase(UserConst.SWITCH_ON, withdrawFaceSwitch);
    }

    /**
     * 提币人脸标识在人脸识别通过时是否自动标识为通过
     * @return
     */
    public boolean isWithdrawFaceAutoPass() {
        return StringUtils.equalsIgnoreCase(UserConst.SWITCH_ON, withdrawFaceAutoPassed);
    }

    /**
     * 当检查到KYC的认证照片不能操作人脸识别时，是否需要自动拒绝KYC认证让用户重新认证
     * @return
     */
    public boolean isAutoRefusedKyc() {
        return StringUtils.equalsIgnoreCase(UserConst.SWITCH_ON, refImageFailAutoRefusedKyc);
    }

    public boolean isInLoginUserIdWhiteList(Long userId) {
        if (StringUtils.isBlank(loginUserIdWhiteList) || userId == null) {
            return false;
        }

        for (String whiteUserId : loginUserIdWhiteList.split(",")) {
            if (userId.toString().equals(whiteUserId.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据国籍和证件类型，判断是否可以跳过证件过期时间的判断
     * @param countryCode
     * @param documentType
     * @return
     */
    public boolean isKycExpiredDateIgnore(String countryCode, String documentType) {
        String params = this.kycJumioExpiredIgnore;
        if (StringUtils.isAnyBlank(params, countryCode, documentType)) {
            return false;
        }
        try {
            // 解析格式如：CN:ID_CARD,PASSPORT;US:VISA,DRIVING_LICENSE
            String[] countryIgnores = params.split(";");
            for (String cIgnore : countryIgnores) {
                String ignore = cIgnore.toUpperCase();
                if (StringUtils.startsWith(ignore, countryCode.toUpperCase())) {
                    return ignore.indexOf(documentType.toUpperCase()) > 0;
                }
            }
        }catch (Exception e) {
            log.error("kyc 证件过期时间是否忽略参数解析异常。", e);
        }
        return false;
    }


    /**
     *
     * @param privateSalt 私有salt，一般可以为邮箱或者手机号，若无可以用空字符串
     * @param code 需要混淆的code
     * @return
     */
    public String convertSecretCode(String privateSalt,String code){
        return DigestUtils.md5Hex(DigestUtils.md5Hex(privateSalt+code)+secretCodeSalt);
    }

    /**
     * 判断是否属于kyc 通过时需要校验的禁止国籍
     * @param countryCode
     * @return
     */
    public boolean isKycPassForbidCountry(String countryCode) {
        if (StringUtils.isBlank(countryCode) || StringUtils.isBlank(this.kycPassForbidCountries)) {
            return false;
        }
        String[] forbidCountries = this.kycPassForbidCountries.split(",");
        for (String forbid : forbidCountries) {
            if (StringUtils.equalsIgnoreCase(forbid, countryCode)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> kycForbidCountry() {
        Set<String> result = new HashSet<>();
        if (StringUtils.isBlank(this.kycPassForbidCountries)) {
            return result;
        }
        String[] forbidCountries = this.kycPassForbidCountries.split(",");
        for (String code : forbidCountries){
            result.add(code.toUpperCase());
        }
        return result;

    }
    
    public boolean isForbidCountry(String countryCode, String channelCode) {
        UserRiskRatingChannelCode code = UserRiskRatingChannelCode.getByCode(channelCode);
        String forbidCountriesCfg = null;
        switch (code) {
            case CHECKOUT:
                forbidCountriesCfg = checkOutForbidCountries;
                break;
            case StandardBank:
                forbidCountriesCfg = standardBankForbidCountries;
                break;
            case ClearJunction:
                forbidCountriesCfg = clearJunctionForbidCountries;
                break;
            case FourBill:
                forbidCountriesCfg = fourBillForbidCountries;
                break;
            case BCB:
                forbidCountriesCfg = bcbForbidCountries;
                break;
            case Qiwi:
                forbidCountriesCfg = qiwiForbidCountries;
                break;
            case worldpay:
                forbidCountriesCfg = worldpayForbidCountries;
                break;
            case FlutterwaveNGN:
                forbidCountriesCfg = flutterwaveNGNForbidCountries;
                break;
            case FlutterwaveUGX:
                forbidCountriesCfg = flutterwaveUGXForbidCountries;
                break;
            case BauPoli:
                forbidCountriesCfg = bauPoliForbidCountries;
                break;
            case BauPayId:
                forbidCountriesCfg = bauPayIdForbidCountries;
                break;
        }
    	if (StringUtils.isBlank(countryCode) || StringUtils.isBlank(forbidCountriesCfg)) {
            return false;
        }
    	String[] forbidCountries = forbidCountriesCfg.split(",");
    	for (String forbid : forbidCountries) {
            if (StringUtils.equalsIgnoreCase(forbid, countryCode)) {
                return true;
            }
        }
        return false;
    }
}
