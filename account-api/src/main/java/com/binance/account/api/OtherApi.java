package com.binance.account.api;

import com.binance.account.vo.kyc.CountryStateVo;
import com.binance.account.vo.other.CleanLocalCacheRequest;
import com.binance.account.vo.other.GetCountryStateRequest;
import com.binance.account.vo.other.GetMessageMapRequest;
import com.binance.account.vo.other.MessageMapVo;
import com.binance.account.vo.other.SendDisableTokenEmailRequest;
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

import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/other")
@Api(value = "其他")
public interface OtherApi {
    
    @ApiOperation(notes = "移除redis缓存", nickname = "removeRedisCach", value = "移除redis缓存")
    @PostMapping("/removeRedisCache")
    public APIResponse<?> removeRedisCache(@RequestBody() @Validated()APIRequest<String> request) throws Exception;
    
    @ApiOperation(notes = "根据tradingAccountId获取撮合中的相关状态", nickname = "getDetailsByTradingAccountId", value = "根据tradingAccountId获取撮合中的相关状态")
    @PostMapping("/getDetailsByTradingAccountId")
    public APIResponse<Object> getDetailsByTradingAccountId(@RequestBody() @Validated()APIRequest<Long> request) throws Exception;
    
    @ApiOperation(notes = "发送带一键禁用连接的邮件", nickname = "sendDisableTokenEmail", value = "发送带一键禁用连接的邮件")
    @PostMapping("/sendDisableTokenEmail")
    public APIResponse<String> sendDisableTokenEmail(@RequestBody() @Validated()APIRequest<SendDisableTokenEmailRequest> request);

    @ApiOperation("清除消息映射缓存")
    @PostMapping("/cleanLocalCache")
    APIResponse<Void> cleanLocalCache(@Validated @RequestBody APIRequest<CleanLocalCacheRequest> request);

    @ApiOperation("获取消息缓存信息")
    @PostMapping("/getMessageMapList")
    APIResponse<List<MessageMapVo>> getMessageMapList(@Validated @RequestBody APIRequest<GetMessageMapRequest> request);

    @ApiOperation("获取消息映射信息, language使用APIRequest中设置的")
    @PostMapping("/getMessageMapResult")
    APIResponse<String> getMessageByKey(@Validated @RequestBody APIRequest<GetMessageMapRequest> request);

    @ApiOperation("获取国家州信息列表")
    @PostMapping("/getCountryStateInfoList")
    APIResponse<List<CountryStateVo>> getCountryStateInfoList(@Validated @RequestBody APIRequest<GetCountryStateRequest> request);
    
    @ApiOperation("获取国家州信息")
    @PostMapping("/getCountryStateByKey")
    APIResponse<CountryStateVo> getCountryStateByKey(@Validated @RequestBody APIRequest<GetCountryStateRequest> request);
    
    @ApiOperation("保存获取国家州信息")
    @PostMapping("/updateCountryState")
    APIResponse<Void> updateCountryState(@Validated @RequestBody APIRequest<GetCountryStateRequest> request);
    
    @ApiOperation("编辑消息映射")
    @PostMapping("/modifyMessageMap")
    APIResponse<Void> modifyMessageMap(@Validated @RequestBody APIRequest<List<MessageMapVo>> request);
    
    @ApiOperation("模糊查询消息映射")
    @PostMapping("/fuzzySearch")
    APIResponse<List<MessageMapVo>> fuzzySearch(@Validated @RequestBody APIRequest<MessageMapVo> request);
}
