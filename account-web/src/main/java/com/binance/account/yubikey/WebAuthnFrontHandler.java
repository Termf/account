package com.binance.account.yubikey;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityCache;
import com.binance.account.data.entity.security.UserYubikey;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.security.UserYubikeyMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.security.impl.UserSecurityBusiness;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.util.UrlUtils;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.security.request.UserLockRequest;
import com.binance.account.vo.yubikey.ActivateYubiKeyRequest;
import com.binance.account.vo.yubikey.DeregisterV2Request;
import com.binance.account.vo.yubikey.DeregisterV3Request;
import com.binance.account.vo.yubikey.FinishRegisterRequest;
import com.binance.account.vo.yubikey.FinishRegisterRequestV2;
import com.binance.account.vo.yubikey.RenameYubikeyRequest;
import com.binance.account.vo.yubikey.StartRegisterReponse;
import com.binance.account.vo.yubikey.UserYubikeyVo;
import com.binance.account.vo.yubikey.WebAuthnListRequest;
import com.binance.account.yubikey.entry.RegistrationFinishRequest;
import com.binance.account.yubikey.entry.RegistrationStartResponse;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
public class WebAuthnFrontHandler extends AbstractWebAuth {


    private static final String REDIS_KEY_USER_YUBIKEY_ACTIVATE_CODE = "yubikey:activate:%s";

    private static final int USER_YUBIKEY_AUTH_FAIL_LIMIT = 5;

    //激活码有效时间30分钟
    private static final long REDIS_KEY_USER_YUBIKEY_ACTIVATE_CODE_EXPIRES_IN_SECONDS = 30L * 60;

    /**
     * 需要与对应的域名段保持一致，多个域名间使用","分割,
     * 注意点：当这个值变更的时候，需要重启服务
     */
    @Value("${yubikey.relypart.id:}")
    private String relyPartIds;

    @Value("${yubikey.relypart.name:Binance_WebAuthn}")
    private String relyPartName;

    @Value("${yubikey.relypart.origins:}")
    private String origins;

    @Value("#{'${yubikey.relypart.origins:}'.split(',')}")
    private Set<String> originSet;

    @Value("${yubikey.max.register.peruser:5}")
    private int maxRegisterPerUser;

    @Value("${spring.profiles.active}")
    private String env;

    private RegistrationStorage registrationStorage;

    @Autowired
    private UserYubikeyMapper yubikeyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCommonBusiness userCommonBusiness;

    @Autowired
    private UserCommonValidateService userCommonValidateService;

    @Autowired
    private UserSecurityBusiness userSecurityBusiness;

    @Autowired
    private UserSecurityMapper userSecurityMapper;

    @Autowired
    private IUserSecurity iUserSecurity;

    /**
     * 根据具体的域名信息配置对应的PR
     * key: app_id
     * value rp
     */
    private Map<String, RelyingParty> rpMap;

    @PostConstruct
    public void init() {
        rpMap = super.init(relyPartIds, origins, relyPartName);
        this.registrationStorage = new RegistrationStorage(yubikeyMapper, "front", false);
    }

    @Override
    RelyingParty getRelyingParty(String origin, List<UserYubikey> userYubikeys) {
        if (CollectionUtils.isEmpty(userYubikeys) || (userYubikeys.size() == 1 && BooleanUtils.isTrue(userYubikeys.get(0).getIsLegacy()))) {
            return rpMap.get(origin);
        } else {
            origin = "https://" + UrlUtils.getDomainName(origin);
            return rpMap.get(origin);
        }

    }

    @Override
    RegistrationStorage getStorage() {
        if (this.registrationStorage == null) {
            synchronized (this) {
                this.registrationStorage = new RegistrationStorage(yubikeyMapper, "front", false);
            }
        }
        return this.registrationStorage;
    }

    @Override
    protected String origins() {
        return origins;
    }

