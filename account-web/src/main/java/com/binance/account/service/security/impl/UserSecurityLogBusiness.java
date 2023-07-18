package com.binance.account.service.security.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.user.User;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.security.request.*;

import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.service.security.IUserSecurityLog;
import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.UserSecurityLogListResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Type;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UserSecurityLogBusiness implements IUserSecurityLog {

    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;
    
    @Resource
    private UserIndexMapper userIndexMapper;

    @Resource
    private UserCommonBusiness userCommonBusiness;

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<GetUserSecurityLogResponse> getUserSecurityLogList(APIRequest<GetUserSecurityLogRequest> request)
            throws Exception {
        final GetUserSecurityLogRequest requestBody = request.getBody();
        if(requestBody.getOffset() <= 0) {
        	requestBody.setOffset(0);
        }
        if(requestBody.getLimit() <= 0) {
        	requestBody.setLimit(0);
        }
        Long count = this.userSecurityLogMapper.getUserSecurityCountByUserIdAndOperateType(requestBody.getUserId(),
                requestBody.getOperateType());
        List<UserSecurityLog> result = Collections.emptyList();
        if (count != null && count > 0) {
            result = this.userSecurityLogMapper.getUserSecurityListByUserIdAndOperateType(requestBody.getUserId(),
                    requestBody.getOperateType(), requestBody.getOffset(), requestBody.getLimit());
        }
        GetUserSecurityLogResponse resp = new GetUserSecurityLogResponse();
        resp.setCount(count);

        List<UserSecurityLogVo> userSecurityLogVos = new ArrayList<>();
        for (UserSecurityLog userSecurityLog : result) {
            UserSecurityLogVo userSecurityLogVo = new UserSecurityLogVo();
            BeanUtils.copyProperties(userSecurityLog, userSecurityLogVo);
            userSecurityLogVos.add(userSecurityLogVo);
        }
        resp.setResult(userSecurityLogVos);
        return APIResponse.getOKJsonResult(resp);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<UserSecurityLogVo> getLastLoginLog(APIRequest<UserIdRequest> request) throws Exception {
        final UserIdRequest requestBody = request.getBody();
        UserSecurityLog userSecurityLog = this.userSecurityLogMapper.getLastLoginLogByUserId(requestBody.getUserId());
        UserSecurityLogVo userSecurityLogVo = null;
        if(userSecurityLog != null) {
        	userSecurityLogVo = new UserSecurityLogVo();
        	BeanUtils.copyProperties(userSecurityLog, userSecurityLogVo);
        }
        return APIResponse.getOKJsonResult(userSecurityLogVo);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<GetUserSecurityLogResponse> getLogPage(APIRequest<UserSecurityRequest> request) {
        final UserSecurityRequest requestBody = request.getBody();
        UserSecurityLog userSecurityLog = null;
        if(requestBody != null) {
            userSecurityLog = new UserSecurityLog();
            BeanUtils.copyProperties(requestBody, userSecurityLog);
        }
        
        try {
            List<UserSecurityLog> userSecurityLogList = this.userSecurityLogMapper.getLogPage(userSecurityLog);
            Long count = this.userSecurityLogMapper.getLogPageTotal(userSecurityLog);
            List<UserSecurityLogVo> userSecurityLogVoList = new ArrayList<>();
            if(!userSecurityLogList.isEmpty()) {
                for (UserSecurityLog log : userSecurityLogList) {
                    UserSecurityLogVo logVo = new UserSecurityLogVo();
                    BeanUtils.copyProperties(log, logVo);
                    userSecurityLogVoList.add(logVo);
                }
            } 
            GetUserSecurityLogResponse resp = new GetUserSecurityLogResponse();
            resp.setCount(count);
            resp.setResult(userSecurityLogVoList);
            return APIResponse.getOKJsonResult(resp);
        }catch (Exception e) {
            log.error("getLogPage error-->",e);
            return APIResponse.getErrorJsonResult(Type.GENERAL,e);
        }
       
    }

    @Override
    public void addSecurityLogAsync(UserSecurityLog record) {
        AsyncTaskExecutor.execute(()->{
            try {
                userSecurityLogMapper.insertSelective(record);
            } catch (Exception e) {
                log.error("addSecurityLogAsync error, record:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(record)), e);
            }
        });
    }

	@Override
	public APIResponse<UserSecurityLogListResponse> getLogByIp(APIRequest<IpPageRequest> request) {
	    final IpPageRequest requestBody = request.getBody();
        
        UserSecurityLog userSecurityLog = null;
        if(requestBody != null) {
            userSecurityLog = new UserSecurityLog();
            BeanUtils.copyProperties(requestBody, userSecurityLog);
        }           
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<UserSecurityLog> userSecurityLogList = userSecurityLogMapper.getLogByIp(userSecurityLog);
        stopWatch.stop();
        log.info("getUserSecurityLogList end, elapsedTime: {} seconds,ip:{}", stopWatch.getTotalTimeSeconds(), requestBody.getIp());
        
        StopWatch stopWatchTo = new StopWatch();
        stopWatchTo.start();
        
        List<UserSecurityLogVo> userSecurityLogVoList = new ArrayList<>();
        UserSecurityLogListResponse resp = new UserSecurityLogListResponse();
        if(!userSecurityLogList.isEmpty()) {
            List<UserSecurityLog> newUserSecurityLogList = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            userSecurityLogList.stream().forEach(log->{
                if(log.getUserId() != null) {
                    userIds.add(log.getUserId());
                }
            });
           
            //根据userIds查email
           List<UserIndex> userIndexList = userIndexMapper.selectByUserIds(userIds);
           //循环设置email到userSecurityLogVoList的email中
           if(!userIndexList.isEmpty()) {
               for(int i = 0; i < userSecurityLogList.size(); i++) {
                   UserSecurityLog us = userSecurityLogList.get(i); 
                   for(int j = 0; j < userIndexList.size(); j++) {
                       UserIndex ui = userIndexList.get(j);
                       if(us.getUserId() != null && us.getUserId().equals(ui.getUserId())) {
                           us.setEmail(ui.getEmail());
                           newUserSecurityLogList.add(us);
                           break;
                       }
                   }
               }
               
               newUserSecurityLogList.stream().forEach(newLog->{
                   UserSecurityLogVo logVo = new UserSecurityLogVo();
                    BeanUtils.copyProperties(newLog, logVo);
                    userSecurityLogVoList.add(logVo);
               });

           }
            
            resp.setResult(userSecurityLogVoList);
        }
        
        stopWatchTo.stop();
        log.info("getUserSecurityLogVoList end, elapsedTime: {} seconds,ip:{}", stopWatchTo.getTotalTimeSeconds(), requestBody.getIp());

        return APIResponse.getOKJsonResult(resp);
	}

	@Override
	public APIResponse<List<Map<String, Object>>> getLogByIpCount(APIRequest<IpRequest> request) {
		final IpRequest requestBody = request.getBody();
		StopWatch stopWatchTo = new StopWatch();
        stopWatchTo.start();
		List<String> ips = requestBody.getIps();
		List<Map<String, Object>> ipMap = userSecurityLogMapper.getLogByIpCount(ips);
		stopWatchTo.stop();
		log.info("getLogByIpCount end, elapsedTime: {} seconds, ip:{}, ipMap:{}", stopWatchTo.getTotalTimeSeconds(), ips, ipMap);
		return APIResponse.getOKJsonResult(ipMap);
	}

    @Override
    public Boolean isBackendDisadbled(Long userId) {
        User user = userCommonBusiness.checkAndGetUserById(userId);
        if (!BitUtils.isTrue(user.getStatus(), Constant.USER_DISABLED)) {
            //这个用户没被锁定就不应该传进来
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        List<String> operationTypeList= Lists.newArrayList();
        operationTypeList.add(Constant.SECURITY_OPERATE_ONE_BUTTON_DISABLE);
        operationTypeList.add(Constant.SECURITY_OPERATE_TYPE_FORBIDDEN_USER);
        operationTypeList.add(AccountConstants.ONE_BUTTON_DISABLE_FRONTEND);
        operationTypeList.add(AccountConstants.ONE_BUTTON_DISABLE_BACKEND);

        List<UserSecurityLog> userSecurityLogList = this.userSecurityLogMapper.getUserSecurityListByUserIdAndOperateTypeList(
                userId,operationTypeList, 0, 10);
        if(CollectionUtils.isEmpty(userSecurityLogList)){
            return false;
        }
        UserSecurityLog lastUserSecurityLog=userSecurityLogList.get(0);
        if(Constant.SECURITY_OPERATE_TYPE_FORBIDDEN_USER.equals(lastUserSecurityLog.getOperateType())
                ||AccountConstants.ONE_BUTTON_DISABLE_FRONTEND.equals(lastUserSecurityLog.getOperateType())){
            log.info("isBackendDisadbled: userId={},false",userId);
            return false;
        }
        if(Constant.SECURITY_OPERATE_ONE_BUTTON_DISABLE.equals(lastUserSecurityLog.getOperateType())
                ||AccountConstants.ONE_BUTTON_DISABLE_BACKEND.equals(lastUserSecurityLog.getOperateType())){
            log.info("isBackendDisadbled: userId={},true",userId);
            return true;
        }
        return false;
    }
}
