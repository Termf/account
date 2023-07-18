package com.binance.account.controller.apimanage;

import com.binance.account.api.ApiManage;
import com.binance.account.service.apimanage.IApiManageService;
import com.binance.account.vo.apimanage.request.*;
import com.binance.account.vo.apimanage.response.*;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ApiManageController implements ApiManage {

    @Autowired
    private IApiManageService apiManageService;

    @Override
    public APIResponse<PagingResult<ApiModelResponse>> loadApiList(
            @RequestBody @Validated APIRequest<SearchApiRequest> request) throws Exception {
        try {
            log.info("正在调用：loadApiList，参数：{}", request.toString());

            PagingResult<ApiModelResponse> response = this.apiManageService.loadApiList(request.getBody());

            log.info("调用：loadApiList，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.loadApiList occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<PagingResult<ApiModelResponse>> loadDeletedApiList(
            @RequestBody @Validated APIRequest<SearchDeletedApiRequest> request) throws Exception {
        try{
            log.info("正在调用：loadDeletedApiList，参数：{}", request.toString());

            PagingResult<ApiModelResponse> response = this.apiManageService.loadDeletedApiList(request.getBody());

            log.info("调用：loadDeletedApiList，返回");
            return APIResponse.getOKJsonResult(response);
        }catch(Exception e){
            log.warn("ApiController.loadDeletedApiList occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<ApiModelResponse> queryApiByUserAndApiKey(@RequestBody @Validated APIRequest<SearchApiRequest> request) throws Exception {
        try {
            log.info("正在调用：queryApiByUserAndApiKey，参数：{}", request.toString());

            ApiModelResponse response = this.apiManageService.queryApiByUserAndApiKey(request.getBody().getUserId(), request.getBody().getApiKey());

            log.info("调用：queryApiByUserAndApiKey，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.queryApiByUserAndApiKey occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<ApiModelResponse> createApiKey(@RequestBody @Validated APIRequest<SaveApiKeyRequest> request) throws Exception {
        try {
            log.info("正在调用：createApiKey，参数：{}", request.toString());

            ApiModelResponse response = this.apiManageService.saveApiKey(request.getBody());

            log.info("调用：createApiKey，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.createApiKey occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<SaveApiKeyV2Response> createApiKeyV2(APIRequest<SaveApiKeyV2Request> request) throws Exception {
        try {
            log.info("正在调用：createApiKeyV2，参数：{}", request.toString());

            SaveApiKeyV2Response response = this.apiManageService.saveApiKeyV2(request.getBody());

            log.info("调用：createApiKeyV2，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.createApiKeyV2 occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<ApiModelResponse> enableApiCreate(@RequestBody @Validated APIRequest<EnableApiCreateRequest> request) throws Exception {
        try {
            log.info("正在调用：enableApiCreate，参数：{}", request.toString());

            ApiModelResponse response = this.apiManageService.enableApiCreate(request.getBody());

            log.info("调用：enableApiCreate，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.enableApiCreate occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<UpdateApiKeyResponse> updateApiKey(@RequestBody @Validated APIRequest<UpdateApiKeyRequest> request) throws Exception {
        try {
            log.info("正在调用：updateApiKey，参数：{}", request.toString());

            UpdateApiKeyResponse response = this.apiManageService.updateApiKey(request.getBody());

            log.info("调用：updateApiKey，返回");

            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.updateApiKey occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<UpdateApiKeyResponse> updateApiKeyV2(@RequestBody @Validated APIRequest<UpdateApiKeyRequest> request) throws Exception {
        try {
            log.info("正在调用：updateApiKeyV2，参数：{}", request.toString());

            UpdateApiKeyResponse response = this.apiManageService.updateApiKeyV2(request.getBody());

            log.info("调用：updateApiKeyV2，返回");

            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.updateApiKeyV2 occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<UpdateApiKeyV3Response> updateApiKeyV3(APIRequest<UpdateApiKeyV3Request> request) throws Exception {
        try {
            log.info("正在调用：updateApiKeyV3，参数：{}", request.toString());

            UpdateApiKeyV3Response response = this.apiManageService.updateApiKeyV3(request.getBody());

            log.info("调用：updateApiKeyV3，返回");

            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.updateApiKeyV3 occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<Void> deleteApiKey(@RequestBody @Validated APIRequest<DeleteApiKeyRequest> request) throws Exception {
        try {
            log.info("正在调用：deleteApiKey，参数：{}", request.toString());

            this.apiManageService.deleteApiKey(request.getBody());

            log.info("调用：deleteApiKey，返回");
            return APIResponse.getOKJsonResult();
        } catch (Exception e) {
            log.warn("ApiController.deleteApiKey occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<EnableUpdateApiKeyResponse> enableUpdateApiKey(@RequestBody @Validated APIRequest<EnableUpdateApiKeyRequest> request) throws Exception {
        try {
            log.info("正在调用：enableUpdateApiKey，参数：{}", request.toString());

            EnableUpdateApiKeyResponse response = this.apiManageService.enableUpdateApiKey(request.getBody());

            log.info("调用：enableUpdateApiKey，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.enableUpdateApiKey occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<EnableApiWithdrawResponse> enableApiWithdraw(@RequestBody @Validated APIRequest<EnableApiWithdrawRequest> request) throws Exception {
        try {
            log.info("正在调用：enableApiWithdraw，参数：{}", request.toString());

            EnableApiWithdrawResponse response = this.apiManageService.enableApiWithdraw(request.getBody());

            log.info("调用：enableApiWithdraw，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.enableApiWithdraw occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<Void> deleteAllApiKey(@RequestBody @Validated APIRequest<DeleteAllApiKeyRequest> request) throws Exception {
        try {
            log.info("正在调用：deleteAllApiKey，参数：{}", request.toString());

            this.apiManageService.deleteAllApiKey(request.getBody());

            log.info("调用：deleteAllApiKey，返回");
            return APIResponse.getOKJsonResult();
        } catch (Exception e) {
            log.warn("ApiController.deleteAllApiKey occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<List<ApiModelResponse>> getApis(@RequestBody @Validated APIRequest<GetApisRequest> request) throws Exception {
        try {
            log.info("正在调用：getApis，参数：{}", request.toString());

            List<ApiModelResponse> response = this.apiManageService.getApis(request.getBody());

            log.info("调用：getApis，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.getApis occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<List<ApiModelResponse>> getApiList(@RequestBody @Validated APIRequest<GetApiListRequest> request) throws Exception {
        try {
            log.info("正在调用：getApiList，参数：{}", request.toString());

            List<ApiModelResponse> response = this.apiManageService.getApiList(request.getBody());

            log.info("调用：getApiList，返回");
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.getApiList occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<ApiModelResponse> modifyApiKeyIpRestrictSwitch(@RequestBody @Validated APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception {
        try {
            log.info("start call：modifyApiKeyIpRestrictSwitch，参数：{}", request.toString());
            ApiModelResponse response = apiManageService.modifyApiKeyIpRestrictSwitch(request.getBody());
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.modifyApiKeyIpRestrictSwitch occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<ApiModelResponse> deleteApiKeyRestrictIp(@RequestBody @Validated APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception {
        try {
            log.info("start call：deleteApiKeyRestrictIp，参数：{}", request.toString());
            ApiModelResponse response = apiManageService.deleteApiKeyRestrictIp(request.getBody());
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.deleteApiKeyRestrictIp occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<ApiModelResponse> addApiKeyRestrictIp(@RequestBody @Validated APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception {
        try {
            log.info("start call：addApiKeyRestrictIp，参数：{}", request.toString());
            ApiModelResponse response = apiManageService.addApiKeyRestrictIp(request.getBody());
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.addApiKeyRestrictIp occurs error", e);
            throw e;
        }
    }

    @Override
    public APIResponse<ApiModelResponse> queryApiKeyRestrictIp(@RequestBody @Validated APIRequest<UpdateApiKeyRestrictIpRequest> request) throws Exception {
        try {
            log.info("start call：queryApiKeyRestrictIp，参数：{}", request.toString());
            ApiModelResponse response = apiManageService.queryApiKeyRestrictIp(request.getBody());
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("ApiController.queryApiKeyRestrictIp occurs error", e);
            throw e;
        }
    }
}
