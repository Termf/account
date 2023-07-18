package com.binance.account.integration.mbxgateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.constants.enums.MatchBoxAccountRestrictionModeEnum;
import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.service.apimanage.impl.BaseServiceImpl;
import com.binance.account.utils.MatchboxReturnUtils;
import com.binance.account.vo.apimanage.ApiKeyVo;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.CouplingCalculationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.mbxgateway.api.IMatchboxApi;
import com.binance.mbxgateway.vo.request.matchbox.DeleteApiKeyRequest;
import com.binance.mbxgateway.vo.request.matchbox.DeleteApiKeyRuleRequest;
import com.binance.mbxgateway.vo.request.matchbox.GetAccountByExternalIdRequest;
import com.binance.mbxgateway.vo.request.matchbox.GetApiKeysRequest;
import com.binance.mbxgateway.vo.request.matchbox.PostAccountRequest;
import com.binance.mbxgateway.vo.request.matchbox.PostAccountRequestV3;
import com.binance.mbxgateway.vo.request.matchbox.PostApiKeyRequest;
import com.binance.mbxgateway.vo.request.matchbox.PostApiKeyRuleRequest;
import com.binance.mbxgateway.vo.request.matchbox.PutAccountRequestV3;
import com.binance.mbxgateway.vo.request.matchbox.PutApiKeyPermissionsRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Log4j2
@Service
public class MatchboxApiClient extends BaseServiceImpl {
    @Autowired
    private IMatchboxApi matchboxApi;

