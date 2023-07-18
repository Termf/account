package com.binance.account.api;

import com.binance.account.vo.apimanage.request.DeleteAllApiKeyRequest;
import com.binance.account.vo.apimanage.request.DeleteApiKeyRequest;
import com.binance.account.vo.apimanage.request.EnableApiCreateRequest;
import com.binance.account.vo.apimanage.request.EnableApiWithdrawRequest;
import com.binance.account.vo.apimanage.request.EnableUpdateApiKeyRequest;
import com.binance.account.vo.apimanage.request.GetApiListRequest;
import com.binance.account.vo.apimanage.request.GetApisRequest;
import com.binance.account.vo.apimanage.request.SaveApiKeyRequest;
import com.binance.account.vo.apimanage.request.SaveApiKeyV2Request;
import com.binance.account.vo.apimanage.request.SearchApiRequest;
import com.binance.account.vo.apimanage.request.SearchDeletedApiRequest;
import com.binance.account.vo.apimanage.request.UpdateApiKeyRequest;
import com.binance.account.vo.apimanage.request.UpdateApiKeyRestrictIpRequest;
import com.binance.account.vo.apimanage.request.UpdateApiKeyV3Request;
import com.binance.account.vo.apimanage.response.ApiModelResponse;
import com.binance.account.vo.apimanage.response.EnableApiWithdrawResponse;
import com.binance.account.vo.apimanage.response.EnableUpdateApiKeyResponse;
import com.binance.account.vo.apimanage.response.PagingResult;
import com.binance.account.vo.apimanage.response.SaveApiKeyV2Response;
import com.binance.account.vo.apimanage.response.UpdateApiKeyResponse;
import com.binance.account.vo.apimanage.response.UpdateApiKeyV3Response;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
public interface ApiManage {

    @ApiOperation("根据条件查询Api信息")
    @PostMapping("/apiManage/getList")
    public APIResponse<PagingResult<ApiModelResponse>> loadApiList(@RequestBody APIRequest<SearchApiRequest> request) throws Exception;

    @PostMapping("/query-api-userandapikey")
    @ApiOperation("根据userId和apiKey查询API信息。参数中只有userId和apiKey会参与查询，而且系统会校验其不能为空")
    public APIResponse<ApiModelResponse> queryApiByUserAndApiKey(@RequestBody APIRequest<SearchApiRequest> request) throws Exception;

    @ApiOperation("根据条件查询删除的Api信息")
    @PostMapping("/account/getDeletedList")
    public APIResponse<PagingResult<ApiModelResponse>> loadDeletedApiList(@RequestBody APIRequest<SearchDeletedApiRequest> request) throws Exception;

    @ApiOperation("创建API，并返回这条API信息")
    @PostMapping("/mgmt/account/createApiKey")
    public APIResponse<ApiModelResponse> createApiKey(@RequestBody APIRequest<SaveApiKeyRequest> request) throws Exception;

    @ApiOperation("创建API，并返回这条API信息")
    @PostMapping("/mgmt/account/createApiKeyV2")
    public APIResponse<SaveApiKeyV2Response> createApiKeyV2(@RequestBody APIRequest<SaveApiKeyV2Request> request) throws Exception;

    @ApiOperation("")
    @PostMapping("/mgmt/account/updateApiKey")
    public APIResponse<UpdateApiKeyResponse> updateApiKey(@RequestBody APIRequest<UpdateApiKeyRequest> request) throws Exception;

    @ApiOperation("")
    @PostMapping("/mgmt/account/updateApiKeyV2")
    public APIResponse<UpdateApiKeyResponse> updateApiKeyV2(@RequestBody APIRequest<UpdateApiKeyRequest> request) throws Exception;

    @ApiOperation("")
    @PostMapping("/mgmt/account/updateApiKeyV3")
    public APIResponse<UpdateApiKeyV3Response> updateApiKeyV3(@RequestBody APIRequest<UpdateApiKeyV3Request> request) throws Exception;

    @ApiOperation("")
    @PostMapping("/mgmt/account/deleteApiKey")
    public APIResponse<Void> deleteApiKey(@RequestBody APIRequest<DeleteApiKeyRequest> request) throws Exception;

    @ApiOperation("")
    @PostMapping("/mgmt/account/deleteAllApiKey")
    public APIResponse<Void> deleteAllApiKey(@RequestBody APIRequest<DeleteAllApiKeyRequest> request) throws Exception;

    @PostMapping(value = "/manageApi/enableApiCreate")
    public APIResponse<ApiModelResponse> enableApiCreate(@RequestBody APIRequest<EnableApiCreateRequest> request) throws Exception;

    @PostMapping(value = "/manageApi/enableUpdateApiKey")
    public APIResponse<EnableUpdateApiKeyResponse> enableUpdateApiKey(@RequestBody APIRequest<EnableUpdateApiKeyRequest> request) throws Exception;

    @PostMapping(value = "/manageApi/enableApiWithdraw")
    public APIResponse<EnableApiWithdrawResponse> enableApiWithdraw(@RequestBody APIRequest<EnableApiWithdrawRequest> request) throws Exception;

    @PostMapping(value = "/manageApi/getApis")
    public APIResponse<List<ApiModelResponse>> getApis(@RequestBody APIRequest<GetApisRequest> request) throws Exception;

    @ApiOperation("根据id、userId、apiKy、apiName四个参数进行查询，各个参数都可能为空")
    @PostMapping(value = "/api/getApiList")
    public APIResponse<List<ApiModelResponse>> getApiList(@RequestBody APIRequest<GetApiListRequest> request) throws Exception;

    @ApiOperation("修改apiKeyIp限制开关，若未传Ip，则读取数据库的或填充默认0.0.0.0")
    @PostMapping(value = "/api/modifyApiKeyIpRestrictSwitch")
    public APIResponse<ApiModelResponse> modifyApiKeyIpRestrictSwitch(@RequestBody APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception;

    @ApiOperation("删除需要限制访问的Ip")
    @PostMapping(value = "/api/deleteApiKeyRestrictIp")
    public APIResponse<ApiModelResponse> deleteApiKeyRestrictIp(@RequestBody APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception;

    @ApiOperation("添加需要限制访问的Ip，必须填写有效ip，且不能包含0.0.0.0")
    @PostMapping(value = "/api/addApiKeyRestrictIp")
    public APIResponse<ApiModelResponse> addApiKeyRestrictIp(@RequestBody APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception;

    @ApiOperation("查询需要限制访问的Ip")
    @PostMapping(value = "/api/queryApiKeyRestrictIp")
    public APIResponse<ApiModelResponse> queryApiKeyRestrictIp(@RequestBody APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception;
}
