package com.binance.account.service.async;

import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.domain.bo.AccountMsgNotification;
import com.binance.master.utils.LogMaskUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *  该类存放非mapper、client类普通调用
 */
@Log4j2
@Component
public class UserAsyncTask {

    @Resource
    protected IMsgNotification iMsgNotification;
    @Autowired
    private UserKycApproveMapper userKycApproveMapper;
    @Autowired
    private CountryMapper countryMapper;
    @Autowired
    private UserSecurityMapper userSecurityMapper;
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserMapper userMapper;




    @Async
    public void sendMgsToFrontGroup(String routingKey, String userId, String eventType,String accountType,String tfaType){
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, String.valueOf(userId));
        dataMsg.put("eventTime", new Date().getTime());
        dataMsg.put("eventType",eventType);
        dataMsg.put("accountType",accountType);
        dataMsg.put("tfaType",tfaType);
        AccountMsgNotification msg = new AccountMsgNotification(routingKey,  dataMsg);
        log.info("iMsgNotification userId:{},future register:{}",userId, LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);
    }

    @Async
    public void selectSendRiskMessage(String exchange, String routingKey, Long userId,String email,String deviceInfo, boolean changedPassword,String ip, boolean isCreateAccount) throws Exception {
        log.info("selectSendRiskMessage.exchange{},routingKey:{},userId:{},email:{},deviceInfo:{},changedPassword:{}",exchange,routingKey,userId,email,deviceInfo,changedPassword);
        Map<String, Object> map = new HashMap<>();
        if (null == userId) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        map.put("userId", String.valueOf(userId));
        if (StringUtils.isBlank(email)) {
            final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
            if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            final User user = userMapper.queryByEmail(userIndex.getEmail());
            if (null == user) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            map.put("userEmail", user.getEmail());
        } else {
            map.put("userEmail", email.trim());
        }
        UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
        if (userKycApprove != null) {
            StringBuffer name = new StringBuffer()
                    .append((StringUtils.isBlank(userKycApprove.getCertificateFirstName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateFirstName())) ? ""
                            : userKycApprove.getCertificateFirstName().trim().replaceAll("N/A", ""))
                    .append((StringUtils.isBlank(userKycApprove.getCertificateLastName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateLastName())) ? ""
                            : " " + userKycApprove.getCertificateLastName().trim().replaceAll("N/A", ""));
            map.put("name", name.toString());
        }
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity != null) {
            String mobileCode = userSecurity.getMobileCode();
            if (StringUtils.isNotBlank(mobileCode) && StringUtils.isNotBlank(userSecurity.getMobile())) {
                Country country = countryMapper.selectByPrimaryKey(mobileCode.toUpperCase());
                map.put("phone", "+" + country.getMobileCode() + userSecurity.getMobile());
            }
        }
        map.put("ip", ip);
        map.put("deviceInfo", deviceInfo);
        map.put("changedPassword", changedPassword);
        map.put("type",isCreateAccount?"create_account":"update_account");
        iMsgNotification.sendNotification(exchange, routingKey, map);
    }

}
