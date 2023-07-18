package com.binance.account.job;

import com.alibaba.fastjson.JSONArray;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.master.utils.BitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.master.error.BusinessException;
import com.binance.master.utils.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.binance.account.constant.AccountCommonConstant;

import lombok.extern.log4j.Log4j2;

import java.util.List;


/**
 * lvt user修复job
 */
@Log4j2
@JobHandler(value = "lvtUserRepairJob")
@Component
public class LVTUserRepairJob extends IJobHandler {

    @Autowired
    private MatchboxApiClient matchboxApiClient;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try{
            log.info("LVTUserRepairJob start");

            List<User> signedLVTUsers = userMapper.selectAllLVTSignedUser();
            log.info("LVTUserRepairJob signedLVTUserIds size={}", signedLVTUsers.size());
            
            for (User user : signedLVTUsers) {
                try {
                    Long userId = user.getUserId();
                    Long status = user.getStatus();
                    log.info("LVTUserRepairJob 开始处理{}", userId);

                    Boolean signed = BitUtils.isTrue(status, AccountCommonConstant.SIGNED_LVT_RISK_AGREEMENT);
                    log.info("LVTUserRepairJob {} signed={}", userId, signed);
                    if (signed) {
                        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
                        matchboxApiClient.putAccountPermission(userInfo.getTradingAccount().toString(), "5");
                        log.info("LVTUserRepairJob 处理成功{}", userId);    
                    } else {
                        log.info("LVTUserRepairJob 处理成功{} signed=false 不需要处理", userId);    
                    }
                    
                } catch (Exception e) {
                    log.error("LVTUserRepairJob 处理失败{} {}", user.getUserId(), e.getMessage());
                }
            }
            
            log.info("LVTUserRepairJob  done");
            return ReturnT.SUCCESS;
        }catch (Exception e){
            log.warn("LVTUserRepairJob job exception", e);
            return ReturnT.FAIL;
        }
    }

}
