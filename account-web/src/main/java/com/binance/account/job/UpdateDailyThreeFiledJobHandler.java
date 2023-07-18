package com.binance.account.job;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.master.constant.Constant;
import com.binance.master.old.data.account.OldUserDataMapper;
import com.binance.master.old.models.account.OldUserData;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xxl.job.core.biz.model.ReturnT.FAIL;
import static com.xxl.job.core.handler.IJobHandler.SUCCESS;

/**
 * Created by yangyang on 2019/6/17.
 */
@Log4j2
@JobHandler(value = "updateDailyThreeFiledJobHandler")
@Component
public class UpdateDailyThreeFiledJobHandler extends IJobHandler{

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private OldUserDataMapper oldUserDataMapper;

    @Override
    @Trace
    public ReturnT<String> execute(String s) throws Exception {
        TrackingUtils.saveTraceId();
        log.info("updateDailyThreeFiledJobHandler.execute.countJobDailyWithdrawCap.start");
        Long defineTotal = null;
        //如果s是有值，同时不以T开头，则为userId
        if (StringUtils.isNotBlank(s) && !s.startsWith("T")){
            if (!StringUtils.isNumeric(s)){
                return FAIL;
            }
            Long userId = Long.parseLong(s);
            Map<String,Object> params = new HashMap<>();
            params.put("start",0);
            params.put("offset",1);
            params.put("userId",userId);
            List<UserInfo> userInfoList = userInfoMapper.selectJobDailyThreeField(params);
            if (CollectionUtils.isEmpty(userInfoList) || userInfoList.size() != 1){
                return FAIL;
            }
            doSeparateUpdateThreeField(userInfoList);
            return SUCCESS;
        }else if (StringUtils.isNotBlank(s) && s.startsWith("T") && StringUtils.isNumeric(s.substring(1))){
            //如果s有值，同时以T开头，则为total
            defineTotal = Long.parseLong(s.substring(1));
        }

        Long total = userInfoMapper.countJobDailyThreeField();
        if (total == null || total == 0){
            log.info("updateDailyThreeFiledJobHandler.execute.countJobDailyThreeField.result.total is null");
            return SUCCESS;
        }
        if (total <= 500){
            doUpdateWithDrawCapSetNull(0L, total);
            return SUCCESS;
        }
        if (total > 10000){
            //每次运行，只修改10000条
            if (defineTotal != null){
                total = defineTotal;
            }else{
                total = 10000L;
            }

        }
        Long len = total%500==0?total/500:(total/500+1);
        for (int i=0;i<len;i++){
            if ((i+1)*500 <= total){
                doUpdateWithDrawCapSetNull(i*500L,500L);
            }else{
                doUpdateWithDrawCapSetNull(i*500L,total-i*500+1);
            }
        }
        log.info("updateDailyThreeFiledJobHandler.execute.countJobDailyWithdrawCap.end");
        return SUCCESS;
    }

    private void doUpdateWithDrawCapSetNull(Long start, Long total) {
        Map<String,Object> params = new HashMap<>(2);
        params.put("start",start);
        params.put("offset",total);
        List<UserInfo> userInfoList = userInfoMapper.selectJobDailyThreeField(params);
        log.info("updateDailyThreeFiledJobHandler.execute.selectJobDailyThreeField.result:{}",userInfoList);
        if (CollectionUtils.isEmpty(userInfoList)){
            log.info("updateDailyThreeFiledJobHandler.execute.selectJobDailyThreeField.result is null");
            return;
        }
        //1.都为0， 查询都为空--这种最多，单独拉出来处理
        List<Long> allZeroUserIds = getAllZeroUserIds(userInfoList);
        if (CollectionUtils.isNotEmpty(allZeroUserIds)){
            doUpdateThreeFieldAllNullByUserIds(allZeroUserIds);
        }
        //分开更新三个字段
        doSeparateUpdateThreeField(userInfoList);


    }

    private void doSeparateUpdateThreeField(List<UserInfo> userInfoList) {
        //2.DailyWithdrawCap为0，则查询DailyWithdrawCap为为空的
        List<Long> zeroDailyWithdrawCapList = getZeroDailyWithdrawCap(userInfoList);
        if (CollectionUtils.isNotEmpty(zeroDailyWithdrawCapList)){
            List<OldUserData> oldUserDatas = getOldUserDatas(zeroDailyWithdrawCapList);
            if (CollectionUtils.isNotEmpty(oldUserDatas)){
                getAndupdateDailywithDrawCap(oldUserDatas);
            }
        }

        //3.DailyWithdrawCountLimit为0，则转空
        List<Long> zeroDailyWithdrawCountLimitList = getZeroDailyWithdrawCountLimit(userInfoList);
        if (CollectionUtils.isNotEmpty(zeroDailyWithdrawCountLimitList)){
            List<OldUserData> oldUserDatas = getOldUserDatas(zeroDailyWithdrawCountLimitList);
            if (CollectionUtils.isNotEmpty(oldUserDatas)){
                getAndupdateDailyWithdrawCountLimit(oldUserDatas);
            }
        }

        //4.AutoWithdrawAduitThreshold为0，则转空
        List<Long> zeroAutoWithdrawAduitThresholdList = getZeroAutoWithdrawAduitThreshold(userInfoList);
        if (CollectionUtils.isNotEmpty(zeroAutoWithdrawAduitThresholdList)){
            List<OldUserData> oldUserDatas = getOldUserDatas(zeroAutoWithdrawAduitThresholdList);
            if (CollectionUtils.isNotEmpty(oldUserDatas)){
                getAndupdateAutoWithdrawAduitThreshold(oldUserDatas);
            }
        }
    }

