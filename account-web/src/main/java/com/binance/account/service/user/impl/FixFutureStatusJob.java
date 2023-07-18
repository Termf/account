package com.binance.account.service.user.impl;

import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.account.data.mapper.user.UserInfoMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.log4j.Log4j2;


/**
 * 有些user已经开通了期货账户，但是status中is_exist_future_account是false。修复status
 */
@Log4j2
@JobHandler(value = "fixFutureStatusJob")
@Component
public class FixFutureStatusJob extends IJobHandler {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserIndexMapper userIndexMapper;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try{
            log.info("fixFutureStatusJob start, params={}", s);
            String[] userIdArr = s.split(",");
            log.info("fixFutureStatusJob userId.size={}", userIdArr.length);
            
            for (String userId : userIdArr) {
                log.info("fixFutureStatusJob 开始修复 userId={}", userId);
                final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.valueOf(userId));
                if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
                    return null;
                }
                User user =  userMapper.queryByEmail(userIndex.getEmail());

                UserInfo userInfo = userInfoMapper.selectByPrimaryKey(Long.valueOf(userId));

                // 有future_user_id，且USER_IS_EXIST_FUTURE_ACCOUNT=false
                if (BitUtils.isFalse(user.getStatus(), AccountCommonConstant.USER_IS_EXIST_FUTURE_ACCOUNT) && userInfo.getFutureUserId() != null) {
                    // 修改状态位
                    int num = userMapper.enableStatus(user.getEmail(), AccountCommonConstant.USER_IS_EXIST_FUTURE_ACCOUNT);
                    log.info("fixFutureStatusJob 修复成功 userId={} futureUserId={} num={}", user.getUserId(), userInfo.getFutureUserId(), num);
                }
            }
            log.info("fixFutureStatusJob done");
            return ReturnT.SUCCESS;
        }catch (Exception e){
            log.warn("fixFutureStatusJob exception", e);
            return ReturnT.FAIL;
        }
    }

}
