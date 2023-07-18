package com.binance.account.yubikey;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.security.UserYubikey;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.util.UrlUtils;
import com.binance.account.vo.yubikey.StartAuthenticateResponse;
import com.binance.account.vo.yubikey.StartRegisterReponse;
import com.binance.account.vo.yubikey.UserYubikeyVo;
import com.binance.account.yubikey.entry.AssertionFinishRequest;
import com.binance.account.yubikey.entry.AuthenticateStartResponse;
import com.binance.account.yubikey.entry.RegistrationFinishRequest;
import com.binance.account.yubikey.entry.RegistrationStartResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Lists;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import com.yubico.webauthn.extension.appid.AppId;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Log4j2
public abstract class AbstractWebAuth {

    abstract RelyingParty getRelyingParty(String origin, List<UserYubikey> userYubikeys);

    abstract RegistrationStorage getStorage();

    protected abstract String origins();

    private Set<String> getRelyPartIdSet(String relyPartIds) {
        if (StringUtils.isBlank(relyPartIds)) {
            log.warn("Yubikey WebAuthn 信息未配置，不能启用yubikey功能.");
            return new HashSet<>();
        }
        Set<String> rpSet = new HashSet<>();
        String[] replyPartIdArray = relyPartIds.split(",");
        for (String id : replyPartIdArray) {
            rpSet.add(id);
        }
        return rpSet;
    }

    private RelyingParty builderRelyPartHandler(String rpId, String appId, String origins, String relyPartName) throws Exception {
        // 可信任的域信息
        Set<String> originSet = new HashSet<>();
        if (StringUtils.isBlank(origins)) {
            // 如果没配置，则直接获取appId设置为可信任来源
            originSet.add(appId);
        }else {
            originSet = new HashSet<>(Arrays.asList(origins.split(",")));
        }
        log.info("=====> Yubikey webauthn begin init rp:{} appId:{} origins:{}", rpId, appId, JSON.toJSONString(originSet));
        RelyingPartyIdentity relyingPartyIdentity = RelyingPartyIdentity.builder()
                .id(rpId)
                .name(relyPartName)
                .build();

        RelyingParty rp = RelyingParty.builder()
                .identity(relyingPartyIdentity)
                .credentialRepository(getStorage())
                .appId(new AppId(appId))
                .validateSignatureCounter(true)
                .origins(originSet)
                .allowOriginSubdomain(true)
                .build();

        log.info("=====> Yubikey webauthn end init rp:{} {}", rpId, rp);
        return rp;
    }

    /**
     * 根据配置信息初始化对应域名下的 RelyingParty
     * @param relyPartIds
     * @param origins
     * @param relyPartName
     * @return
     */
    protected Map<String, RelyingParty> init(String relyPartIds, String origins, String relyPartName) {
        Map<String, RelyingParty> rpMap = new HashMap<>();
        Set<String> rpIdSet = getRelyPartIdSet(relyPartIds);
        if (rpIdSet.isEmpty()) {
            return rpMap;
        }
        log.info("======> init yubikey webauthn.");
        try {
            for (String rpId : rpIdSet) {
                // appId 必须时存在证书的域名，否则不可用
                String appId = String.format("https://%s", rpId);
                RelyingParty rp = builderRelyPartHandler(rpId, appId, origins, relyPartName);
                rpMap.put(appId, rp);
            }
            return rpMap;
        }catch (Exception e) {
            log.error("init yubikey webauthn error.", e);
            throw new BusinessException(GeneralCode.SYS_ERROR, "yubikey init fail." + e.getMessage());
        }
    }