    /**
     * 查询用户的绑定信息列表
     * @param request
     * @return
     */
    public List<UserYubikeyVo> getList(WebAuthnListRequest request) {
        if (request == null || (request.getUserId() == null && StringUtils.isBlank(request.getEmail()))) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        Long userId = request.getUserId();
        String email = request.getEmail();
        if (userId == null) {
            User user = userMapper.queryByEmail(email);
            if (user == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            } else {
                userId = user.getUserId();
            }
        }
        List<UserYubikeyVo> result = super.getList(userId, request.getOrigin());
        if (result != null && !result.isEmpty()) {
            result.stream().forEach(item -> item.setEmail(email));
        }
        return result;
    }

    public List<String> getUserRegisteredOrigins(Long userId) {
        List<UserYubikeyVo> yubikeyVos = super.getList(userId, null);
        if (CollectionUtils.isEmpty(yubikeyVos)) {
            return Collections.emptyList();
        }

        return yubikeyVos.stream().map(key -> key.getOrigin()).collect(Collectors.toList());
    }

    public boolean isOriginSupported(String origin) {
        for (String allowed : originSet) {
            if (UrlUtils.isSameDomainName(origin, allowed)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasUserBound(Long userId) {
        boolean bound = CollectionUtils.isNotEmpty(getStorage().getByUserId(userId));
        log.info("hasUserBound result: {}, user: {}", bound, userId);
        return bound;
    }

    public List<Long> registerdYubikeyUserIds(List<Long> userIds) {
        return yubikeyMapper.findRegisteredUserIds(userIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deregisterForAdmin(Long userId) {
        List<UserYubikey> userYubikeys = getStorage().getByUserId(userId);
        if (CollectionUtils.isEmpty(userYubikeys)) {
            return 0;
        }
        int i = 0;
        for (UserYubikey key : userYubikeys) {
            i += getStorage().deleteUserYubikey(userId, key.getId());
        }
        if (i > 0) {
            markAsDeregistered(userId);
        }
        return i;
    }

    @Override
    protected void validateIsRegister(Long userId, String origin, String nickname) {
        List<UserYubikey> yubikeys = getStorage().getByOrigin(userId, origin);
        if (yubikeys!= null && !yubikeys.isEmpty()) {
            if (yubikeys.size() >= maxRegisterPerUser) {
                throw new BusinessException(AccountErrorCode.YUBIKEY_EXCEED_REGISTER_LIMIT_PER_USER);
            }
            if (StringUtils.isBlank(nickname)) {
                throw new IllegalArgumentException();
            }
            for (UserYubikey yubikey : yubikeys) {
                if (StringUtils.equalsIgnoreCase(nickname, yubikey.getNickName())) {
                    throw new BusinessException(AccountErrorCode.YUBIKEY_ALREADY_EXIST_NICKNAME);
                }
            }
        }
    }

    @Override
    public StartRegisterReponse startRegistration(Long userId, String origin, String nickname) {
        preRegisterCheck(userId, origin);
        if (StringUtils.isBlank(nickname)) {
            nickname = "My key-" + RandomStringUtils.randomAlphanumeric(4);
        }
        return super.startRegistration(userId, origin, nickname);
    }

    private void preRegisterCheck(Long userId, String origin) {
        Boolean isSubUser = userCommonValidateService.isSubUser(userId);
        if (BooleanUtils.isTrue(isSubUser)) {
            throw new BusinessException(AccountErrorCode.SUBUSER_REGISTER_YUBIKEY_NOT_ENABLED);
        }
//        List<UserYubikeyVo> registered = getList(userId, origin);
//        if (CollectionUtils.isNotEmpty(registered)) {
//            throw new BusinessException(AccountErrorCode.AT_MOST_ONE_YUBIKEY_PER_USER_PER_ORIGIN);
//        }
    }

    @Override
    protected void saveUserYubikey(UserYubikey userYubikey) {
        if (userYubikey == null) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        //临时存在redis，等邮件激活再落库.
        String activateCode = StringUtils.getRandom(16);
        String key = String.format(REDIS_KEY_USER_YUBIKEY_ACTIVATE_CODE, activateCode);
        RedisCacheUtils.set(key, JSON.toJSONString(userYubikey), REDIS_KEY_USER_YUBIKEY_ACTIVATE_CODE_EXPIRES_IN_SECONDS);

        //发送激活邮件
        User user = userCommonBusiness.checkAndGetUserById(userYubikey.getUserId());
        userCommonBusiness.sendActivateYubiKeyEmail(user, activateCode, userYubikey.getNickName(), "", "yubikey.register." + userYubikey.getUserId());
        if (StringUtils.endsWithAny(env.toLowerCase(), "dev", "qa")) {
            log.info("yubikey activation email sent to user: {}, origin: {}, activationCode: {}", userYubikey.getUserId(), userYubikey.getOrigin(), activateCode);
        } else {
            log.info("yubikey activation email sent to user: {}, origin: {}", userYubikey.getUserId(), userYubikey.getOrigin());
        }
    }


    public void check2Fa(Long userId, AuthTypeEnum authType, String code) throws Exception {
        User user = userCommonBusiness.checkAndGetUserById(userId);
        //如果用户既没有绑定google auth，也没有绑定mobile，则跳过。
        if (BitUtils.isFalse(user.getStatus(), Constant.USER_GOOGLE) && BitUtils.isFalse(user.getStatus(), Constant.USER_MOBILE)) {
            return;
        }
        if (authType == null || StringUtils.isBlank(code)) {
            throw new BusinessException(GeneralCode.USER_2FA_CODE_ERROR);
        }
        userSecurityBusiness.verificationsTwo(userId, authType, code, true);
    }

    private void handleNickname(FinishRegisterRequest finishRegisterRequest) {
        if (StringUtils.isBlank(finishRegisterRequest.getNickname())) {
            return;
        }

        try {
            RegistrationFinishRequest finishRequest = WebAuthnHelper.jsonMapper.readValue(finishRegisterRequest.getFinishDetail(), RegistrationFinishRequest.class);
            if (finishRequest == null) {
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            String requestId = finishRequest.getRequestId();
            RegistrationStartResponse startResponse = WebAuthnHelper.getCacheStartResponse(requestId, RegistrationStartResponse.class);

            // validate nickname
            validateIsRegister(finishRegisterRequest.getUserId(), startResponse.getOrigin(), finishRegisterRequest.getNickname());

            if (startResponse == null || !Objects.equals(finishRegisterRequest.getUserId(), startResponse.getUserId())) {
                log.warn("user request finish register yubikey start info get fail. userId:{} requestId:{}",
                        finishRegisterRequest.getUserId(), requestId);
                throw new BusinessException(AccountErrorCode.YUBIKEY_VERIFY_TIMEOUT);
            }
            startResponse.setCredentialNickname(finishRegisterRequest.getNickname());
            WebAuthnHelper.cacheRequestStorage(requestId, startResponse);
        } catch (BusinessException e) {
            throw e;
        } catch (JsonParseException | JsonMappingException e) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        } catch (Exception e) {
            log.error(String.format("user request finish register yubikey error. userId: %s", finishRegisterRequest.getUserId()), e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    public boolean finishRegistration(FinishRegisterRequest finishRegisterRequest) throws Exception {
        check2Fa(finishRegisterRequest.getUserId(), finishRegisterRequest.getAuthType(), finishRegisterRequest.getCode());
        handleNickname(finishRegisterRequest);
        return super.finishRegistration(finishRegisterRequest.getUserId(), finishRegisterRequest.getFinishDetail());
    }

    public Long finishRegisterV2(FinishRegisterRequestV2 request) throws Exception {
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(request.getUserId())
                .bizScene(BizSceneEnum.YUBIKEY_SAFE_VERIFY)
                .emailVerifyCode(request.getEmailVerifyCode())
                .googleVerifyCode(request.getGoogleVerifyCode())
                .mobileVerifyCode(request.getMobileVerifyCode())
                .yubikeyVerifyCode(request.getYubikeyVerifyCode())
                .build();
        userSecurityBusiness.verifyMultiFactors(verify);
        FinishRegisterRequest finishRegisterRequest=new FinishRegisterRequest();
        BeanUtils.copyProperties(request,finishRegisterRequest);
        handleNickname(finishRegisterRequest);
        boolean result= finishAndActiveRegistration(finishRegisterRequest.getUserId(), finishRegisterRequest.getFinishDetail());
        if(result){
            return finishRegisterRequest.getUserId();
        }else{
            return null;
        }
    }


    /**
     * 完成 Yubikey 注册申请
     * @param userId
     * @param requestStr
     * @return
     */
    public boolean finishAndActiveRegistration(Long userId, String requestStr) {
        String origin = null;
        try {
            RegistrationFinishRequest finishRequest = WebAuthnHelper.jsonMapper.readValue(requestStr, RegistrationFinishRequest.class);
            if (finishRequest == null) {
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            String requestId = finishRequest.getRequestId();
            RegistrationStartResponse startResponse = WebAuthnHelper.getCacheStartResponse(requestId, RegistrationStartResponse.class);
            if (startResponse == null || !Objects.equals(userId, startResponse.getUserId())) {
                log.warn("user request finish register yubikey start info get fail. userId:{} requestId:{}", userId, requestId);
                throw new BusinessException(AccountErrorCode.YUBIKEY_VERIFY_TIMEOUT);
            }
            origin = startResponse.getOrigin();
            RelyingParty rp = getRelyingParty(origin, Collections.emptyList());
            if (rp == null) {
                log.error("user finish register yubikey but rp is null. userId:{} origin:{}", userId, origin);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "origin");
            }
            log.info("user request finish register userId:{} origin:{}", userId, origin);

            // 开始校验参数签名信息
            RegistrationResult registrationResult = rp.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(startResponse.getPublicKeyCredentialCreationOptions())
                            .response(finishRequest.getCredential())
                            .build()
            );
            log.info("user request finish register yubikey validate success. userId:{} origin:{}", userId, origin);
            UserIdentity userIdentity = startResponse.getPublicKeyCredentialCreationOptions().getUser();
            String nickname = startResponse.getCredentialNickname();
            long signatureCount = finishRequest.getCredential().getResponse().getAttestation().getAuthenticatorData().getSignatureCounter();
            String userHandler = WebAuthnHelper.byteArrayToString(userIdentity.getId());
            String credentialId = WebAuthnHelper.byteArrayToString(registrationResult.getKeyId().getId());
            String publicKey = WebAuthnHelper.byteArrayToString(registrationResult.getPublicKeyCose());

            // 创建一个用户的信息并保存
            UserYubikey yubikey = new UserYubikey();
            yubikey.setUserId(userId);
            yubikey.setNickName(nickname);
            yubikey.setOrigin(origin);
            yubikey.setCredentialId(credentialId);
            yubikey.setUserHandle(userHandler);
            yubikey.setPublicKey(publicKey);
            yubikey.setSignatureCount(signatureCount);
            yubikey.setCreateTime(DateUtils.getNewUTCDate());
            yubikey.setUpdateTime(DateUtils.getNewUTCDate());

            // 已绑定的
            final List<UserYubikey> userReigsteredKeys = getStorage().getByUserId(userId);

            // 落库
            super.saveUserYubikey(yubikey);

            // 只有绑第一个key才打开所有场景验证
            if (CollectionUtils.isEmpty(userReigsteredKeys)) {
                //更新UserSecurity
                UserSecurity userSecurity = new UserSecurity();
                userSecurity.setYubikeyEnabledScenarios(SecurityKeyApplicationScenario.allOn());
                userSecurity.setUserId(yubikey.getUserId());
                userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
            }
            log.info("user request finish register userId:{} updateByPrimaryKeySelective", userId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (JsonParseException | JsonMappingException e) {
            log.warn("finish register parse json error. requestStr=" + requestStr, e);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        } catch (RegistrationFailedException e) {
            log.warn(String.format("user request finish register yubikey fail. userId: %s origin: %s", userId, origin), e);
            return false;
        } catch (Exception e) {
            log.error(String.format("user request finish register yubikey error. userId: %s origin: %s", userId, origin), e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }


    public Long activate(ActivateYubiKeyRequest activateYubiKeyRequest) {
        log.info("start activate by code: {}.", activateYubiKeyRequest == null ? "null" : activateYubiKeyRequest.getActivateCode());
        String key = String.format(REDIS_KEY_USER_YUBIKEY_ACTIVATE_CODE, activateYubiKeyRequest.getActivateCode());

        String userYubikeyJson = RedisCacheUtils.get(key, String.class);
        log.info("Got useryubikey object:{} by code: {}.", userYubikeyJson, activateYubiKeyRequest.getActivateCode());
        if (StringUtils.isBlank(userYubikeyJson)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserYubikey userYubikey;
        try {
            userYubikey = JSON.parseObject(userYubikeyJson, UserYubikey.class);
            if (userYubikey != null) {
                List<UserYubikeyVo> registered = getList(userYubikey.getUserId(), userYubikey.getOrigin());
                if (CollectionUtils.isNotEmpty(registered)) {
                    throw new BusinessException(AccountErrorCode.AT_MOST_ONE_YUBIKEY_PER_USER_PER_ORIGIN);
                }
                userYubikey.setUpdateTime(new Date());
                super.saveUserYubikey(userYubikey);
                //更新UserSecurity
                UserSecurity userSecurity = new UserSecurity();
                userSecurity.setYubikeyEnabledScenarios(SecurityKeyApplicationScenario.allOn());
                userSecurity.setUserId(userYubikey.getUserId());
                userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
                return userYubikey.getUserId();
            } else {
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
        } catch (Exception e) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }


    @Override
    public boolean finishAuthenticate(Long userId, String requestStr, boolean allowDeregister) {
        log.info("start finishAuthenticate userId: {}, env: {}", userId, env);
        userCommonBusiness.checkAndGetUserById(userId);
        //子账户不能绑定Yubikey，也不能验证
        if (userCommonValidateService.isSubUser(userId)) {
            throw new BusinessException(AccountErrorCode.SUBUSER_REGISTER_YUBIKEY_NOT_ENABLED);
        }

        UserSecurityCache userSecurityCache =
                RedisCacheUtils.get(userId.toString(), UserSecurityCache.class, CacheKeys.USER_SECURITY_INFO);
        if (userSecurityCache != null) {
            if (userSecurityCache.getWebAuthnErrorTime() != null
                    && (userSecurityCache.getWebAuthnErrorTime() + TimeUnit.SECONDS.toMillis(3)) > DateUtils
                    .getNewUTCTimeMillis()) {
                throw new BusinessException(AccountErrorCode.USER_U2F_FAILED_EXCEED_LIMIT);
            }
            //错误次数超过限制,且最近一次失败是30分钟内
            if (ObjectUtils.defaultIfNull(userSecurityCache.getWebAuthnErrorCount(), 0) >= USER_YUBIKEY_AUTH_FAIL_LIMIT
                    && userSecurityCache.getWebAuthnErrorTime() != null
                    && userSecurityCache.getWebAuthnErrorTime() + TimeUnit.MINUTES.toMillis(30) > DateUtils.getNewUTCTimeMillis()) {
                throw new BusinessException(AccountErrorCode.USER_U2F_FAILED_EXCEED_LIMIT);
            }
        }

        boolean pass = super.finishAuthenticate(userId, requestStr, allowDeregister);
        if (userSecurityCache == null) {
            userSecurityCache = new UserSecurityCache();
        }
        if (pass) {
            handleAuthSuccess(userSecurityCache);
        } else {
            handleAuthFailure(userId, userSecurityCache);
        }
        RedisCacheUtils.set(userId.toString(), userSecurityCache, 30L * 60, CacheKeys.USER_SECURITY_INFO);
        return pass;
    }

    private void handleAuthSuccess(UserSecurityCache userSecurityCache) {
        userSecurityCache.setWebAuthnErrorTime(null);
        userSecurityCache.setWebAuthnErrorCount(0);
    }

    private void handleAuthFailure(Long userId, UserSecurityCache userSecurityCache) {
        int errCnt = 1 + ObjectUtils.defaultIfNull(userSecurityCache.getWebAuthnErrorCount(), 0);
        if (errCnt >= 5) {
            UserLockRequest userLockRequest = new UserLockRequest();
            userLockRequest.setUserId(userId);
            userLockRequest.setLockEndTime(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(120)));
            log.info("u2f auth failed, lock user: {}", userId);
            userSecurityBusiness.lockUser(APIRequest.instance(userLockRequest));

            userSecurityCache.setWebAuthnErrorTime(null);
            userSecurityCache.setWebAuthnErrorCount(0);
        } else {
            log.info("u2f auth failed {} times, user: {}", errCnt, userId);
            userSecurityCache.setWebAuthnErrorTime(DateUtils.getNewUTCTimeMillis());
            userSecurityCache.setWebAuthnErrorCount(errCnt);
        }
    }

    @Override
    @Transactional
    public void deregisterCredential(Long userId, String credentialId) {
        super.deregisterCredential(userId, credentialId);
        //更新UserSecurity
        markAsDeregistered(userId);
    }

    private void markAsDeregistered(Long userId) {
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setYubikeyEnabledScenarios(0L);
        userSecurity.setDeregisterYubikeyTime(DateUtils.getNewUTCDate());
        userSecurity.setUserId(userId);
        userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
    }

    public boolean renameYubikey(RenameYubikeyRequest request) {
        final UserYubikey yubikey = getStorage().getByCredentialId(request.getUserId(), request.getCredentialId());
        if (yubikey == null) {
            log.info("getByCredentialId returned null, reqeuset: {}", JSON.toJSONString(request));
            throw new BusinessException(AccountErrorCode.YUBIKEY_USER_CREDENTIAL_MISS);
        }

        final List<UserYubikey> yubikeys = getStorage().getByOrigin(request.getUserId(), request.getOrigin());
        yubikeys.forEach(yubi -> {
            if (request.getNickName().equals(yubi.getNickName())) {
                throw new BusinessException(AccountErrorCode.YUBIKEY_DUPLCATE_NICKNAME);
            }
        });

        // rename
        yubikey.setNickName(request.getNickName());
        final int affectedRows = getStorage().updateYubikeySelective(yubikey);

        return affectedRows > 0;
    }

    public APIResponse<Void> deregisterV2(DeregisterV2Request requestBody) throws Exception {
        //2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.DEREGISTER_YUBIKEY)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        iUserSecurity.verifyMultiFactors(verify);

        JSONObject finishDetail;
        try {
            finishDetail = JSON.parseObject(requestBody.getFinishDetail());
        } catch (Exception e) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "finishDetail");
        }
        finishDetail.put("deregister", true);
        boolean success = finishAuthenticate(requestBody.getUserId(), JSON.toJSONString(finishDetail), true);
        if (success) {
            return APIResponse.getOKJsonResult();
        } else {
            return APIResponse.getErrorJsonResult("deregister failed.");
        }
    }

    public APIResponse<Void> deregisterV3(DeregisterV3Request req) throws Exception {
        log.info("deregisterV3 req: {}", JSON.toJSONString(req));
        final List<UserYubikey> yubikeys = getStorage().getByUserId(req.getUserId());
        boolean success = false;
        if (CollectionUtils.isEmpty(yubikeys)) {
            throw new BusinessException(AccountErrorCode.YUBIKEY_NOT_REGISTER);
        } else if (yubikeys.size() == 1) {
            log.info("only one yubikey, use deregisterV2");
            return deregisterV2(req);
        } else {
            log.info("have multiple keys");
            JSONObject finishDetail;
            try {
                finishDetail = JSON.parseObject(req.getFinishDetail());
            } catch (Exception e) {
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "finishDetail");
            }
            finishDetail.put("deregister", false);
            boolean authSuccess = finishAuthenticate(req.getUserId(), JSON.toJSONString(finishDetail), false);
            log.info("auth result: {}", authSuccess);
            if (authSuccess) {
                for (UserYubikey yubikey : yubikeys) {
                    if (yubikey.getId().toString().equals(req.getCredentialId())) {
                        log.info("delete yubikey by credentialId: {}", yubikey.getCredentialId());
                        final int affectedRows = getStorage().deleteUserYubikey(req.getUserId(), yubikey.getId());
                        success = affectedRows > 0;
                    }
                }
            }
            if (success) {
                return APIResponse.getOKJsonResult();
            } else {
                return APIResponse.getErrorJsonResult("deregister failed.");
            }
        }
    }
}