    /**
     * 创建撮合账户接口
     * 注意：
     * 这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
     */
    public Long postAccount(UserInfo tempUserInfo, MatchBoxAccountTypeEnum matchBoxAccountType) throws Exception {
        APIRequest<PostAccountRequest> originRequest = new APIRequest<PostAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        PostAccountRequest request = new PostAccountRequest();
        request.setExternalId(tempUserInfo.getUserId().toString());
        request.setMakerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getMakerCommission())));
        request.setTakerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getTakerCommission())));
        request.setBuyerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getBuyerCommission())));
        request.setSellerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getSellerCommission())));
        request.setAccountType(matchBoxAccountType.getAccountType());
        log.info("MatchboxApiClient.postAccount start：request={}", request);
        APIResponse<String> apiResponse = matchboxApi.postAccount(APIRequest.instance(originRequest, request));
        log.info("MatchboxApiClient.postAccount end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus() || StringUtils.isBlank(apiResponse.getData())) {
            log.error("MatchboxApiClient.postAccount : error" + apiResponse.getErrorData());
            throw new BusinessException("postAccount failed");
        }
        Map<String,String> resultMap=JsonUtils.toMap(apiResponse.getData(),String.class,String.class);
        return Long.valueOf(resultMap.get("accountId"));
    }


    /**
     * 创建撮合账户接口
     * 注意：
     * 这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
     */
    public Long postAccountWithRestrictionMode(UserInfo tempUserInfo, MatchBoxAccountTypeEnum matchBoxAccountType, MatchBoxAccountRestrictionModeEnum accountRestrictionModeEnum,String symbols,String permissionsBitmask) throws Exception {
        APIRequest<PostAccountRequest> originRequest = new APIRequest<PostAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        PostAccountRequestV3 request = new PostAccountRequestV3();
        request.setExternalId(tempUserInfo.getUserId().toString());
        request.setMakerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getMakerCommission())));
        request.setTakerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getTakerCommission())));
        request.setBuyerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getBuyerCommission())));
        request.setSellerCommission(String.valueOf(CouplingCalculationUtils.feeLong(tempUserInfo.getSellerCommission())));
        request.setAccountType(matchBoxAccountType.getAccountType());
        request.setRestrictionMode(accountRestrictionModeEnum.getRestrictionMode());
        String[] symbolarray={symbols};
        request.setSymbols(JsonUtils.toJsonNotNullKey(symbolarray));
        request.setPermissionsBitmask(permissionsBitmask);
        log.info("MatchboxApiClient.postAccountWithRestrictionMode start：request={}", request);
        APIResponse<String> apiResponse = matchboxApi.postAccountV3(APIRequest.instance(originRequest, request));
        log.info("MatchboxApiClient.postAccountWithRestrictionMode end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus() || StringUtils.isBlank(apiResponse.getData())) {
            log.error("MatchboxApiClient.postAccountWithRestrictionMode : error" + apiResponse.getErrorData());
            throw new BusinessException("postAccountWithRestrictionMode failed");
        }
        Long accountId=JSON.parseObject(apiResponse.getData()).getLong("accountId");
        return accountId;
    }

    public ApiKeyVo postApiKey(String accountId, String desc, String canAccessSecureWs,
                               String canControlUserStreams, String canTrade, String canViewMarketData,
                               String canViewUserData, String force, String publicKey) {
        try {
            PostApiKeyRequest req = new PostApiKeyRequest();
            req.setAccountId(accountId);
            req.setDesc(desc);
            req.setCanAccessSecureWs(canAccessSecureWs);
            req.setCanControlUserStreams(canControlUserStreams);
            req.setCanTrade(canTrade);
            req.setCanViewMarketData(canViewMarketData);
            req.setCanViewUserData(canViewUserData);
            req.setForce(force);
            req.setPublicKey(publicKey);
            String response = this.getAPIRequestResponse(matchboxApi.postApiKey(this.newAPIRequest(req)));
            return MatchboxReturnUtils.getMbxValue(response, ApiKeyVo.class);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway postApiKey失败:{}", e);
            throw new BusinessException("query account postApiKey fail");
        }
    }

    public void deleteApiKey(String accountId,  String keyId) {
        try {
            DeleteApiKeyRequest req = new DeleteApiKeyRequest();
            req.setAccountId(accountId);
            req.setKeyId(keyId);
            matchboxApi.deleteApiKey(this.newAPIRequest(req));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway deleteApiKey:{}", e);
            throw new BusinessException("query account deleteApiKey fail");
        }
    }

    public String getApiKeys(String accountId) {
        try {
            GetApiKeysRequest req = new GetApiKeysRequest();
            req.setAccountId(accountId);
            return this.getAPIRequestResponse(matchboxApi.getApiKeys(this.newAPIRequest(req)));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway getApiKeys:{}", e);
            throw new BusinessException("query account getApiKeys fail");
        }
    }

    public void postApiKeyRule(String accountId, String ip, String keyId) {
        try {
            PostApiKeyRuleRequest req = new PostApiKeyRuleRequest();
            req.setAccountId(accountId);
            req.setKeyId(keyId);
            req.setIp(ip);
            matchboxApi.postApiKeyRule(this.newAPIRequest(req));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway getApiKeys:{}", e);
            throw new BusinessException("query account getApiKeys fail");
        }
    }

    public void deleteApiKeyRule(String accountId, String keyId, String ruleId) {
        try {
            DeleteApiKeyRuleRequest req = new DeleteApiKeyRuleRequest();
            req.setAccountId(accountId);
            req.setKeyId(keyId);
            req.setRuleId(ruleId);
            matchboxApi.deleteApiKeyRule(this.newAPIRequest(req));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway getApiKeys:{}", e);
            throw new BusinessException("query account getApiKeys fail");
        }
    }

    public void putApiKeyPermissions(String accountId, String canAccessSecureWs,
                                     String canControlUserStreams, String canTrade, String canViewMarketData,
                                     String canViewUserData, String keyId, String force) {
        try {
            PutApiKeyPermissionsRequest req = new PutApiKeyPermissionsRequest();
            req.setAccountId(accountId);
            req.setCanAccessSecureWs(canAccessSecureWs);
            req.setCanControlUserStreams(canControlUserStreams);
            req.setCanTrade(canTrade);
            req.setCanViewMarketData(canViewMarketData);
            req.setCanViewUserData(canViewUserData);
            req.setKeyId(keyId);
            req.setForce(force);
            matchboxApi.putApiKeyPermissions(this.newAPIRequest(req));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway getApiKeys:{}", e);
            throw new BusinessException("query account getApiKeys fail");
        }
    }


    /**
     * 获取账户类型
     */
    public String getAccountTypeByUserId(Long userId) throws Exception {
        APIRequest<PostAccountRequest> originRequest = new APIRequest<PostAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetAccountByExternalIdRequest request = new GetAccountByExternalIdRequest();
        request.setExternalId(userId.toString());
        log.info("MatchboxApiClient.getAccountTypeByUserId start：request={}", request);
        APIResponse<String> apiResponse = matchboxApi.getAccountByExternalId(APIRequest.instance(originRequest, request));
        log.info("MatchboxApiClient.getAccountTypeByUserId end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus() || StringUtils.isBlank(apiResponse.getData())) {
            log.error("MatchboxApiClient.getAccountTypeByUserId : error" + apiResponse.getErrorData());
            throw new BusinessException("getAccountTypeByUserId failed");
        }
        JSONArray jsonArray = JSON.parseArray(apiResponse.getData());
        for (int i = 0;i<jsonArray.size();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String accountType = jsonObject.getString("accountType");
           return accountType;
        }
        return null;
    }

    public void putAccountPermission(String accountId, String permissionsBitmask) {
        try {
            PutAccountRequestV3 req = new PutAccountRequestV3();
            req.setAccountId(accountId);
            req.setPermissionsBitmask(permissionsBitmask);
            log.info("MatchboxApiClient.putAccountV3 start：request={}", req);
            this.getAPIRequestResponse(matchboxApi.putAccountV3(this.newAPIRequest(req)));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway putAccountV3:{}", e);
            throw new BusinessException("query account putAccountV3 fail");
        }
    }

}
