package com.binance.account.api;

import com.binance.account.vo.user.request.UserReferralSettingsRequest;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 提交用户的返佣设置。
 *
 * 现用于美国站交易返佣的合规。
 *
 * @author sunzhenlei
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE)
@RequestMapping(value = "/user/referralSettings")
@Api(value = "用户返佣设置")
public interface UserReferralSettingsApi {

    @ApiOperation(notes = "提交返佣设置信息", nickname = "referralInfoSubmit", value = "提交用户返佣设置信息")
    @PostMapping("/submit")
    APIResponse<?> submit(@RequestBody() APIRequest<UserReferralSettingsRequest> request);
}
