package com.binance.account.yubikey;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.data.entity.security.UserYubikey;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.io.BaseEncoding;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;

import java.util.UUID;

public class WebAuthnHelper {

    public static final ObjectMapper jsonMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .registerModule(new Jdk8Module());;

    public static <T> JSONObject toResponse(T result) throws Exception {
        return JSON.parseObject(jsonMapper.writeValueAsString(result));
    }

    public static String generateRequestId(Long userId) {
        return String.format("%d.%s", userId, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public static String byteArrayToString(ByteArray byteArray) {
        return byteArray.toJsonString();
    }

    public static ByteArray toByteArray(String param) throws Exception {
        return ByteArray.fromBase64Url(param);
    }

    public static Long userHanderToUserId(ByteArray userHandler) throws Exception {
        String userId = new String(BaseEncoding.base64Url().decode(WebAuthnHelper.byteArrayToString(userHandler)), "UTF-8");
        return Long.valueOf(userId);
    }

    public static UserIdentity generateUserIdentity(Long userId, String nickname) {
        String username = userId.toString();
        UserIdentity userIdentity = UserIdentity.builder()
                .name(username)
                .displayName(nickname)
                .id(new ByteArray(username.getBytes()))
                .build();
        return userIdentity;
    }

    public static RegisteredCredential getRegisteredCredential(UserYubikey userYubikey) throws Exception {
        RegisteredCredential credential = RegisteredCredential.builder()
                .credentialId(toByteArray(userYubikey.getCredentialId()))
                .userHandle(toByteArray(userYubikey.getUserHandle()))
                .publicKeyCose(toByteArray(userYubikey.getPublicKey()))
                .signatureCount(userYubikey.getSignatureCount())
                .build();
        return credential;
    }

    public static <T> void cacheRequestStorage(String requestId, T startResponse) throws Exception {
        String value = jsonMapper.writeValueAsString(startResponse);
        // 缓存30分钟，因为流程改变
        RedisCacheUtils.set(requestId, value, 60*30);
    }

    public static <T> T getCacheStartResponse(String requestId, Class<T> clazz) throws Exception {
        String value = RedisCacheUtils.get(requestId);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return jsonMapper.readValue(value, clazz);
    }
}
