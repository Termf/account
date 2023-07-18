package com.binance.account.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.other.AddTradeCompleteNotifyTaskRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/ussite/autoEmail")
@Api(value = "美国站点自动通知用户邮件任务")
public interface UsSiteAutomatedEmailDripApi {
	
	@ApiOperation("判断是否需要发送充值5000usd后未交易邮件通知")
    @PostMapping("/needSendDepositNotify")
	APIResponse<Boolean> needSendDepositNotify(@Validated @RequestBody APIRequest<Long> request);
	
	@ApiOperation("充值5000usd后未交易邮件通知")
    @PostMapping("/addDepositNotifyTask")
	APIResponse<Void> addDepositNotifyTask(@Validated @RequestBody APIRequest<Long> request);
	
	@ApiOperation("US站添加交易满额邮件通知任务")
    @PostMapping("/batchAddTradeCompleteTask")
	APIResponse<Void> batchAddTradeCompleteTask(@Validated @RequestBody APIRequest<List<AddTradeCompleteNotifyTaskRequest>> request);

}
