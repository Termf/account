package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.master.enums.SysType;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.TrackingUtils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步所有子账户的市场手续费
 *
 * @author pcx
 */
@Log4j2
@JobHandler(value = "repairSubAccountUserProductFeeJobHandler")
@Component
public class RepairSubAccountUserProductFeeJobHandler extends IJobHandler {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private IMsgNotification iMsgNotification;
    @Resource
    private SubUserBindingMapper subUserBindingMapper;

    /**
     * userIds:userId集合，用逗号分隔
     */
    @Override
    @Trace
    public ReturnT<String> execute(String param) throws Exception {
        TrackingUtils.saveTraceId();

        StopWatch sw = new StopWatch();
        XxlJobLogger.log("param={0}", param);
        log.info("param={0}", param);
        log.info("start RepairSubAccountUserProductFeeJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            if(StringUtils.isNotBlank(param)){
                String[] userIdArry = param.split(",");
                List<Long> userIdList=Lists.newArrayList();
                for(String userStr:userIdArry){
                    userIdList.add(Long.parseLong(userStr));
                }
                repairSubAccountUserProductFee(userIdList);
            }else{
                repairSubAccountUserProductFee(null);
            }
            return SUCCESS;
        } catch (Exception e) {
            log.error("RepairSubAccountUserProductFeeJobHandler error-->{}", e);
            return FAIL;
        } finally {
            sw.stop();
            log.info("end RepairSubAccountUserProductFeeJobHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
        }
    }

    public void repairSubAccountUserProductFee(List<Long> userIdList) {
        try {
            if(CollectionUtils.isEmpty(userIdList)){
                List<User> userList = userMapper.getAllParentAccount();
                userIdList=Lists.transform(userList, new Function<User, Long>() {
                    @Override
                    public Long apply(@Nullable User user) {
                        return user.getUserId();
                    }
                });
            }
            for (Long parentUserId : userIdList) {
                try {
                    UserIndex parentUserIndex = userIndexMapper.selectByPrimaryKey(parentUserId);
                    if (parentUserIndex == null || StringUtils.isBlank(parentUserIndex.getEmail())) {
                        // 账号不存在
                        log.info("RepairSubAccountUserProductFeeJobHandler.repairSubAccountUserProductFee ,parentUserId={} not exist", parentUserId);
                        continue;
                    }
                    User parentUser = this.userMapper.queryByEmail(parentUserIndex.getEmail());
                    if(null==parentUser){
                        // 账号不存在
                        log.info("RepairSubAccountUserProductFeeJobHandler.repairSubAccountUserProductFee ,parentUserId={} is null", parentUserId);
                        continue;
                    }
                    UserStatusEx userStatusEx=new UserStatusEx(parentUser.getStatus());
                    if (!userStatusEx.getIsSubUserFunctionEnabled().booleanValue()) {
                        log.info("this userId is not patentUser : parentUserId={}", parentUserId);
                        continue;
                    }
                    try {
                        Map<String, Object> dataMsg = new HashMap<>();
                        dataMsg.put("parentUserId", parentUserId);
                        MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE_ONLY_PARENT_MARGIN, dataMsg);
                        log.info("iMsgNotification sendUserProductFeeMsgForMargin:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                        iMsgNotification.send(msg);
                    } catch (Exception e) {
                        log.error("sendUserProductFeeMsgForMargin.send failed:", e);
                    }

                    List<Long> subUserIdList= subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
                    if(CollectionUtils.isEmpty(subUserIdList)){
                        log.info("subUserIdList is empty : parentUserId={}", parentUserId);
                        continue;
                    }
                    for(Long subUserId:subUserIdList){
                        // 同步数据至PNK ADMIN
                        try {
                            Map<String, Object> dataMsg = new HashMap<>();
                            dataMsg.put("parentUserId", parentUserId);
                            dataMsg.put("subUserId", subUserId);
                            MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE, dataMsg);
                            log.info("iMsgNotification sendUserProductFeeMsg:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                            iMsgNotification.send(msg);
                        } catch (Exception e) {
                            log.error("repairSubAccountUserProductFee.send failed:", e);
                        }
                    }

                } catch (Exception e) {
                    log.error("RepairSubAccountUserProductFeeJobHandler,parentUserId=" + parentUserId + ":error", e);
                }
            }
        } catch (Exception e) {
            log.info("RepairSubAccountUserProductFeeJobHandler error-->{}", e);
        }
    }


}
