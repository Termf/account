package com.binance.account.integration.mbxgateway;

import java.util.List;

import com.binance.mbxgateway.vo.request.GetAccountRequest;
import com.binance.mbxgateway.vo.response.TradingAccountResponseV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import com.binance.mbxgateway.api.IAccountApi;
import com.binance.mbxgateway.vo.ApiKeyInfoVo;
import com.binance.mbxgateway.vo.request.GetApiInfoRequest;
import com.binance.mbxgateway.vo.request.SetGasRequest;
import com.binance.mbxgateway.vo.request.matchbox.PostAccountRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class AccountApiClient {
    @Autowired
    private IAccountApi accountApi;

    /**
     * 设置燃烧bnb的开关
     */
    public void setGas(String userId, Boolean isUseBnbFee) throws Exception {
        APIRequest<PostAccountRequest> originRequest = new APIRequest<PostAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        SetGasRequest request = new SetGasRequest();
        request.setUserId(userId);
        request.setIsUseBnbFee(isUseBnbFee);
        log.info("AccountApiClient.setGas start：request={}", request);
        APIResponse<Void> apiResponse = accountApi.setGas(APIRequest.instance(originRequest, request));
        log.info("AccountApiClient.setGas end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("AccountApiClient.setGas : error" + apiResponse.getErrorData());
            throw new BusinessException("setGas failed");
        }
    }


    /**
     * 获取用户api key信息
     */
    public List<ApiKeyInfoVo> getApiInfo(String userId) throws Exception {
        APIRequest<GetApiInfoRequest> originRequest = new APIRequest<GetApiInfoRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetApiInfoRequest request = new GetApiInfoRequest();
        request.setUserId(userId);
        log.info("AccountApiClient.getApiInfo start：request={}", request);
        APIResponse<List<ApiKeyInfoVo>> apiResponse = accountApi.getApiInfo(APIRequest.instance(originRequest, request));
        log.info("AccountApiClient.getApiInfo end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("AccountApiClient.getApiInfo : error" + apiResponse.getErrorData());
            throw new BusinessException("getApiInfo failed");
        }
        return apiResponse.getData();
    }

    /**
     * 获取 account信息
     * mbx的accountByExternalId接口生产环境第一次查询比较慢，不建议常规用户流程使用，可用getAccountV3替代
     */
    public TradingAccountResponseV3 getAccountByExternalIdV3(String userId) {
        try {
            APIRequest<GetAccountRequest> originRequest = new APIRequest<GetAccountRequest>();
            originRequest.setLanguage(LanguageEnum.ZH_CN);
            originRequest.setTerminal(TerminalEnum.WEB);
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            GetAccountRequest request = new GetAccountRequest();
            request.setUserId(userId);
            log.info("AccountApiClient.getAccountByExternalIdV3 start：request={}", request);
            APIResponse<TradingAccountResponseV3> apiResponse = accountApi.getAccountByExternalIdV3(APIRequest.instance(originRequest, request));
            log.info("AccountApiClient.getAccountByExternalIdV3 end ：request={},response={}", request, apiResponse);
            if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
                log.error("AccountApiClient.getAccountByExternalIdV3 : error" + apiResponse.getErrorData());
                throw new BusinessException("getAccountByExternalIdV3 failed");
            }
            return apiResponse.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway getAccountByExternalIdV3失败:{}", e);
            throw new BusinessException("query mbx getAccountByExternalIdV3 fail");
        }
    }

    /**
     * 获取 account信息
     * 接口会先从user_info表中查tradingAccount, 新激活用户需考虑读写库同步延迟
     */
    public TradingAccountResponseV3 getAccountV3(String userId) {
        try {
            APIRequest<GetAccountRequest> originRequest = new APIRequest<GetAccountRequest>();
            originRequest.setLanguage(LanguageEnum.ZH_CN);
            originRequest.setTerminal(TerminalEnum.WEB);
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            GetAccountRequest request = new GetAccountRequest();
            request.setUserId(userId);
            log.info("AccountApiClient.getAccountV3 start：request={}", request);
            APIResponse<TradingAccountResponseV3> apiResponse = accountApi.getAccountV3(APIRequest.instance(originRequest, request));
            log.info("AccountApiClient.getAccountV3 end ：request={},response={}", request, apiResponse);
            if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
                log.error("AccountApiClient.getAccountV3 : error" + apiResponse.getErrorData());
                throw new BusinessException("getAccountV3 failed");
            }
            return apiResponse.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用mbx-gateway getAccountV3:{}", e);
            throw new BusinessException("query mbx getAccountV3 fail");
        }
    }
}
