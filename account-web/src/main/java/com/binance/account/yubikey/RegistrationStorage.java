package com.binance.account.yubikey;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.security.UserYubikey;
import com.binance.account.data.mapper.security.YubikeyMapper;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class RegistrationStorage implements CredentialRepository {

    /**
     * 缓存两分钟
     */
    private static final long CACHE_VALIDATE_TIME = 2 * 60;
    private static final String CACHE_VALIDATE_PRE = "webAuthn.validate.%s.%s";
    /**
     * 缓存时间20分钟
     */
    private static final long CACHE_YUBIKEY_TIME = 20 * 60;
    private static final String CACHE_YUBIKEY_PRE = "webAuthn.yubikey.%s.%d";

    private String appType;
    private YubikeyMapper yubikeyMapper;
    private boolean useCache;

    public RegistrationStorage(YubikeyMapper yubikeyMapper, String appType) {
        this(yubikeyMapper, appType, true);
    }

    public RegistrationStorage(YubikeyMapper yubikeyMapper, String appType, boolean useCache) {
        this.yubikeyMapper = yubikeyMapper;
        this.appType = appType;
        this.useCache = useCache;
    }

    //根据用户ID缓存用户的UserYubikey 信息
    private void setCache(final Long userId) {
        if (!useCache) {
            return;
        }
        HintManager hintManager = null;
        try {
            Thread.sleep(700); // 主要防止读写库同步问题
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            List<UserYubikey> list = yubikeyMapper.getByUserId(userId, null, null);
            if (list == null || list.isEmpty()) {
                // 如果是空值，存入一个空列表
                list = new ArrayList<>();
            }
            String key = String.format(CACHE_YUBIKEY_PRE, this.appType, userId);
            String value = JSON.toJSONString(list);
            RedisCacheUtils.set(key, value, CACHE_YUBIKEY_TIME);
        }catch (Exception e) {
            log.error("save user yubikey entry cache error. userId:{}", userId, e);
        }finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
    }

    private List<UserYubikey> getCache(final Long userId) {
        if (!useCache) {
            log.info("will not use cache, userId: {}", userId);
            return yubikeyMapper.getByUserId(userId, null, null);
        }
        // 先从缓存中获取，如果获取到，直接返回，如果获取不到，从数据库中获取后在触发下缓存的存储
        String key = String.format(CACHE_YUBIKEY_PRE, this.appType, userId);
        String value = RedisCacheUtils.get(key, String.class);
        if (StringUtils.isBlank(value)) {
            // 缓存中获取不到，直接从数据库获取
            List<UserYubikey> userYubikeys = yubikeyMapper.getByUserId(userId, null, null);
            setCache(userId);
            return userYubikeys;
        }else {
            return JSON.parseArray(value, UserYubikey.class);
        }
    }

    /**
     * 创建一条记录
     * @param userYubikey
     * @return
     */
    public int save(UserYubikey userYubikey) {
        int count = yubikeyMapper.insert(userYubikey);
        setCache(userYubikey.getUserId());
        return count;
    }

    public List<UserYubikey> getByUserId(Long userId) {
        return getCache(userId);
    }

    /**
     * 根据用户ID 和 域名获取对应的记录
     * @param userId
     * @param origin
     * @return
     */
    public List<UserYubikey> getByOrigin(Long userId, String origin) {
        if (StringUtils.isBlank(origin)) {
            return Collections.emptyList();
        }
        List<UserYubikey> list = getCache(userId);
        if (list != null && !list.isEmpty()) {
            List<UserYubikey> result = list.stream()
                    .filter(item -> StringUtils.equalsIgnoreCase(item.getOrigin(), origin))
                    .collect(Collectors.toList());
            return result;
        } else {
            return yubikeyMapper.getByUserId(userId, origin, null);
        }
    }

    /**
     * 根据用户ID 和 凭证ID 获取对应的记录
     * @param userId
     * @param credentialId
     * @return
     */
    public UserYubikey getByCredentialId(Long userId, String credentialId) {
        if (StringUtils.isBlank(credentialId)) {
            return null;
        }
        List<UserYubikey> list = getCache(userId);
        if (list != null && !list.isEmpty()) {
            Optional<UserYubikey> userYubikey = list.stream()
                    .filter(item -> StringUtils.equalsIgnoreCase(credentialId, item.getCredentialId()))
                    .findFirst();
            if (userYubikey.isPresent()) {
                return userYubikey.get();
            }else {
                return null;
            }
        }else {
            list = yubikeyMapper.getByUserId(userId, null, credentialId);
            if (list == null || list.isEmpty()) {
                return null;
            }
            // credentialId 唯一
            return list.get(0);
        }
    }

    /**
     * 更新验签次数
     * @param userId
     * @param credentialId
     * @param signatureCount
     * @return
     */
    public int updateSignatureCount(Long userId, String credentialId, long signatureCount) {
        UserYubikey userYubikey = new UserYubikey();
        userYubikey.setUserId(userId);
        userYubikey.setCredentialId(credentialId);
        userYubikey.setSignatureCount(signatureCount);
        userYubikey.setUpdateTime(DateUtils.getNewUTCDate());
        int count = yubikeyMapper.updateSignatureCountByCredentialId(userYubikey);
        // 变更后更新缓存
        setCache(userId);
        return count;
    }

    /**
     * 删除一个UserYubikey信息
     * @param userId
     * @param id
     * @return
     */
    public int deleteUserYubikey(Long userId, Long id) {
        int count = yubikeyMapper.deleteByPrimaryKey(id, userId);
        // 变更后更新缓存
        setCache(userId);
        return count;
    }

    /**
     * 验证成功时做一个缓存，方便使用方来查询是否验证成功
     * @param requestId
     * @param validateResult
     */
    public void cacheAuthenticateResult(String requestId, boolean validateResult) {
        String key = String.format(CACHE_VALIDATE_PRE, this.appType, requestId);
        RedisCacheUtils.set(key, validateResult, CACHE_VALIDATE_TIME);
    }

    /**
     * 从缓存中获取校验结果
     * @param requestId
     * @return
     */
    public boolean getAuthenticateResult(String requestId) {
        String key = String.format(CACHE_VALIDATE_PRE, this.appType, requestId);
        Boolean result = RedisCacheUtils.get(key, Boolean.class);
        if (result == null) {
            return false;
        }
        return result;
    }


    // --- 实现接口需要处理的结果集

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return Collections.emptySet();
        }
        Long userId = Long.valueOf(username);
        List<UserYubikey> list = getCache(userId);
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        Set<PublicKeyCredentialDescriptor> setValue = new HashSet<>();
        for (UserYubikey userYubikey : list) {
            try {
                PublicKeyCredentialDescriptor descriptor = PublicKeyCredentialDescriptor.builder()
                        .id(WebAuthnHelper.toByteArray(userYubikey.getCredentialId()))
                        .build();
                setValue.add(descriptor);
            }catch (Exception e) {
                log.error("user yubikey credential_id parse error. userId:{} credentialId:{}", username, userYubikey.getCredentialId(), e);
            }
        }
        return setValue;
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return Optional.empty();
        }
        // user_id 转换到ByteArray就是UserHandle(创建的时候就这样定义，方便转换)
        return Optional.of(new ByteArray(username.getBytes()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        if (userHandle == null || userHandle.isEmpty()) {
            return Optional.empty();
        }
        // 转换到user_id
        try {
            Long userId = WebAuthnHelper.userHanderToUserId(userHandle);
            return Optional.of(userId.toString());
        }catch (Exception e) {
            log.error("userHandler to userId ERROR. userHandler:{}", userHandle);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        if (credentialId == null || credentialId.isEmpty() || userHandle == null || userHandle.isEmpty()) {
            return Optional.empty();
        }
        Long userId = null;
        try {
            userId = WebAuthnHelper.userHanderToUserId(userHandle);
        }catch (Exception e) {
            log.error("userHandler to userId ERROR. userHandler:{}", userHandle);
            return Optional.empty();
        }
        String credentialIdStr = WebAuthnHelper.byteArrayToString(credentialId);
        UserYubikey userYubikey = getByCredentialId(userId, credentialIdStr);
        RegisteredCredential credential = builderRegisteredCredential(userYubikey);
        if (credential == null) {
            return Optional.empty();
        }else {
            return Optional.of(credential);
        }
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        // 主要是防止在注册的时候验证同一个凭证ID只能注册一次的问题， 直接从库查询
        Set<RegisteredCredential> result = new HashSet<>();
        if (credentialId != null && !credentialId.isEmpty()) {
            String credential = WebAuthnHelper.byteArrayToString(credentialId);
            UserYubikey userYubikey = yubikeyMapper.getByCredentialId(credential);
            RegisteredCredential registeredCredential = builderRegisteredCredential(userYubikey);
            if (registeredCredential != null) {
                result.add(registeredCredential);
            }
        }
        return result;
    }

    private RegisteredCredential builderRegisteredCredential(UserYubikey userYubikey) {
        if (userYubikey == null) {
            return null;
        }
        try {
            return WebAuthnHelper.getRegisteredCredential(userYubikey);
        }catch (Exception e) {
            log.error("UserYubikey to RegisteredCredential error. userId:{} credentialId:{}", userYubikey.getUserId(), userYubikey.getCredentialId());
            return null;
        }
    }

    public int updateYubikeySelective(UserYubikey userYubikey) {
        return yubikeyMapper.updateByPrimaryKeySelective(userYubikey);
    }
}
