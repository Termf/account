package com.binance.account.service.security;

import com.binance.account.vo.security.request.UserForbidRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public interface IUserForbid {

    /**
     * 禁用用户
     * 老的禁用方式，部分禁用逻辑在pnk中
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> forbiddenUser(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 禁用用户
     * 包含完整禁用逻辑
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> forbiddenUserTotal(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 点击链接禁用禁用用户
     *
     * @param request
     * @return
     */
    APIResponse<Boolean> forbidUserByCode(APIRequest<UserForbidRequest> request);

}
