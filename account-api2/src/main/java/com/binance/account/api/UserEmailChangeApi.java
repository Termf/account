package com.binance.account.api;

import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.OldEmailCaptchaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.binance.account.vo.user.response.UserEmailChangeInitResponse;
import com.binance.account.vo.user.response.UserEmailChangeResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/userEmailChange")
@Api(value = "用户更改邮箱")
public interface UserEmailChangeApi {

    @ApiOperation(notes = "初始化流程", nickname = "initFlow", value = "初始化流程")
    @PostMapping("/initFlow")
    APIResponse<UserEmailChangeInitResponse> initFlow(@Validated @RequestBody APIRequest<UserEmailChangeInitFlowRequest> userEmailChangeInitFlowRequest);

    @Deprecated
    @ApiOperation(notes = "点击老邮箱link", nickname = "linkOldEmail", value = "点击老邮箱link")
    @GetMapping("/linkOldEmail")
    APIResponse<Void> linkOldEmail(@RequestParam("flowId")String flowId, @RequestParam("userId") Long userId);


    @ApiOperation(notes = "点击老邮箱link", nickname = "linkOldEmail", value = "点击老邮箱link")
    @PostMapping("/linkOldEmail")
    APIResponse<Void> linkOldEmail(@Validated @RequestBody APIRequest<UserEmailChangeLinkRequest> request);

    @ApiOperation(notes = "验证老邮箱验证码", nickname = "validOldEmailCaptcha", value = "验证老邮箱验证码")
    @PostMapping("/validOldEmailCaptcha")
    APIResponse<OldEmailCaptchaResponse> validOldEmailCaptcha(@Validated @RequestBody APIRequest<OldEmailCaptchaRequest> request);


    @ApiOperation(notes = "confirm新邮箱", nickname = "confirmNewEmail", value = "confirm新邮箱")
    @PostMapping("/confirmNewEmail")
    APIResponse<String> confirmNewEmail(@Validated @RequestBody APIRequest<UserEmailChangeConfirmNewEmailRequest> request);


    @Deprecated
    @ApiOperation(notes = "点击新邮箱link", nickname = "linkNewEmail", value = "点击新邮箱link")
    @GetMapping("/linkNewEmail")
    APIResponse<UserEmailChangeInitResponse> linkNewEmail(@RequestParam("flowId") String flowId, @RequestParam("userId") Long userId);



    @ApiOperation(notes = "点击新邮箱link", nickname = "linkNewEmail", value = "点击新邮箱link")
    @PostMapping("/linkNewEmail")
    APIResponse<UserEmailChangeInitResponse> linkNewEmail(@Validated @RequestBody APIRequest<UserEmailChangeLinkRequest> request);


    @ApiOperation(notes = "验证新邮箱的几种验证码", nickname = "validNewEmailCaptcha", value = "验证新邮箱的几种验证码")
    @PostMapping("/validNewEmailCaptcha")
    APIResponse<UserEmailChangeInitResponse> validNewEmailCaptcha(@Validated @RequestBody APIRequest<NewEmailCaptchaRequest> request);



    @ApiOperation(notes = "重新发送老邮箱邮件", nickname = "sendOldEmail", value = "重新发送老邮箱邮件")
    @GetMapping("/sendOldEmail")
    APIResponse<Void> sendOldEmail(@RequestParam("flowId") String flowId, @RequestParam("userId") Long userId,@RequestParam("email") String email);



    @ApiOperation(notes = "重新发送新邮箱邮件", nickname = "linkNewEmail", value = "重新发送新邮箱邮件")
    @GetMapping("/sendNewEmail")
    APIResponse<Void> sendNewEmail(@RequestParam("flowId")String flowId, @RequestParam("userId")Long userId,@RequestParam("email") String email);

    @ApiOperation(notes = "分页查询用户更换邮箱接口", nickname = "getEmailChangeList", value = "分页查询用户更换邮箱接口")
    @PostMapping("/getEmailChangeList")
    APIResponse<UserEmailChangeResponse> getEmailChangeList(@RequestBody APIRequest<UserEmailChangeRequest> request);

    @ApiOperation(notes = "根据flowId更新流程状态信息", nickname = "updateUserEmailChangeByFlowId", value = "根据flowId更新流程状态信息")
    @PostMapping("/updateUserEmailChangeByFlowId")
    APIResponse<Void> updateUserEmailChangeByFlowId(@RequestBody APIRequest<UserEmailChangeRequest> request);


    @ApiOperation(notes = "验证码版本 确认新邮箱", nickname = "confirmNewEmailV3", value = "验证码版本 确认新邮箱")
    @PostMapping("/confirmNewEmailV3")
    APIResponse<String> confirmNewEmailV3(@Validated @RequestBody APIRequest<NewEmailConfirmRequest> request);


}
