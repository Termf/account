package com.binance.account.service.apimanage;

import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequest.VoidBody;
import com.binance.master.models.APIResponse;

public interface IBaseService {
    /**
     * 根据给定请求内容返回API请求对象
     * 
     * @param body
     * @param <T>
     * @return
     */
    public <T> APIRequest<T> newAPIRequest(T body);

    public <T> APIRequest<T> newAPIRequest(T body, String language);

    public APIRequest<VoidBody> newVoidAPIRequest();

    public APIRequest<VoidBody> newVoidAPIRequest(String language);

    /**
     * 解析返回相应返回值，如果相应出错，则抛出BusinessException
     * 
     * @param response
     * @param <T>
     * @return
     */
    public <T> T getAPIRequestResponse(APIResponse<T> response);

    public String getHttpBasePath();


}
