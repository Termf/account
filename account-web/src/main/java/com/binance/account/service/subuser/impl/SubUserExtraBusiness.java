package com.binance.account.service.subuser.impl;

import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.integration.mbxgateway.MbxgatewayIOrderApiClient;
import com.binance.account.service.apimanage.IApiManageService;
import com.binance.account.service.subuser.ISubUser;
import com.binance.account.service.subuser.ISubUserExtra;
import com.binance.account.vo.apimanage.request.DeleteAllApiKeyRequest;
import com.binance.account.vo.subuser.request.OpenOrCloseSubUserReq;
import com.binance.master.constant.Constant;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.WebUtils;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhao chenkai on 2019/12/11.
 * 补充service，可避免循环依赖
 */
@Log4j2
@Service
public class SubUserExtraBusiness extends CheckSubUserBusiness implements ISubUserExtra {

    @Autowired
    private ISubUser subUser;
    @Autowired
    private IApiManageService apiManageService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MbxgatewayIOrderApiClient mbxgatewayOrderApiClient;

    @Override
    public APIResponse<Integer> enableOrDisableSubUser(APIRequest<OpenOrCloseSubUserReq> request, boolean toEnable) throws Exception {
        OpenOrCloseSubUserReq requestBody = request.getBody();

        // 确保是母账户
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());


        List<Long> subUserIds = requestBody.getUserIds();
        // 确保是子母关系
        subUserIds.stream().forEach(subUserId -> assertParentSubUserBoundNotCheckParent(parentUserId, subUserId));

        // 批量查子账户信息，获取子账户userId、status值
        List<User> subUsers = userMapper.selectByUserIds(subUserIds);

        final UserSecurityLog securityLog = new UserSecurityLog();
        String subUserIdsStr = new Gson().toJson(subUserIds);

        log.info("enableOrDisableSubUser parentUserId:{}, toEnable:{}, subUserIds:{}", parentUserId, toEnable,
                subUserIdsStr);

        int num = 0;

        // 启用(可交易、可登陆)
        if (toEnable) {
            for (User subUser : subUsers) {
                Long status = subUser.getStatus();
                if(checkAssetSubUser(status)){
                    continue;
                }
                status = BitUtils.enable(status, Constant.USER_IS_SUB_USER_ENABLED);
                status = BitUtils.disable(status, Constant.USER_DISABLED);

                User updatedSubUser = new User();
                updatedSubUser.setStatus(status);
                updatedSubUser.setEmail(subUser.getEmail());
                num += this.userMapper.updateByEmailSelective(updatedSubUser);
            }
            securityLog.setOperateType(Constant.SECURITY_OPERATE_ENABLED_SUBUSER);
            securityLog.setDescription("启用子账户,子账户userIds:" + subUserIdsStr);
        } else {
            // 禁用(禁止交易、禁止登陆)
            for (User subUser : subUsers) {
                Long status = subUser.getStatus();
                if(checkAssetSubUser(status)){
                    continue;
                }
                status = BitUtils.disable(status, Constant.USER_IS_SUB_USER_ENABLED);
                status = BitUtils.enable(status, Constant.USER_DISABLED);

                User updatedSubUser = new User();
                updatedSubUser.setStatus(status);
                updatedSubUser.setEmail(subUser.getEmail());
                num += this.userMapper.updateByEmailSelective(updatedSubUser);

                // 用户禁用后，删除相关api和mbx order
                apiManageService.deleteApiAndOrders(String.valueOf(subUser.getUserId()));
            }
            securityLog.setOperateType(Constant.SECURITY_OPERATE_DISABLE_SUBUSER);
            securityLog.setDescription("禁用子账户,子账户userIds:" + subUserIdsStr);
        }

        // 添加安全日志
        addSecurityLog(securityLog, parentUserId, "enableOrDisableSubUser");

        return APIResponse.getOKJsonResult(num);
    }

    /**
     * 添加安全日志
     *
     * @param securityLog
     * @param parentUserId:母账户userId
     * @param wayName:方法名
     */
    private void addSecurityLog(UserSecurityLog securityLog, Long parentUserId, String wayName) {
        try {
            String ip = WebUtils.getRequestIp();
            final APIRequestHeader header = WebUtils.getAPIRequestHeader();

            securityLog.setUserId(parentUserId);
            securityLog.setIp(ip);

            if (header != null) {
                securityLog.setClientType(header.getTerminal().getCode());
            } else {
                securityLog.setClientType(TerminalEnum.OTHER.getCode());
            }
            securityLog.setIpLocation(IP2LocationUtils.getCountryCity(ip));
            securityLog.setOperateTime(DateUtils.getNewUTCDate());
            this.userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error("add {} securityLog failed, userId:{}, exception:{}", wayName, parentUserId, e);
        }
    }
}
