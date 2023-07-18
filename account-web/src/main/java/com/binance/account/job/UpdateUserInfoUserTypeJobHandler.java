package com.binance.account.job;


import com.binance.account.constants.enums.UserTypeEnum;
import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.account.data.entity.user.RootUserIndex;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.agent.UserAgentLogMapper;
import com.binance.account.data.mapper.user.RootUserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Log4j2
@JobHandler(value = "updateUserInfoUserTypeJobHandler")
@Component
public class UpdateUserInfoUserTypeJobHandler extends IJobHandler {

    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RootUserIndexMapper rootUserIndexMapper;

    @Override
    @Trace
    public ReturnT<String> execute(String s) throws Exception {
        TrackingUtils.saveTraceId();
        log.info("updateUserInfoUserTypeJobHandler.start");
        int batchNum = 1;
        if(StringUtils.isNumeric(s)){
            batchNum = Integer.parseInt(s);
        }
        //约束
        if (batchNum > 10){
            batchNum = 10;
        }
        if (batchNum < 1){
            batchNum = 1;
        }
        Map<String, Object> param = new HashMap<>(500);
        param.put("start", 0);
        param.put("offset", 500*batchNum);
        List<UserInfo> userInfos = userInfoMapper.selectUserType0(param);
        log.info("updateUserInfoUserTypeJobHandler.userAgentLogs.size:{}", userInfos == null ? 0 : userInfos.size());
        if (CollectionUtils.isEmpty(userInfos)) {
            return ReturnT.SUCCESS;
        }
        for (UserInfo userInfo : userInfos) {
            User user = userCommonBusiness.getUserByUseryId(userInfo.getUserId());
            if (user != null && user.getStatus() != null) {
                String accountType = UserTypeEnum.getAccountTypeName(user.getStatus());
                if (UserTypeEnum.NORMAL.name().equals(accountType)) {
                    insertInfoRootUserIndex(userInfo.getUserId(), userInfo.getFutureUserId(), UserTypeEnum.FUTURE.name());
                    insertInfoRootUserIndex(userInfo.getUserId(), userInfo.getMarginUserId(), UserTypeEnum.MARGIN.name());
                    insertInfoRootUserIndex(userInfo.getUserId(), userInfo.getFiatUserId(), UserTypeEnum.FIAT.name());
                    insertInfoRootUserIndex(userInfo.getUserId(), userInfo.getCardUserId(), UserTypeEnum.CARD.name());
                    insertInfoRootUserIndex(userInfo.getUserId(), userInfo.getMiningUserId(), UserTypeEnum.MINING.name());
                }
                userInfo.setAccountType(accountType);
                userInfo.setUserId(userInfo.getUserId());
                log.info("updateUserInfoUserTypeJobHandler.accountType:{}", accountType);
                userInfoMapper.updateUserType(userInfo);
            }

        }

        log.info("updateUserInfoUserTypeJobHandler.end");
        return ReturnT.SUCCESS;
    }

    private void insertInfoRootUserIndex(Long rootUserId, Long userId, String userType) {
        if (userId == null) {
            return;
        }
        try {
            RootUserIndex rootUserIndex = new RootUserIndex();
            rootUserIndex.setAccountType(userType);
            rootUserIndex.setRootUserId(rootUserId);
            rootUserIndex.setUserId(userId);
            rootUserIndexMapper.insertSelective(rootUserIndex);
        }catch (Exception e){
            log.error("UpdateUserInfoUserTypeJobHandler.insertInfoRootUserIndex error,rootUserId:{},userId:{},userType:{},exception:{}",rootUserId,userId,userType,e);
        }


    }
}
