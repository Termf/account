package com.binance.account.job;


import com.binance.account.constants.enums.UserTypeEnum;
import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.agent.UserAgentLogMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangyang on 2019/6/17.
 */
@Log4j2
@JobHandler(value = "updateUserAgentLogUserTypeJobHandler")
@Component
public class UpdateUserAgentLogUserTypeJobHandler extends IJobHandler{

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserAgentLogMapper userAgentLogMapper;
    @Autowired
    private UserCommonBusiness userCommonBusiness;

    @Override
    @Trace
    public ReturnT<String> execute(String s) throws Exception {
        TrackingUtils.saveTraceId();
        log.info("updateUserAgentLogUserTypeJobHandler.start");
        long total = userAgentLogMapper.countUserType0();
        if (total <= 0){
            log.info("updateUserAgentLogUserTypeJobHandler.countUserType0 is 0");
            return ReturnT.SUCCESS;
        }
        long batchNum = (total%500==0)?total/500:((total/500)+1);
        log.info("UpdateUserAgentLogUserTypeJobHandler.batchNum:{}",batchNum);
        for (int i=0;i<batchNum;i++){
            log.info("UpdateUserAgentLogUserTypeJobHandler.executor:{}",batchNum);
            Map<String,Object> param = new HashMap<>(500);
            param.put("start",i*500);
            if (i == batchNum-1){
                param.put("offset",total-(i*500));
            }else{
                param.put("offset",500);
            }
            List<UserAgentLog> userAgentLogs = userAgentLogMapper.selectUserType0(param);
            log.info("UpdateUserAgentLogUserTypeJobHandler.userAgentLogs.size:{}",userAgentLogs == null?0:userAgentLogs.size());
            for (UserAgentLog userAgentLog:userAgentLogs){
                User user = userMapper.queryByEmail(userAgentLog.getReferralEmail());
                if (user == null){
                    //如果用户的email变更了，则通过userId来获取
                     user = userCommonBusiness.getUserByUseryId(userAgentLog.getReferralUser());
                }
                if (user != null && user.getStatus() != null){
                    Integer accountType = UserTypeEnum.getAccountType(user.getStatus());
                    userAgentLog.setUserType(accountType);
                    log.info("UpdateUserAgentLogUserTypeJobHandler.accountType:{}",accountType);
                    userAgentLogMapper.updateUserType(userAgentLog);
                }

            }

        }
        log.info("updateUserAgentLogUserTypeJobHandler.end");
        return ReturnT.SUCCESS;
    }
}