    private List<Long> getAllZeroUserIds(List<UserInfo> userInfoList) {
        if (CollectionUtils.isEmpty(userInfoList)){
            return Lists.newArrayList();
        }
        List<Long> list = new ArrayList<>(userInfoList.size());
        for (UserInfo userInfo:userInfoList){
            if (userInfo != null && userInfo.getUserId() != null && checkIsZero(userInfo.getDailyWithdrawCap())
                    && checkIsZero(userInfo.getDailyWithdrawCountLimit()) && checkIsZero(userInfo.getAutoWithdrawAuditThreshold())){
                list.add(userInfo.getUserId());
            }
        }
        return list;
    }

    private List<Long> getZeroDailyWithdrawCap(List<UserInfo> userInfoList) {
        List<Long> result = new ArrayList<>(userInfoList.size());
        for (UserInfo userInfo:userInfoList){
            if (userInfo != null && userInfo.getUserId() != null && checkIsZero(userInfo.getDailyWithdrawCap())){
                result.add(userInfo.getUserId());
            }
        }
        return result;
    }

    private List<Long> getZeroDailyWithdrawCountLimit(List<UserInfo> userInfoList) {
        List<Long> result = new ArrayList<>(userInfoList.size());
        for (UserInfo userInfo:userInfoList){
            if (userInfo != null && userInfo.getUserId() != null && checkIsZero(userInfo.getDailyWithdrawCountLimit())){
                result.add(userInfo.getUserId());
            }
        }
        return result;
    }

    private List<Long> getZeroAutoWithdrawAduitThreshold(List<UserInfo> userInfoList) {
        List<Long> result = new ArrayList<>(userInfoList.size());
        for (UserInfo userInfo:userInfoList){
            if (userInfo != null && userInfo.getUserId() != null && checkIsZero(userInfo.getAutoWithdrawAuditThreshold()) ){
                result.add(userInfo.getUserId());
            }
        }
        return result;
    }

    private boolean checkIsZero(BigDecimal bigDecimal){
        return bigDecimal != null && bigDecimal.doubleValue() == 0;
    }

    private boolean checkIsZero(Integer integer){
        return integer != null && 0 == integer;
    }

    private void doUpdateThreeFieldAllNullByUserIds(List<Long> allNullUserIds) {
        List<User> users = userMapper.selectByUserIds(allNullUserIds);
        if (CollectionUtils.isEmpty(users)){
            log.info("doUpdateThreeFieldAllNullByUserIds.execute.selectByUserIds.result:{}", JsonUtils.toJsonHasNullKey(users));
            return;
        }
        List<String> userIds = getUserIds(users);
        if (CollectionUtils.isEmpty(userIds)){
            return;
        }
        List<OldUserData> oldUserDatas = oldUserDataMapper.selectByUserIds(userIds);
        log.info("doUpdateThreeFieldAllNullByUserIds.execute.oldUserDatas.result:{}",JsonUtils.toJsonHasNullKey(oldUserDatas));
        if (CollectionUtils.isEmpty(oldUserDatas)){
            return;
        }

        getAndupdateAllnull(oldUserDatas);
    }

    private List<OldUserData> getOldUserDatas(List<Long> list) {
        List<User> users = userMapper.selectByUserIds(list);
        if (CollectionUtils.isEmpty(users)){
            log.info("updateDailyThreeFiledJobHandler.execute.selectByUserIds.result:{}", JsonUtils.toJsonHasNullKey(users));
            return Lists.newArrayList();
        }
        List<String> userIds = getUserIds(users);
        if (CollectionUtils.isEmpty(userIds)){
            return Lists.newArrayList();
        }
        List<OldUserData> oldUserDatas = oldUserDataMapper.selectByUserIds(userIds);
        log.info("updateDailyThreeFiledJobHandler.execute.oldUserDatas.result:{}",JsonUtils.toJsonHasNullKey(oldUserDatas));
        if (CollectionUtils.isEmpty(oldUserDatas)){
            return Lists.newArrayList();
        }
        return oldUserDatas;
    }

