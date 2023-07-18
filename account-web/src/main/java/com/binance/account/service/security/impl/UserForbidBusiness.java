package com.binance.account.service.security.impl;

import java.util.List;

import com.binance.account.service.device.IUserDevice;
import com.binance.account.vo.device.request.UserDeviceDeleteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.binance.account.aop.SecurityLog;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.integration.capital.CapitalClient;
import com.binance.account.integration.mbxgateway.MbxgatewayIOrderApiClient;
import com.binance.account.integration.streamer.StreamerOrderApiClient;
import com.binance.account.service.apimanage.IApiManageService;
import com.binance.account.service.security.IUserForbid;
import com.binance.account.vo.apimanage.request.DeleteAllApiKeyRequest;
import com.binance.account.vo.security.request.UserForbidRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.matchbox.api.AccountApi;
import com.binance.streamer.api.response.vo.OpenOrderVo;
import com.google.common.collect.Lists;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class UserForbidBusiness implements IUserForbid {

    @Autowired
    private UserSecurityLogMapper userSecurityLogMapper;
    @Autowired
    private StreamerOrderApiClient streamerOrderApiClient;
    @Autowired
    private MbxgatewayIOrderApiClient mbxGatewayOrderApiCLient;
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserSecurityMapper userSecurityMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private IApiManageService apiManageService;
    @Autowired
    private CapitalClient capitalClient;
    @Autowired
    private AccountApi accountApi;
    @Autowired
    private IUserDevice iUserDevice;
    

    @Override
    @SecurityLog(name = "一键禁用用户前台", operateType = AccountConstants.ONE_BUTTON_DISABLE_FRONTEND,
            userId = "#request.body.userId")
    public APIResponse<Integer> forbiddenUser(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        //撤单
        cancelOrder(requestBody.getUserId());
        boolean done = this.forbidUser(requestBody.getUserId());
        AsyncTaskExecutor.execute(() -> {
            UserSecurityLog log = new UserSecurityLog();
            log.setUserId(requestBody.getUserId());
            log.setIp(WebUtils.getRequestIp());
            TerminalEnum terminal = WebUtils.getTerminal();
            log.setClientType(terminal == null?"other":terminal.getCode());
            log.setIpLocation(IP2LocationUtils.getCountryCity(WebUtils.getRequestIp()));
            log.setOperateType(AccountConstants.ONE_BUTTON_DISABLE_FRONTEND);
            log.setDescription((terminal == null?"other":terminal.getCode())+"_一键禁用用户前台_result="+(done?"成功":"失败"));
            log.setOperateTime(DateUtils.getNewUTCDate());
            this.userSecurityLogMapper.insertSelective(log);
        });
        return APIResponse.getOKJsonResult(done ? 1 : 0);
    }

    @Override
    @SecurityLog(name = "一键禁用用户前台", operateType = AccountConstants.ONE_BUTTON_DISABLE_FRONTEND,
            userId = "#request.body.userId")
    public APIResponse<Integer> forbiddenUserTotal(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        //撤单
        cancelOrder(requestBody.getUserId());
        boolean done = this.forbidUserTotal(requestBody.getUserId());
        AsyncTaskExecutor.execute(() -> {
            UserSecurityLog log = new UserSecurityLog();
            log.setUserId(requestBody.getUserId());
            log.setIp(WebUtils.getRequestIp());
            TerminalEnum terminal = WebUtils.getTerminal();
            log.setClientType(terminal == null?"other":terminal.getCode());
            log.setIpLocation(IP2LocationUtils.getCountryCity(WebUtils.getRequestIp()));
            log.setOperateType(AccountConstants.ONE_BUTTON_DISABLE_FRONTEND);
            log.setDescription((terminal == null?"other":terminal.getCode())+"_一键禁用用户前台_result="+(done?"成功":"失败"));
            log.setOperateTime(DateUtils.getNewUTCDate());
            this.userSecurityLogMapper.insertSelective(log);
        });
        return APIResponse.getOKJsonResult(done ? 1 : 0);
    }

    @Override
    @SecurityLog(name = "一键禁用用户前台", operateType = AccountConstants.ONE_BUTTON_DISABLE_FRONTEND,
            userId = "#request.body.userId")
    public APIResponse<Boolean> forbidUserByCode(APIRequest<UserForbidRequest> request) {
        UserForbidRequest requestBody = request.getBody();
        String cache = RedisCacheUtils.get(requestBody.getCode(), String.class, CacheKeys.USER_DISABLE_CODE);
        if (StringUtils.isNotBlank(cache) && cache.equals(requestBody.getUserId().toString())) {
            //撤单
            cancelOrder(requestBody.getUserId());
            boolean done = this.forbidUser(requestBody.getUserId());
            return APIResponse.getOKJsonResult(done);
        } else {
            throw new BusinessException(GeneralCode.USER_INVALID_LINK_CODE);
        }
    }

    private void cancelOrder(Long userId){
        //查询到这个用户的所有挂单，并且全部撤销
        List<OpenOrderVo> openOrderVoList= streamerOrderApiClient.selectOpenOrderOnlyByUserId(userId);
        if(CollectionUtils.isEmpty(openOrderVoList)){
            return;
        }
        for(OpenOrderVo openOrderVo:openOrderVoList){
            try {
                String symbol= openOrderVo.getSymbol();
                Long orderId=openOrderVo.getOrderId();
                mbxGatewayOrderApiCLient.mDeleteOrder(userId.toString(), Lists.newArrayList(symbol),Lists.newArrayList(orderId.toString()));
            }catch (Exception e){
                String errorMessage=String.format("用户一键禁用 撤销用户订单失败:userId:%s, symbol:%s, orderId:%s",userId,openOrderVo.getSymbol(),openOrderVo.getOrderId());
                log.error(errorMessage, e);
            }
        }
    }

    private boolean forbidUser(Long userId) {
        log.info("forbidUser start userId={}", userId);
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 1.登录禁用-修改用户状态	
        User user = this.userMapper.queryByEmail(userIndex.getEmail());

        User userRecord = new User();
        Long status = user.getStatus();
        status = BitUtils.enable(status, Constant.USER_DISABLED);
        // 交易禁用	
        status = BitUtils.enable(status, Constant.USER_TRADE);
        userRecord.setStatus(status);
        userRecord.setEmail(user.getEmail());
        int flag = userMapper.updateByEmail(userRecord);

        // 修改用户安全信息	
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
        userSecurity.setLoginFailedNum(0);
        userSecurity.setDisableTime(DateUtils.getNewUTCDate());
        //userSecurity.setWithdrawSecurityStatus(1);	
        int flag2 = this.userSecurityMapper.updateByPrimaryKeySelective(userSecurity);

        // 2.交易禁用-调用撮合引擎	
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {
            accountApi.setTradingAccount(userInfo.getTradingAccount(), false, true, true);
            log.info("disable tradingAccount done, userId:{}, tradingAccountId:{}", userId,
                    userInfo.getTradingAccount());
        }
        // 3.取消处理中的提币请求，pnk中实现	
        // 4.删除所有API，pnk中实现
        log.info("forbidUser finish userId={} flag={} flag2={}", userId, flag, flag2);
        return flag == 1 && flag == flag2;
    }
    
    private boolean forbidUserTotal(Long userId) {
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 1.登录禁用-修改用户状态
        User user = this.userMapper.queryByEmail(userIndex.getEmail());

        User userRecord = new User();
        Long status = user.getStatus();
        status = BitUtils.enable(status, Constant.USER_DISABLED);
        // 交易禁用
        status = BitUtils.enable(status, Constant.USER_TRADE);
        userRecord.setStatus(status);
        userRecord.setEmail(user.getEmail());
        int flag = userMapper.updateByEmail(userRecord);
        log.info("forbidUserTotal 禁用用户 userId={} flag={}", userId, flag);

        // 修改用户安全信息
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
        userSecurity.setLoginFailedNum(0);
        userSecurity.setDisableTime(DateUtils.getNewUTCDate());
        //userSecurity.setWithdrawSecurityStatus(1);
        int flag2 = this.userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
        log.info("forbidUserTotal 修改用户安全信息 userId={} flag2={}", userId, flag2);

        // 2.交易禁用-调用撮合引擎
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {
            accountApi.setTradingAccount(userInfo.getTradingAccount(), false, true, true);
            log.info("forbidUserTotal disable tradingAccount done, userId:{}, tradingAccountId:{}", userId,
                    userInfo.getTradingAccount());
        }
        // 3.取消处理中的提币请求
        Boolean flag3 = capitalClient.cancelUserAllWithdrawing(userId);
        log.info("forbidUserTotal 取消处理中提币 userId={} flag3={}", userId, flag3);
        
        // 4.删除所有API
        Boolean flag4 = false;
        try {
            DeleteAllApiKeyRequest deleteAllApiKeyRequest = new DeleteAllApiKeyRequest();
            deleteAllApiKeyRequest.setLoginUid(userId.toString());
            apiManageService.deleteAllApiKey(deleteAllApiKeyRequest);
            flag4 = true;
            log.info("forbidUserTotal 删除所有API userId={} flag4={}", userId, flag4);
        } catch (Exception e) {
            log.error("forbidUserTotal deleteAllApiKey error, userId="+userId, e);
        }
        
        // 5.删除用户所有设备信息
        Boolean flag5 = false;
        try {
            UserDeviceDeleteRequest deviceDeleteRequest = new UserDeviceDeleteRequest();
            deviceDeleteRequest.setUserId(userId);
            deviceDeleteRequest.setSource("user");
            deviceDeleteRequest.setMemo("用户一键禁用");
            iUserDevice.deleteDevices(APIRequest.instance(deviceDeleteRequest));
            flag5 = true;
            log.info("forbidUserTotal 删除所有设备信息 userId={} flag5={}", userId, flag5);
        } catch (Exception e) {
            log.error("forbidUserTotal deleteDevices error, userId="+userId, e);       
        }
        
        return flag == 1 && flag == flag2 && flag3 && flag4 && flag5;
    }

}
