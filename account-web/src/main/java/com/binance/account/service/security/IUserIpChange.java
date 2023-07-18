package com.binance.account.service.security;

import com.binance.account.data.entity.user.User;
import com.binance.account.vo.security.request.ConfirmedUserIpChangeRequest;
import com.binance.account.vo.security.response.ConfirmedUserIpChangeResponse;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public interface IUserIpChange {

    /**
     * 判断是否为历史登录ip
     */
    boolean isHistoryIp(Long userId, String ip);
    /**
     * 检测是否为新ip
     */
    void sensitiveIpCheck(User user, String ip, AuthTypeEnum authType, String ipChangeConfirmLink, String customForbiddenLink, boolean isStrictMode) throws Exception;

    APIResponse<ConfirmedUserIpChangeResponse> confirmedUserIpChange(APIRequest<ConfirmedUserIpChangeRequest> request) throws Exception;

    /**
     * 是否为sensitive用户（大户）
     * @return true.是
     */
    boolean isSensitive(Long userId);
}
