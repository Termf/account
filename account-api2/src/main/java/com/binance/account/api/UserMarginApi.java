package com.binance.account.api;

import com.binance.account.vo.margin.request.CheckIsolatedMarginUserRelationShipReq;
import com.binance.account.vo.margin.request.CreateIsolatedMarginUserReq;
import com.binance.account.vo.margin.request.GetIsolatedMarginUserListReq;
import com.binance.account.vo.margin.request.GetRootUserIdByIsolatedMarginUserIdReq;
import com.binance.account.vo.margin.response.CreateIsolatedMarginUserResp;
import com.binance.account.vo.margin.response.GetIsolatedMarginUserListResp;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.request.MainMarginAccountTransferRequest;
import com.binance.account.vo.user.response.MainMarginAccountTransferResponse;
import com.binance.account.vo.user.response.MarginUserTypeResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lufei
 * @date 2019/3/11
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/margin")
@Api(value = "Margin账户")
public interface UserMarginApi {

    @ApiOperation("根据母账户获取本身、子账户、Margin账户信息")
    @PostMapping("/allUserInfo")
    APIResponse<MarginUserTypeResponse> allUserInfo(@RequestBody @Validated APIRequest<UserIdRequest> request)
            throws Exception;

    /**
     * 子账号的margin账户，当成子账户处理
     */
    @ApiOperation("根据母账户获取本身、子账户、Margin账户信息，加上子账户的margin账户")
    @PostMapping("/globalUserInfo")
    APIResponse<MarginUserTypeResponse> globalUserInfo(@RequestBody @Validated APIRequest<UserIdRequest> request)
            throws Exception;


    @ApiOperation("主账号margin账号划转")
    @PostMapping("/subMainMarginAccountTransfer")
    APIResponse<MainMarginAccountTransferResponse> subMainMarginAccountTransfer(@RequestBody APIRequest<MainMarginAccountTransferRequest> request) throws Exception;



    @ApiOperation("创建margin逐仓账号")
    @PostMapping("/createIsolatedMarginUser")
    APIResponse<CreateIsolatedMarginUserResp> createIsolatedMarginUser(@RequestBody() APIRequest<CreateIsolatedMarginUserReq> request) throws Exception;


    @ApiOperation("查询margin逐仓账号列表")
    @PostMapping("/getIsolatedMarginUserList")
    APIResponse<GetIsolatedMarginUserListResp> getIsolatedMarginUserList(@RequestBody() APIRequest<GetIsolatedMarginUserListReq> request) throws Exception;


    @ApiOperation("查询margin逐仓账号列表")
    @PostMapping("/getRootUserIdByIsolatedMarginUserId")
    APIResponse<Long> getRootUserIdByIsolatedMarginUserId(@RequestBody() APIRequest<GetRootUserIdByIsolatedMarginUserIdReq> request) throws Exception;


    @ApiOperation("检查逐仓margin账号关系")
    @PostMapping("/checkIsolatedMarginRelationShip")
    APIResponse<Boolean> checkIsolatedMarginRelationShip(@RequestBody() APIRequest<CheckIsolatedMarginUserRelationShipReq> request) throws Exception;






}
