package com.binance.account.api;

import com.binance.account.vo.subuser.BrokerUserCommisssionVo;
import com.binance.account.vo.subuser.request.AddOrUpdateBrokerUserCommissionRequest;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by yangyang on 2019/8/21.
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/broker/sub-user/admin")
@Api(value = "broker母子账号接口-BnbAdmin管理")
public interface BrokerSubUserAdminApi {

    @ApiOperation("开启broker母子账号功能")
    @PostMapping("/function/enable")
    APIResponse<Boolean> enableBrokerSubUserFunction(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("关闭broker母子账号功能（一期无视这个功能）")
    @PostMapping("/function/disable")
    APIResponse<Boolean> disableBrokerSubUserFunction(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;


    @ApiOperation("查询broker账户配置")
    @PostMapping("/getBrokerUserCommission")
    APIResponse<BrokerUserCommisssionVo> getBrokerUserCommission(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("add or update broker账户配置")
    @PostMapping("/addOrUpdateBrokerUserCommission")
    APIResponse<Integer> addOrUpdateBrokerUserCommission(@RequestBody() APIRequest<AddOrUpdateBrokerUserCommissionRequest> request) throws Exception;



}
