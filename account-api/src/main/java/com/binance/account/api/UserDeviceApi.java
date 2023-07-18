package com.binance.account.api;

import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.device.request.*;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.account.vo.device.response.CheckUserDeviceResponse;
import com.binance.account.vo.device.response.CheckWithdrawDeviceResponse;
import com.binance.account.vo.device.response.FindMostSimilarUserDeviceResponse;
import com.binance.account.vo.device.response.ResendAuthorizeDeviceEmailResponse;
import com.binance.account.vo.device.response.UserDeviceAuthorizeResponse;
import com.binance.account.vo.device.response.UserDeviceHistoryVo;
import com.binance.account.vo.device.response.UserDeviceVo;
import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.user.request.LoginUserRequest;
import com.binance.account.vo.user.response.LoginUserResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/device")
@Api(value = "用户设备")
public interface UserDeviceApi {

    @ApiOperation(notes = "新增设备指纹", nickname = "add device", value = "新增设备指纹，Return：设备id")
    @PostMapping("/add")
    APIResponse<AddUserDeviceResponse> addDevice(@RequestBody APIRequest<UserDeviceRequest> request);

    @ApiOperation("记录敏感操作的设备指纹")
    @PostMapping("/associate/sensitive")
    APIResponse<AddUserDeviceResponse> associateSensitiveDevice(@Validated @RequestBody APIRequest<UserDeviceRequest> request);

    @ApiOperation(notes = "校验设备指纹", nickname = "check device", value = "校验设备指纹")
    @PostMapping("/check")
    APIResponse<CheckUserDeviceResponse> checkDevice(@RequestBody APIRequest<UserDeviceRequest> request);

    @ApiOperation("校验提现时的设备指纹")
    @PostMapping("/check/withdraw")
    APIResponse<CheckWithdrawDeviceResponse> checkWithdrawDevice(@RequestBody APIRequest<UserDeviceRequest> request);

    @ApiOperation(notes = "确认设备", nickname = "check device", value = "确认设备")
    @PostMapping("/authorize")
    APIResponse<UserDeviceAuthorizeResponse> authorizeDevice(@RequestBody APIRequest<UserDeviceAuthorizeRequest> request);

    @ApiOperation(notes = "查询用户设备列表", nickname = "query device", value = "查询用户设备")
    @PostMapping("/list")
    APIResponse<List<UserDeviceVo>> listDevice(@RequestBody APIRequest<UserDeviceListRequest> request);

    @ApiOperation(notes = "分页查询用户设备列表", nickname = "page query device", value = "查询用户设备")
    @PostMapping("/page")
    APIResponse<SearchResult<UserDeviceVo>> pageDevice(@RequestBody APIRequest<UserDeviceListRequest> request);

    @ApiOperation("查询用户设备列表-admin")
    @PostMapping("/admin/list")
    APIResponse<SearchResult> listDeviceForAdmin(@RequestBody APIRequest<UserDeviceListRequest> request);

    @ApiOperation(notes = "查询用户设备", nickname = "query device", value = "查询用户设备")
    @PostMapping("/get")
    APIResponse<UserDeviceVo> getDevice(@RequestBody APIRequest<IdLongRequest> request);

    @ApiOperation(notes = "查询指定设备登陆历史", nickname = "query device history", value = "查询指定设备登陆历史")
    @PostMapping("/history")
    APIResponse<List<UserDeviceHistoryVo>> listHistory(@RequestBody APIRequest<UserDeviceHistoryQueryRequest> request);

    @ApiOperation(notes = "查询指定设备登陆历史", nickname = "query device history", value = "查询指定设备登陆历史")
    @PostMapping("/delete")
    APIResponse<Void> deleteDevice(@RequestBody APIRequest<UserDeviceDeleteRequest> request);

    @ApiOperation(notes = "Admin-查询设备指纹配置", nickname = "query properties", value = "查询设备属性配置")
    @PostMapping("/config/property/list")
    APIResponse<?> listDeviceProperties(APIRequest<UserDevicePropertyRequest> request);

    @ApiOperation(notes = "Admin-新增设备指纹配置", nickname = "add property", value = "新增设备指纹配置")
    @PostMapping("/config/property/add")
    APIResponse<?> addDeviceProperty(@RequestBody APIRequest<UserDevicePropertyRequest> request);

    @ApiOperation(notes = "Admin-编辑设备指纹配置", nickname = "edit property", value = "编辑设备指纹配置")
    @PostMapping("/config/property/edit")
    APIResponse<?> editDeviceProperty(@RequestBody APIRequest<UserDevicePropertyRequest> request);

    @ApiOperation(notes = "Admin-删除设备指纹配置", nickname = "delete property", value = "删除设备指纹配置")
    @PostMapping("/config/property/delete")
    APIResponse<?> deleteDeviceProperty(@RequestBody APIRequest<IDRequest> request);

    @ApiOperation("Admin-查询待授权的特殊设备列表")
    @PostMapping("/special/cache")
    APIResponse<?> specialCache();

    @ApiOperation(notes = "设备指纹开关状态查询", nickname = "switch", value = "设备指纹开关状态查询")
    @PostMapping("/config/switch")
    APIResponse<?> getSwitch();

    @ApiOperation("查询设备关系列表")
    @GetMapping("/relation/list")
    APIResponse<List> listRelation(@RequestParam("devicePk") Long devicePk);

    @ApiOperation("查找相似设备")
    @PostMapping("/most-similar")
    APIResponse<FindMostSimilarUserDeviceResponse> findMostSimilarUserDevice(@RequestBody APIRequest<FindMostSimilarUserDeviceRequest> request);

    @ApiOperation(notes = "重新发送新设备授权邮件", nickname = "resendAuthorizeDeviceEmail", value = "重新发送新设备授权邮件")
    @PostMapping("/resendAuthorizeDeviceEmail")
    APIResponse<ResendAuthorizeDeviceEmailResponse> resendAuthorizeDeviceEmail(@RequestBody() APIRequest<ResendAuthorizeDeviceEmailRequest> request) throws Exception;

    @ApiOperation(notes = "verifyAuthDeviceCode", nickname = "verifyAuthDeviceCode", value = "验证新设备验证码是否合法")
    @PostMapping("/verifyAuthDeviceCode")
    APIResponse<Boolean> verifyAuthDeviceCode(@RequestBody() APIRequest<VerifyAuthDeviceCodeRequest> request) throws Exception;

    @ApiOperation("查询用户上次登陆的设备")
    @GetMapping("/getUserLastLoginDevice")
    APIResponse<TerminalEnum> getUserLastLoginDevice(@RequestBody @Validated APIRequest<Long> request)throws Exception;
}