    private void getAndupdateAutoWithdrawAduitThreshold(List<OldUserData> oldUserDatas) {
        List<Long> updateAutoWithdrawAduitThresholdUserIds = getNullAutoWithdrawAduitThreshold(oldUserDatas);
        log.info("updateDailyThreeFiledJobHandler.execute.updateUserIds.result:{}",updateAutoWithdrawAduitThresholdUserIds);
        if (CollectionUtils.isEmpty(updateAutoWithdrawAduitThresholdUserIds)){
            return;
        }
        userInfoMapper.updateAutoWithdrawAuditThresholdNull(updateAutoWithdrawAduitThresholdUserIds);
        log.info("updateDailyThreeFiledJobHandler.execute.updateAutoWithdrawAduitThresholdUserIds.end");
    }

    private void getAndupdateDailyWithdrawCountLimit(List<OldUserData> oldUserDatas) {
        List<Long> updateDailyWithdrawCountLimitUserIds = getNullDailyWithdrawCountLimit(oldUserDatas);
        log.info("updateDailyThreeFiledJobHandler.execute.updateUserIds.result:{}",updateDailyWithdrawCountLimitUserIds);
        if (CollectionUtils.isEmpty(updateDailyWithdrawCountLimitUserIds)){
            return;
        }
        userInfoMapper.updateDailyWithdrawCountLimitNull(updateDailyWithdrawCountLimitUserIds);
        log.info("updateDailyThreeFiledJobHandler.execute.updateDailyWithdrawCountLimitUserIds.500end");
    }

    private void getAndupdateDailywithDrawCap(List<OldUserData> oldUserDatas) {
        List<Long> updateDailyWithdrawCapUserIds = getNullDailyWithdrawCap(oldUserDatas);
        log.info("updateDailyThreeFiledJobHandler.execute.updateUserIds.result:{}",updateDailyWithdrawCapUserIds);
        if (CollectionUtils.isEmpty(updateDailyWithdrawCapUserIds)){
            return;
        }
        userInfoMapper.updateDailyWithdrawCapNull(updateDailyWithdrawCapUserIds);
        log.info("updateDailyThreeFiledJobHandler.execute.updateDailyWithdrawCapNull.500end");
    }

    private void getAndupdateAllnull(List<OldUserData> oldUserDatas) {
        List<Long> updateAllNullUserIds = getAllNullDailyThreeFiled(oldUserDatas);
        log.info("updateDailyThreeFiledJobHandler.execute.updateUserIds.result:{}",updateAllNullUserIds);
        if (CollectionUtils.isEmpty(updateAllNullUserIds)){
            return;
        }
        userInfoMapper.updateThreeDailyWithdrawFiledNull(updateAllNullUserIds);
        log.info("updateDailyThreeFiledJobHandler.execute.updateDailyWithdrawCapNull.500end");
    }

    private List<Long> getAllNullDailyThreeFiled(List<OldUserData> oldUserDatas) {
        List<Long> result = new ArrayList<>(oldUserDatas.size());
        for (OldUserData oldUserData:oldUserDatas){
            if (oldUserData != null && oldUserData.getUserId() != null && oldUserData.getWithdrawMaxAssetDay() == null
                    && oldUserData.getWithdrawMaxCountDay() == null && oldUserData.getReviewQuota() == null){
                result.add(Long.parseLong(oldUserData.getUserId()));
            }
        }
        return result;
    }

    private List<Long> getNullDailyWithdrawCap(List<OldUserData> oldUserDatas) {
        List<Long> result = new ArrayList<>(oldUserDatas.size());
        for (OldUserData oldUserData:oldUserDatas){
            if (oldUserData != null && oldUserData.getUserId() != null && oldUserData.getWithdrawMaxAssetDay() == null){
                result.add(Long.parseLong(oldUserData.getUserId()));
            }
        }
        return result;
    }

    private List<Long> getNullDailyWithdrawCountLimit(List<OldUserData> oldUserDatas) {
        List<Long> result = new ArrayList<>(oldUserDatas.size());
        for (OldUserData oldUserData:oldUserDatas){
            if (oldUserData != null && oldUserData.getUserId() != null && oldUserData.getWithdrawMaxCountDay() == null){
                result.add(Long.parseLong(oldUserData.getUserId()));
            }
        }
        return result;
    }

    private List<Long> getNullAutoWithdrawAduitThreshold(List<OldUserData> oldUserDatas) {
        List<Long> result = new ArrayList<>(oldUserDatas.size());
        for (OldUserData oldUserData:oldUserDatas){
            if (oldUserData != null && oldUserData.getUserId() != null && oldUserData.getReviewQuota() == null){
                result.add(Long.parseLong(oldUserData.getUserId()));
            }
        }
        return result;
    }

    private List<String> getUserIds(List<User> users) {
        List<String> result = new ArrayList<>(users.size());
        for (User user:users){
            if (isNormalUser(user) && user.getUserId() != null){
                result.add(String.valueOf(user.getUserId()));
            }
        }
        return result;
    }

    protected boolean isNormalUser(User user) {
        return user != null
                && !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)
                && !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_MARGIN_USER)
                && !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_FUTURE_USER)
                && !BitUtils.isEnable(user.getStatus(), Constant.USER_DELETE);
    }
}
