package com.binance.account.service.security.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.security.UserIpChange;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.UserIpChangeMapper;
import com.binance.account.data.mapper.security.UserSensitiveMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.service.security.IUserIpChange;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.security.request.ConfirmedUserIpChangeRequest;
import com.binance.account.vo.security.response.ConfirmedUserIpChangeResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.RedisVerify;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.google.common.collect.ImmutableMap;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class UserIpChangeBusiness implements IUserIpChange {

    @Resource
    private UserSensitiveMapper userSensitiveMapper;

    @Resource
    private UserIpChangeMapper userIpChangeMapper;

    @Resource
    private UserIpMapper userIpMapper;
    @Autowired
    private UserCommonBusiness userCommonBusiness;

    private static final String PATTERN_IP_AUTH_EMAIL = "USER_LOGIN_IP_AUTH_EMAIL_%s";

    @Override
    @Monitored
    public boolean isHistoryIp(Long userId, String ip) {
        if (userId==null || StringUtils.isBlank(ip)){
            log.warn("UserIpChangeBusiness.isHistoryIp invalid params {}-{}", userId, ip);
            return false;
        }
        Long historyIpCount = this.userIpMapper.queryByCount(userId, ip);
        return historyIpCount > 0L;
    }

    @Override
    @Monitored
    public void sensitiveIpCheck(User user, String ip, AuthTypeEnum authType, String ipChangeConfirmLink,
                                 String customForbiddenLink, boolean isStrictMode) throws Exception {
        log.info("userId:{},authType:{},ip:{}", user.getUserId(), ip);
        /*
         * 1.ip无使用历史，进入下一步
         * 2.authType为空（即未开启2FA验证）或 开启ip严格验证模式，进行ip校验，进入步骤4
         * 3.authType不为空（即开启了2FA验证）且 未开启ip严格验证模式，根据客户类型判断
         *  3.1. 大客户+谷歌验证，还需要ip校验，进入步骤4（预防Google认证被盗的情况）
         *  3.2. 非大客户，不再做ip校验，结束
         * 4.ip验证
         */
        if (this.isHistoryIp(user.getUserId(), ip)) {
            return;
        }
        if (isStrictMode || authType==null){
            checkIpChange(user, ip, ipChangeConfirmLink, customForbiddenLink);
        }else {
            if (isSensitive(user.getUserId()) && Objects.equals(authType, AuthTypeEnum.GOOGLE)){
                checkIpChange(user, ip, ipChangeConfirmLink, customForbiddenLink);
            }
        }
    }

    private void checkIpChange(User user, final String ip, String ipChangeConfirmLink, String customForbiddenLink) throws NoSuchAlgorithmException {
        UserIpChange userIpChange = this.userIpChangeMapper.selectByUserIdAndIp(user.getUserId(), ip);
        if (userIpChange != null && userIpChange.getStatus()) {
            return;
        }
        String cacheKey = String.format(PATTERN_IP_AUTH_EMAIL, user.getUserId());
        RedisVerify verify = RedisCacheUtils.get(cacheKey, RedisVerify.class);
        String disableToken = null;
        // 十分钟最多发一次邮件
        if (verify==null || verify.getTime().before(DateUtils.getNewUTCDateAddMinute(-10))){
            String id = StringUtils.getTimestampRandom32();

            String link = UserCommonBusiness.emailLinkGenerator(ipChangeConfirmLink, String.format("%suser/ipChangeConfirm.html?id={id}&userId={userId}", WebUtils.getHeader(Constant.BASE_URL)),
                    ImmutableMap.of("id", id, "userId", user.getUserId()));
//            String link = String.format("%suser/ipChangeConfirm.html?id=%s&userId=%s", WebUtils.getHeader(Constant.BASE_URL), id, user.getUserId());

            Map<String, Object> data = new HashMap<>();
            data.put("link", link);
            disableToken = userCommonBusiness.sendDisableTokenEmail(Constant.USER_IP_CHANGE_CONFIRM, user, data, "大户保护发送邮件", customForbiddenLink);
            verify = new RedisVerify();
            verify.setToken(id);
            verify.setCode(ip);
            verify.setTime(DateUtils.getNewUTCDate());
            RedisCacheUtils.set(cacheKey, verify, Constant.HOUR_HALF);
        }

        throw new BusinessException(GeneralCode.USER_IP_CHANGE_EMAIL_CONFIRM, disableToken, null);
    }

    @Override
    public APIResponse<ConfirmedUserIpChangeResponse> confirmedUserIpChange(
            APIRequest<ConfirmedUserIpChangeRequest> request) {
        ConfirmedUserIpChangeRequest requestBody = request.getBody();
        String cacheKey = String.format(PATTERN_IP_AUTH_EMAIL, requestBody.getUserId());
        RedisVerify verify = RedisCacheUtils.get(cacheKey, RedisVerify.class);
        if (verify!=null && Objects.equals(verify.getToken(), requestBody.getId())){
            UserIpChange record = new UserIpChange();
            record.setId(requestBody.getId());
            record.setUserId(requestBody.getUserId());
            record.setIp(verify.getCode());
            record.setStatus(true);
            record.setInsertTime(DateUtils.getNewUTCDate());
            record.setUpdateTime(DateUtils.getNewUTCDate());
            int cnt = userIpChangeMapper.insertIgnore(record);
            if (cnt==0){
                log.warn("UserIpChangeMapper.insertIgnore , non-data inserted: {}", JSON.toJSONString(record));
            }
            RedisCacheUtils.del(cacheKey);
            return APIResponse.getOKJsonResult(new ConfirmedUserIpChangeResponse(requestBody.getUserId()));
        }else {
            throw new BusinessException(GeneralCode.USER_IP_CHANGE_EMAIL_CONFIRM_ERROR);
        }
    }

    @Override
    public boolean isSensitive(Long userId) {
        return userSensitiveMapper.selectByPrimaryKey(userId)!=null;
    }

}
