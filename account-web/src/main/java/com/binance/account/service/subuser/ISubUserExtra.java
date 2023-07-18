package com.binance.account.service.subuser;

import com.binance.account.vo.subuser.request.OpenOrCloseSubUserReq;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * Created by zhao chenkai on 2019/12/11.
 * 补充service，可避免循环依赖
 */
public interface ISubUserExtra {

    /**
     * 启用或禁用子账户
     *
     * @param request
     * @param toEnable 是否启用
     * @return
     */
    APIResponse<Integer> enableOrDisableSubUser(APIRequest<OpenOrCloseSubUserReq> request, boolean toEnable) throws Exception;

}
