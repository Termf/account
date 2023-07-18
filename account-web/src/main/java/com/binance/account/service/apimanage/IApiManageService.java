package com.binance.account.service.apimanage;

import com.binance.account.vo.apimanage.request.*;
import com.binance.account.vo.apimanage.response.*;

import java.util.List;

public interface IApiManageService {

    PagingResult<ApiModelResponse> loadApiList(SearchApiRequest body) throws Exception;

    ApiModelResponse queryApiByUserAndApiKey(String userId, String apiKey) throws Exception;

    PagingResult<ApiModelResponse> loadDeletedApiList(SearchDeletedApiRequest body) throws Exception;

    ApiModelResponse saveApiKey(SaveApiKeyRequest body) throws Exception;

    SaveApiKeyV2Response saveApiKeyV2(SaveApiKeyV2Request request) throws Exception;

    void deleteApiKey(DeleteApiKeyRequest body) throws Exception;

    void deleteAllApiKey(DeleteAllApiKeyRequest body) throws Exception;

    UpdateApiKeyResponse updateApiKey(UpdateApiKeyRequest request) throws Exception;

    UpdateApiKeyResponse updateApiKeyV2(UpdateApiKeyRequest request) throws Exception;

    UpdateApiKeyV3Response updateApiKeyV3(UpdateApiKeyV3Request request) throws Exception;

    EnableUpdateApiKeyResponse enableUpdateApiKey(EnableUpdateApiKeyRequest request) throws Exception;

    ApiModelResponse enableApiCreate(EnableApiCreateRequest body) throws Exception;

    EnableApiWithdrawResponse enableApiWithdraw(EnableApiWithdrawRequest body) throws Exception;

    List<ApiModelResponse> getApis(GetApisRequest body) throws Exception;

    List<ApiModelResponse> getApiList(GetApiListRequest body) throws Exception;

    void updateDBApikey() throws Exception;

    void deleteApiAndOrders(String userId);

    String fetchAntiCode(Long userId) throws Exception;

    ApiModelResponse modifyApiKeyIpRestrictSwitch(UpdateApiKeyRestrictIpRequest request) throws Exception;

    ApiModelResponse addApiKeyRestrictIp(UpdateApiKeyRestrictIpRequest request) throws Exception;

    ApiModelResponse deleteApiKeyRestrictIp(UpdateApiKeyRestrictIpRequest request) throws Exception;

    ApiModelResponse queryApiKeyRestrictIp(UpdateApiKeyRestrictIpRequest request) throws Exception;
}
