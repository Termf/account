package com.binance.account.service.user.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.binance.account.common.enums.UserConfigTypeEnum;
import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.assetservice.UserAssetApiClient;
import com.binance.account.vo.user.request.EnableUserLVTByAdminRequest;
import com.binance.account.vo.user.response.SignLVTStatusResponse;
import com.binance.assetservice.vo.response.UserAssetResponse;
import com.binance.master.utils.TrackingUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.integration.mbxgateway.AccountApiClient;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.service.user.IUserLVT;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.StringUtils;
import com.binance.mbxgateway.enums.PermissionsBitEnum;
import com.binance.mbxgateway.vo.response.TradingAccountResponseV3;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Monitored
public class UserLVTBusiness implements IUserLVT {

    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private MatchboxApiClient matchboxApiClient;
    @Autowired
    private AccountApiClient accountApiClient;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UserAssetApiClient userAssetApiClient;
    

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Boolean> signLVTRiskAgreement(APIRequest<UserIdReq> request) {
        try {
            log.info("signLVTRiskAgreement start, params: {}", JSONObject.toJSONString(request));
            final UserIdReq requestBody = request.getBody();
            final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
            if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }

            final User user = this.userMapper.queryByEmail(userIndex.getEmail());
            if (user == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            
            // 先查user_config，查看是否有admin禁用其lvt
            UserConfig userConfig = selectLVTAdminOperRecord(requestBody.getUserId());
            if (userConfig != null && !Boolean.valueOf(userConfig.getConfigName())) {
                log.warn("userId={} signlVT failed, disabled by admin", requestBody.getUserId());
                throw new BusinessException(AccountErrorCode.NOT_ALLOW_SIGNLVT_ERROR);   
            }

            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());

            User updateDO = new User();
            updateDO.setEmail(user.getEmail());
            updateDO.setStatus(BitUtils.enable(user.getStatus(), AccountCommonConstant.SIGNED_LVT_RISK_AGREEMENT));
            userMapper.updateUserStatusByEmail(updateDO);

            // 1.从mbx拿到用户已有的permission信息
//            TradingAccountResponseV3 accountInfo = accountApiClient.getAccountV3(user.getUserId().toString());
//            // 接口返回的是permission字符串，需要先转换为其对应的status值
//            List<String> permissionList = accountInfo.getPermissions();
//            Long permissionStatus = 0L;
//            if (CollectionUtils.isNotEmpty(permissionList)) {
//                for (String permission : permissionList) {
//                    PermissionsBitEnum bitEnum = PermissionsBitEnum.valueOf(permission);
//                     if (bitEnum != null) {
//                         permissionStatus += bitEnum.getCode();
//                     }
//                }
//            }

            // 2.enable LEVERAGED bit
//            permissionStatus = BitUtils.enable(permissionStatus, PermissionsBitEnum.LEVERAGED.getCode());

            // 3.请求mbx putAccount，传入permissionStatus
            try {
                matchboxApiClient.putAccountPermission(userInfo.getTradingAccount().toString(), "5");
            } catch (BusinessException e) {
                if (e.getBizMessage().startsWith("The requested action would change no state")) {
                    // mbx返回状态未改变，可以不抛异常，不回滚
                    log.warn("signLVTRiskAgreement putAccountPermission return {}", e.getBizMessage());
                } else {
                    throw e;
                }
            }

            log.info("signLVTRiskAgreement success");
            return APIResponse.getOKJsonResult();
        } catch (BusinessException e) {
            log.error("signLVTRiskAgreement error ", e);
            throw e;
        } catch (Exception e) {
            log.error("signLVTRiskAgreement error ", e);
            throw new BusinessException("signLVTRiskAgreement error");
        }
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Boolean> enableUserLVTByAdmin(APIRequest<EnableUserLVTByAdminRequest> request) throws Exception {
        log.info("cancelSignLVT start, params: {}", JSONObject.toJSONString(request));
        final EnableUserLVTByAdminRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        if (requestBody.getEnableLVT()) {
            // admin启用某用户lvt
            // 启用。1.user_config 设置为true; 2.调用signLVTRiskAgreement（user status设置为true；同步mbx）。都是幂等的
            UserConfig userConfig = new UserConfig();
            userConfig.setUserId(userId);
            userConfig.setConfigType(UserConfigTypeEnum.LVT_ADMIN_OPER.name());
            userConfig.setConfigName("true");
            userInfoMapper.insertOrUpdateUserConfig(userConfig);

            final APIRequest originRequest = new APIRequest<>();
            originRequest.setLanguage(request.getLanguage());
            originRequest.setTerminal(request.getTerminal());
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            UserIdReq userIdReq = new UserIdReq();
            userIdReq.setUserId(userId);
            ((UserLVTBusiness) applicationContext.getBean(this.getClass())).signLVTRiskAgreement(APIRequest.instance(originRequest, userIdReq));
        } else {
            // admin禁用某用户lvt
            // 要先查该用户是否有杠杆代币资产
            UserAssetResponse userAssetResponse = userAssetApiClient.getPrivateUserAsset(userId.toString(), null);
            for (UserAssetResponse.UserAsset userAsset : userAssetResponse.getUserAssetList()) {
                if (!userAsset.isEtf()) {
                    continue;    
                }
                BigDecimal free = userAsset.getFree() != null ? userAsset.getFree() : new BigDecimal("0");
                BigDecimal freeze = userAsset.getFreeze() != null ? userAsset.getFreeze() : new BigDecimal("0");
                BigDecimal locked = userAsset.getLocked() != null ? userAsset.getLocked() : new BigDecimal("0");
                BigDecimal withdrawing = userAsset.getWithdrawing() != null ? userAsset.getWithdrawing() : new BigDecimal("0");
                if (free.add(freeze).add(locked).add(withdrawing).compareTo(new BigDecimal("0")) > 0) {
                    log.info("userId={} has etf asset, disable lvt failed", userId);
                    throw new BusinessException(AccountErrorCode.USER_HAS_ETF_ASSET_ERROR);
                }
            }
            
            // 禁用。1.user_config 设置为false; 2.调用cancelSignLVT(user status设置为false；同步mbx) 。都是幂等的
            UserConfig userConfig = new UserConfig();
            userConfig.setUserId(userId);
            userConfig.setConfigType(UserConfigTypeEnum.LVT_ADMIN_OPER.name());
            userConfig.setConfigName("false");
            userInfoMapper.insertOrUpdateUserConfig(userConfig);

            final APIRequest originRequest = new APIRequest<>();
            originRequest.setLanguage(request.getLanguage());
            originRequest.setTerminal(request.getTerminal());
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            UserIdReq userIdReq = new UserIdReq();
            userIdReq.setUserId(userId);
            ((UserLVTBusiness) applicationContext.getBean(this.getClass())).cancelSignLVT(APIRequest.instance(originRequest, userIdReq));
        }
        // 查询时，sysLimit == null 取signStatus
        // sysLimit == true 时，都开放了，不弹页面
        // sysLimit == false时，都禁用了，也不弹页面

        return APIResponse.getOKJsonResult(true);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Boolean> cancelSignLVT(APIRequest<UserIdReq> request) {
        try {
            log.info("cancelSignLVT start, params: {}", JSONObject.toJSONString(request));
            final UserIdReq requestBody = request.getBody();
            final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
            if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }

            final User user = this.userMapper.queryByEmail(userIndex.getEmail());
            if (user == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }

            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());

            User updateDO = new User();
            updateDO.setEmail(user.getEmail());
            updateDO.setStatus(BitUtils.disable(user.getStatus(), AccountCommonConstant.SIGNED_LVT_RISK_AGREEMENT));
            userMapper.updateUserStatusByEmail(updateDO);

            // 1.从mbx拿到用户已有的permission信息
            TradingAccountResponseV3 accountInfo = accountApiClient.getAccountV3(user.getUserId().toString());
            // 接口返回的是permission字符串，需要先转换为其对应的status值
            List<String> permissionList = accountInfo.getPermissions();
            Long permissionStatus = 0L;
            if (CollectionUtils.isNotEmpty(permissionList)) {
                for (String permission : permissionList) {
                    PermissionsBitEnum bitEnum = PermissionsBitEnum.valueOf(permission);
                    if (bitEnum != null) {
                        permissionStatus += bitEnum.getCode();
                    }
                }
            }

            // 2.disable LEVERAGED bit
            permissionStatus = BitUtils.disable(permissionStatus, PermissionsBitEnum.LEVERAGED.getCode());

            // 3.请求mbx putAccount，传入permissionStatus
            try {
                matchboxApiClient.putAccountPermission(userInfo.getTradingAccount().toString(), permissionStatus.toString());
            } catch (BusinessException e) {
                if (e.getBizMessage().startsWith("The requested action would change no state")) {
                    // mbx返回状态未改变，可以不抛异常，不回滚
                    log.warn("cancelSignLVT putAccountPermission return {}", e.getBizMessage());
                } else {
                    throw e;
                }
            }

            log.info("cancelSignLVT success");
            return APIResponse.getOKJsonResult();
        } catch (BusinessException e) {
            log.error("cancelSignLVT error ", e);
            throw e;
        } catch (Exception e) {
            log.error("cancelSignLVT error ", e);
            throw new BusinessException("cancelSignLVT error");
        }
    }

    @Override
    public APIResponse<SignLVTStatusResponse> signLVTStatus(APIRequest<UserIdReq> request) {
        final UserIdReq requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        log.info("signLVTStatus userId: {}", userId);

        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        User user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Boolean isSignedLVTRiskAgreement = BitUtils.isTrue(user.getStatus(), AccountCommonConstant.SIGNED_LVT_RISK_AGREEMENT);

        // 查询是否有admin启用/禁用记录
        UserConfig userConfig = selectLVTAdminOperRecord(userId);
        Boolean adminLVTStatus = null;
        if (userConfig != null) {
            adminLVTStatus = Boolean.valueOf(userConfig.getConfigName());        
        }

        SignLVTStatusResponse response = new SignLVTStatusResponse();
        response.setUserId(userId);
        response.setIsSignedLVTRiskAgreement(isSignedLVTRiskAgreement);
        response.setAdminLVTStatus(adminLVTStatus);
        return APIResponse.getOKJsonResult(response);
    }
    
    UserConfig selectLVTAdminOperRecord(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        List<String> configTypes = Arrays.asList(UserConfigTypeEnum.LVT_ADMIN_OPER.name());
        params.put("configTypes", configTypes);
        List<UserConfig> userConfigs = userInfoMapper.selectUserConfigList(params);
        if (CollectionUtils.isNotEmpty(userConfigs)) {
            return userConfigs.get(0);
        }    
        return null;
    }
}
