package com.binance.account.api;

import com.binance.account.vo.subuser.request.AssetSubUserToCommonRequest;
import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.SubUserIdReq;
import com.binance.account.vo.subuser.response.ParentUserSubUsersResp;
import com.binance.account.vo.subuser.response.SubUserParentUserResp;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Fei.Huang on 2018/10/12.
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/sub-user/admin")
@Api(value = "母子账号接口-BnbAdmin管理")
public interface SubUserAdminApi {

    @ApiOperation("开启母子账号功能")
    @PostMapping("/function/enable")
    APIResponse<Boolean> enableSubUserFunction(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("关闭母子账号功能")
    @PostMapping("/function/disable")
    APIResponse<Boolean> disableSubUserFunction(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("绑定母子账号")
    @PostMapping("/relation/binding")
    APIResponse<Boolean> bindParentSubUser(@RequestBody() APIRequest<BindingParentSubUserReq> request) throws Exception;

    @ApiOperation("根据子账号获取母子账号列表")
    @PostMapping("/relation/query/sub-user-id")
    APIResponse<SubUserParentUserResp> queryBySubUserId(@RequestBody() APIRequest<SubUserIdReq> request) throws Exception;

    @ApiOperation("根据母账号获取母子账号列表")
    @PostMapping("/relation/query/parent-user-id")
    APIResponse<ParentUserSubUsersResp> queryByParentUserId(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("更新子账号备注")
    @PostMapping("/relation/remark/update")
    APIResponse<Boolean> updateSubUserRemark(@Validated() @RequestBody() APIRequest<SubUserIdReq> request) throws Exception;

    @ApiOperation("查看是否绑定有子账号并返回子账号数量")
    @PostMapping("/relation/count/parent-user-id")
    APIResponse<Long> countSubUsersByParentUserId(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("将资管子账号修改为普通账号")
    @PostMapping("/assetSubUserToCommon")
    APIResponse<Void> assetSubUserToCommon(@RequestBody() APIRequest<AssetSubUserToCommonRequest> request) throws Exception;

}