package com.binance.account.integration.futureengine;

import com.binance.account.constants.AccountConstants;
import com.binance.master.constant.Constant;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.account.domain.bo.FutureRes;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import com.binance.memgmt.api.client.BinanceApiClientFactory;
import com.binance.memgmt.api.client.MgmtApiRestClient;
import com.binance.memgmt.api.client.domain.account.Account;
import com.binance.memgmt.api.client.domain.account.request.FeeAdjustRequest;
import com.binance.memgmt.api.client.domain.account.request.OrderOperationRequest;
import com.binance.memgmt.api.client.domain.apiKey.ApiKey;
import com.binance.memgmt.api.client.domain.apiKey.ApiKeyResponse;
import com.binance.memgmt.api.client.domain.apiKey.UpdateApiKeyRule;
import com.binance.memgmt.api.client.domain.general.CommissionResponse;
import com.binance.memgmt.api.client.domain.general.FeeAdjustResponse;
import com.binance.memgmt.api.client.domain.general.NewSymbolResponse;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class FutureAccountApiClient {

    @Value("${future.mgmt.api.url:}")
    private String MGMT_API_URL;

    private static final String SUCCESS = "msg: success";
    MgmtApiRestClient client = null;

    @PostConstruct
    public void init() {
        try{
            BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
            client = factory.newRestClient(MGMT_API_URL);
            log.info("initializing MgmtApiRestClient MGMT_API_URL : " + MGMT_API_URL);
            log.info("initializing MgmtApiRestClient result + " + client);
        }catch (Exception e){
            log.error("initializing MgmtApiRestClient error :",e);
        }

    }

    public Long createAccount(Long externalId,Integer tradeLevel) {
        Account account = Account
                .builder()
                .externalId(externalId.longValue())
                .feeTier(tradeLevel.intValue())
                .build();
        Account result = client.newAccount(account);
        log.info("Call mgmt to create future account with result -{}", result);
        return new Long(result.getAccountId());
    }

    public Long updateAccount(Long externalId,boolean enable) {
        Account account = Account
                .builder()
                .externalId(externalId.longValue())
                .canTrade(enable)
                .build();
        Account result = client.newAccount(account);
        log.info("Call mgmt to create updateAccount account with result -{}", result);
        return new Long(result.getAccountId());
    }

    public void cancelFutureOpenOrders(String accountId) {
        //todo 结果检验
        OrderOperationRequest orderOperationRequest = new OrderOperationRequest(accountId,"1");
        Map<String,Object> result = (Map<String, Object>) client.cancelOpenOrders(orderOperationRequest);
        log.info("Call mgmt to  cancelFutureOpenOrders with result -{}", JsonUtils.toJsonHasNullKey(result));
        if (result == null || result.get("msg") == null || !FutureRes.SUCCESS.equals(result.get("msg"))){
            throw new BusinessException("cancelFutureOpenOrders error,accountId:{}",accountId);
        }
    }

    /**
     * 同步期货的apikey
     */
    public void createFutureApiKey(Long futureAccountId, String apiKey, Long keyId, String apiKeySecret, String desc) {
        ApiKey apiKeyRequest = ApiKey.builder().accountId(futureAccountId.intValue())
                .apiKey(apiKey).keyId(keyId.longValue()).apiKeySecret(apiKeySecret).desc(desc)
                .canTrade(false).canViewUserData(true).canControlUserStreams(true).canViewMarketData(true).canAccessSecureWs(true).build();
        String result = client.newApiKey(apiKeyRequest);
        log.info("Call mgmt to createFutureApiKey with result -{}", result);
    }

    /**
     * 同步期货的apikey
     */
    public void updateApiKeyPermissions(Long futureAccountId, Long keyId, Boolean force, Boolean canTrade, Boolean canViewUserData,
                                          Boolean canControlUserStreams, Boolean canViewMarketData, Boolean canAccessSecureWs) {
        client.updateApiKeyPermissions(futureAccountId.intValue(), keyId, force, canTrade, canViewUserData, canControlUserStreams, canViewMarketData, canAccessSecureWs);
        log.info("Call mgmt to updateApiKeyPermissions with result");

    }

    /**
     *  删除期货的apikey
     */
    public void deleteApiKey(Long futureAccountId, Long keyId) {
        client.deleteApiKey(futureAccountId.intValue(),keyId);
        log.info("Call mgmt to deleteApiKey with result");
    }


    /**
     *  调整期货手续费（某个交易对的）
     *  这四个参数都是必填
     */
    public FeeAdjustResponse feeAdjust(Long futureAccountId, String symbol,Integer makerCommission,Integer takerCommission) {
        FeeAdjustRequest feeAdjustRequest=new FeeAdjustRequest();
        feeAdjustRequest.setAccountId(futureAccountId.intValue());
        feeAdjustRequest.setSymbol(symbol);
        feeAdjustRequest.setMakerCommission(makerCommission);
        feeAdjustRequest.setTakerCommission(takerCommission);
        log.info("Call mgmt to feeAdjust with request：feeAdjustRequest={}",JsonUtils.toJsonNotNullKey(feeAdjustRequest));
        FeeAdjustResponse feeAdjustResponse= client.feeAdjust(feeAdjustRequest);
        log.info("Call mgmt to feeAdjust with result：feeAdjustResponse={}",JsonUtils.toJsonNotNullKey(feeAdjustResponse));
        return feeAdjustResponse;

    }
    /**
     *  获取某一个或者所有交易对的信息
     */
    public List<NewSymbolResponse> getAllSymbols(String symbol){
        log.info("Call mgmt to getAllSymbols with request：symbol={}",symbol);
        List<NewSymbolResponse> newSymbolResponseList= client.getAllSymbols(symbol);
        log.info("Call mgmt to getAllSymbols with result：newSymbolResponseList={}",JsonUtils.toJsonNotNullKey(newSymbolResponseList));
        return newSymbolResponseList;
    }


    /**
     *  获取某一个或者所有交易对的信息,带缓存的
     */
    public List<NewSymbolResponse> getAllSymbolsCache(String symbol){
        log.info("Call mgmt to getAllSymbols with request：symbol={}",symbol);
        String allSymbolStr = RedisCacheUtils.get(AccountConstants.ACCOUNT_FUTURE_SYMBOL_ALL_KEY);
        if(StringUtils.isBlank(allSymbolStr)){
            List<NewSymbolResponse> newSymbolResponseList= getAllSymbols(symbol);
            RedisCacheUtils.set(AccountConstants.ACCOUNT_FUTURE_SYMBOL_ALL_KEY,JsonUtils.toJsonNotNullKey(newSymbolResponseList),Constant.MINUTE_5);
            return newSymbolResponseList;
        }else{
            List<NewSymbolResponse> newSymbolResponseList=JsonUtils.toObjList(allSymbolStr,NewSymbolResponse.class);
            if(StringUtils.isNotBlank(symbol)){
                for(NewSymbolResponse newSymbolResponse:newSymbolResponseList){
                    if(symbol.equals(newSymbolResponse.getSymbol())){
                        return Lists.newArrayList(newSymbolResponse);
                    }
                }
                return Lists.newArrayList();
            }
            return newSymbolResponseList;
        }
    }

    /**
     *  查询某个用户某个交易对的手续费信息，两个入参都是必填
     */
    public CommissionResponse getCommission(String symbol,Long futureAccountId){
        log.info("Call mgmt to getCommission with request：symbol={},futureAccountId={}",symbol,futureAccountId);
        CommissionResponse commissionResponse= client.getCommission(symbol,futureAccountId.intValue());
        log.info("Call mgmt to getCommission with result：commissionResponse={}",JsonUtils.toJsonNotNullKey(commissionResponse));
        return commissionResponse;
    }


    /**
     *  查询某个用户所有交易对的手续费信息
     */
    public List<CommissionResponse> getCommissionByFutureAccountId(Long futureAccountId,Boolean useCache){
        log.info("Call mgmt to getCommissionByFutureAccountId with request：futureAccountId={},useCache={}",futureAccountId,useCache);
        List<NewSymbolResponse> newSymbolResponseList=Lists.newArrayList();
        if(useCache){
            newSymbolResponseList= getAllSymbolsCache(null);
        }else{
            newSymbolResponseList= getAllSymbols(null);
        }
        if(CollectionUtils.isEmpty(newSymbolResponseList)){
            return Lists.newArrayList();
        }
        List<CommissionResponse> commissionResponseList=Lists.newArrayList();
        for(NewSymbolResponse newSymbolResponse:newSymbolResponseList){
            try{
                CommissionResponse commissionResponse=getCommission(newSymbolResponse.getSymbol(),futureAccountId);
                if(null!=commissionResponse){
                    commissionResponseList.add(commissionResponse);
                }
            }catch (Exception e){
                log.warn("Call mgmt to getCommissionByFutureAccountId single error",e);
            }

        }
        log.info("Call mgmt to getCommissionByFutureAccountId with result：commissionResponseList={}",JsonUtils.toJsonNotNullKey(commissionResponseList));
        return commissionResponseList;
    }


    /**
     *  获取所有交易对的信息,带缓存的
     *  key : symbolId
     *  value:NewSymbolResponse
     */
    public Map<Integer,NewSymbolResponse> getAllSymbolMap(){
        log.info("Call mgmt to getAllSymbolMap with request");
        List<NewSymbolResponse> newSymbolResponseList=getAllSymbolsCache(null);
        Map<Integer,NewSymbolResponse> responseMap= Maps.uniqueIndex(newSymbolResponseList, new Function<NewSymbolResponse, Integer>() {
            @Override
            public Integer apply(@Nullable NewSymbolResponse newSymbolResponse) {
                return newSymbolResponse.getSymbolId();
            }
        });
        return responseMap;
    }


    public List<ApiKeyResponse> getApiKeys(Long futureAccountId){
        log.info("Call mgmt to getApiKeys with request：futureAccountId={}",futureAccountId);
        List<ApiKeyResponse> apiKeys = client.getApiKeys(futureAccountId.intValue());
        log.info("Call mgmt to getApiKeys with result：apiKeys={}", LogMaskUtils.maskJsonString2(JsonUtils.toJsonNotNullKey(apiKeys)));
        return apiKeys;
    }

    public void updateApiKeyRules(Long keyId, Integer accountId, String ip){
        log.info("Call mgmt to updateApiKeyRules with request：keyId={} accountId={} ip={}",keyId,accountId,ip);
        UpdateApiKeyRule updateApiKeyRule = UpdateApiKeyRule.builder()
                .keyId(keyId)
                .accountId(accountId)
                .ipList(ip)
                .build();

        client.updateApiKeyRules(updateApiKeyRule);
        log.info("Call mgmt to updateApiKeyRules success");
    }

}
