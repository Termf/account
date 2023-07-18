package com.binance.account.api;

import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.user.request.EnableUserLVTByAdminRequest;
import com.binance.account.vo.user.response.SignLVTStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/user-lvt")
@Api(value = "user lvt相关api")
public interface UserLVTApi {

    @ApiOperation("签署LVT风险协议，开启杠杆代币交易")
    @PostMapping("/signLVTRiskAgreement")
    APIResponse<Boolean> signLVTRiskAgreement(@RequestBody() APIRequest<UserIdReq> request) throws Exception;
    
    @ApiOperation("admin开启关闭用户LVT杠杆代币交易")
    @PostMapping("/enableUserLVTByAdmin")
    APIResponse<Boolean> enableUserLVTByAdmin(@RequestBody() APIRequest<EnableUserLVTByAdminRequest> request) throws Exception;

    @ApiOperation("用户签署LVT风险协议状态")
    @PostMapping("/signLVTStatus")
    APIResponse<SignLVTStatusResponse> signLVTStatus(@RequestBody APIRequest<UserIdReq> request) throws Exception;

}