    protected void validateIsRegister(Long userId, String origin, String nickname) {
        List<UserYubikey> yubikeys = getStorage().getByOrigin(userId, origin);
        if (yubikeys!= null && !yubikeys.isEmpty()) {
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

    /**
     * 开始初始化注册信息
     * @param userId
     * @param origin
     * @param nickname
     * @return
     */
    public StartRegisterReponse startRegistration(Long userId, String origin, String nickname) {
        RelyingParty rp = getRelyingParty(origin, Collections.emptyList());
        if (rp == null) {
            log.warn("当前域名下未配置Yubikey信息，不能进行注册. origin:{}", origin);
            throw new BusinessException(AccountErrorCode.YUBIKEY_NOT_SUPPORTED_IN_THE_ORIGIN, new Object[]{ origins() });
        }
        // 检查用户是否已经绑定过
        validateIsRegister(userId, origin, nickname);
        try {
            // 进行开始注册
            log.info("user request register yubikey, userId:{} origin:{}", userId, origin);
            UserIdentity userIdentity = WebAuthnHelper.generateUserIdentity(userId, nickname);
            PublicKeyCredentialCreationOptions creationOptions = rp.startRegistration(
                    StartRegistrationOptions.builder()
                            .user(userIdentity)
                            .authenticatorSelection(AuthenticatorSelectionCriteria.builder().requireResidentKey(false).build())
                            .build()
            );

            RegistrationStartResponse startResponse = new RegistrationStartResponse();
            startResponse.setRequestId(WebAuthnHelper.generateRequestId(userId));
            startResponse.setOrigin(origin);
            startResponse.setCredentialNickname(nickname);
            startResponse.setUserId(userId);
            startResponse.setPublicKeyCredentialCreationOptions(creationOptions);

            // 存入缓存，方便在完成注册验证时进行校验
            WebAuthnHelper.cacheRequestStorage(startResponse.getRequestId(), startResponse);
            return new StartRegisterReponse(startResponse.getRequestId(), WebAuthnHelper.toResponse(creationOptions));
        } catch (Exception e) {
            log.error("user request start register yubikey error. userId:{} origin:{}", userId, origin);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }


    /**
     * 完成 Yubikey 注册申请
     * @param userId
     * @param requestStr
     * @return
     */
    public boolean finishRegistration(Long userId, String requestStr) {
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

            // 落库
            saveUserYubikey(yubikey);
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

    protected void saveUserYubikey(UserYubikey userYubikey) {
        getStorage().save(userYubikey);
    }

    /**
     * 申请验证
     * @param userId
     * @param origin
     */
    public StartAuthenticateResponse startAuthenticate(Long userId, String origin) {
        if (userId == null || StringUtils.isBlank(origin)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "origin");
        }
        List<UserYubikey> userYubikeys = getStorage().getByUserId(userId);
//        List<UserYubikey> topLevelDomainYubikeys = getStorage().getByOrigin(userId, "https://" + UrlUtils.getDomainName(origin));
//        List<UserYubikey> userYubikeys = Lists.newArrayList(userYubikeys0);
//        userYubikeys.addAll(topLevelDomainYubikeys);
        if (userYubikeys == null || userYubikeys.isEmpty()) {
            log.info("user is not register. userId:{}, origin:{}", userId, origin);
            throw new BusinessException(AccountErrorCode.YUBIKEY_NOT_REGISTER);
        }
        RelyingParty rp = getRelyingParty(origin, userYubikeys);
        if (rp == null) {
            log.warn("user start authenticate but origin rp is null. userId:{}, origin:{}", userId, origin);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "origin");
        }
        try {
            // 生成进行验证的信息
            AssertionRequest request = rp.startAssertion(
                    StartAssertionOptions.builder()
                            .username(userId.toString())
                            .build()
            );
            AuthenticateStartResponse startResponse = new AuthenticateStartResponse();
            startResponse.setRequestId(WebAuthnHelper.generateRequestId(userId));
            startResponse.setRequest(request);
            startResponse.setOrigin(origin);
            startResponse.setUserId(userId);
            startResponse.setPublicKeyCredentialRequestOptions(request.getPublicKeyCredentialRequestOptions());
            // 缓存请求信息后返回
            WebAuthnHelper.cacheRequestStorage(startResponse.getRequestId(), startResponse);
            return new StartAuthenticateResponse(startResponse.getRequestId(), WebAuthnHelper.toResponse(request.getPublicKeyCredentialRequestOptions()));
        } catch (Exception e) {
            log.error(String.format("user start authenticate error. userId: %s, origin: %s", userId, origin), e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }


    /**
     * 结束验证
     * @param userId
     * @param requestStr
     * @return
     */
    public boolean finishAuthenticate(Long userId, String requestStr, boolean allowDeregister) {
        if (userId == null || StringUtils.isBlank(requestStr)) {
            log.info("finishAuthenticate ILLEGAL_PARAM, userId: {}, requestStr: {}", userId, requestStr);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        try {
            AssertionFinishRequest finishRequest = WebAuthnHelper.jsonMapper.readValue(requestStr, AssertionFinishRequest.class);
            if (finishRequest == null) {
                log.info("finishAuthenticate ILLEGAL_PARAM, userId: {}, requestStr: {}, finishRequest: {}", userId, requestStr, "null");
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            AuthenticateStartResponse startResponse = WebAuthnHelper.getCacheStartResponse(finishRequest.getRequestId(), AuthenticateStartResponse.class);
            if (startResponse == null || !Objects.equals(userId, startResponse.getUserId())) {
                log.info("user request finish authenticate yubikey start info get fail. userId{} requestId:{}", userId, finishRequest.getRequestId());
                throw new BusinessException(AccountErrorCode.YUBIKEY_VERIFY_TIMEOUT);
            }
            String origin = startResponse.getOrigin();

            RelyingParty rp = getRelyingParty(origin, getStorage().getByUserId(userId));
            if (rp == null) {
                log.info("user finish authenticate yubikey but rp is null. userId:{} origin:{}", userId, origin);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "origin");
            }
            log.info("start validate user authenticate info. userId:{}", userId);
            /*
               注意点，在google 浏览器的70.xxx 版本之后，会有个问题就是获取到的 userHandler是空串"", 这时候解析出来的
               userHandler是存在有值的，在对比时会出现不相等的错误，参考：https://github.com/Yubico/java-webauthn-server/issues/12
               解决方案：1. 由前端在请求前判断该参数值，如果是空串则设置为null, 2. 后端判断，在拿到该值时，如果存在并且为空串，
               则设置为不存在的情况, 这时候对比的源会从数据存储中获取该用户的userHandler, 对比参考点：FinishAssertionSteps.Step0 的 userHandler 初始化
             */
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential = finishRequest.getCredential();
            Optional<ByteArray> userHandler = credential.getResponse().getUserHandle();
            if (userHandler.isPresent() && userHandler.get().isEmpty()) {
                AuthenticatorAssertionResponse assertionResponse = credential.getResponse().toBuilder().userHandle(Optional.empty()).build();
                credential = credential.toBuilder().response(assertionResponse).build();
            }
            String credentialId = WebAuthnHelper.byteArrayToString(credential.getId());
            FinishAssertionOptions finishAssertionOptions = FinishAssertionOptions.builder()
                    .request(startResponse.getRequest())
                    .response(credential)
                    .build();
            AssertionResult result = rp.finishAssertion(finishAssertionOptions);
            boolean deregister = finishRequest.isDeregister();
            boolean validateResult = false;
            if (result.isSuccess()) {
                log.info("user finish authenticate success. userId:{} origin:{}", userId, origin);
                if (deregister && allowDeregister) {
                    // 验证成功，并且当前请求是需要解绑，直接把当前用户的绑定信息解除
                    deregisterCredential(userId, credentialId);
                }else {
                    // 验证成功，变更计数次数
                    getStorage().updateSignatureCount(userId, credentialId, result.getSignatureCount());
                }
                validateResult = true;
            }else {
                log.info("user finish authenticate fail. userId:{} origin:{}", userId, origin);
                validateResult = false;
            }
            log.info("验证完成后，对requestId做一段时间的缓存，用于使用方查询是否验证成功. userId:{} requestId:{} validateResult:{}", userId, finishRequest.getRequestId(), validateResult);
            getStorage().cacheAuthenticateResult(finishRequest.getRequestId(), validateResult);
            return validateResult;
        } catch (AssertionFailedException e) {
            log.info(String.format("authenticate yubikey failed due to AssertionFailedException. for userId: %s", userId), e);
            throw new BusinessException(GeneralCode.SYS_VALID);
        } catch (BusinessException e) {
            log.info(String.format("authenticate yubikey failed due to BusinessException. for userId: %s", userId), e);
            throw e;
        } catch (Exception e) {
            log.error(String.format("finish authenticate error: userId: %s", userId), e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    /**
     * 根据userId和凭证ID 删除绑定关系
     * @param userId
     * @param credentialId
     */
    @Transactional
    public void deregisterCredential(Long userId, String credentialId) {
        log.info("deregisterCredential userId:{} credentialId:{}", userId, credentialId);
        UserYubikey userYubikey = getStorage().getByCredentialId(userId, credentialId);
        if (userYubikey == null) {
            log.warn("get user yubikey entry fail by userId:{} credentialId:{}", userId, credentialId);
            throw new BusinessException(AccountErrorCode.YUBIKEY_USER_CREDENTIAL_MISS);
        }
        // 如果存在了，直接进行删除修改操作
        getStorage().deleteUserYubikey(userId, userYubikey.getId());
    }

    /**
     * 获取验证结果，注意该结果只缓存两分钟的有效期
     * @param userId
     * @param requestId
     * @return
     */
    public boolean getAuthenticateResult(Long userId, String requestId) {
        if (userId == null || StringUtils.isBlank(requestId)) {
            return false;
        }
        if (!StringUtils.startsWith(requestId, userId.toString())) {
            // requestId 格式不正确的时候直接返回false
            return false;
        }
        return getStorage().getAuthenticateResult(requestId);
    }

    public List<UserYubikeyVo> getList(Long userId, String origin) {
        List<UserYubikey> userYubikeys;
        if (StringUtils.isBlank(origin)) {
            userYubikeys = getStorage().getByUserId(userId);
        } else {
            userYubikeys = getStorage().getByOrigin(userId, origin);
        }

        List<UserYubikeyVo> result = new ArrayList<>();
        if (userYubikeys != null && !userYubikeys.isEmpty()) {
            for (UserYubikey userYubikey : userYubikeys) {
                UserYubikeyVo vo = new UserYubikeyVo();
                BeanUtils.copyProperties(userYubikey, vo);
                result.add(vo);
            }
        }
        return result;
    }


}
