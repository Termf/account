package com.binance.account.controller.certificate;

import com.binance.account.api.UserAddressApi;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserAddressQuery;
import com.binance.account.service.certificate.IUserAddress;
import com.binance.account.vo.user.UserAddressVo;
import com.binance.account.vo.user.request.AddressAuditRequest;
import com.binance.account.vo.user.request.UserAddressChangeStatusRequest;
import com.binance.account.vo.user.request.UserAddressRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserAddressController implements UserAddressApi {
    @Resource
    private IUserAddress address;


    @Override
    public APIResponse<SearchResult<UserAddressVo>> getList(@RequestBody() @Validated APIRequest<UserAddressQuery> request) throws Exception {
        return address.getList(request);
    }

    @Override
    public APIResponse<?> audit(@RequestBody() @Validated APIRequest<AddressAuditRequest> request) throws Exception {
        return address.audit(request);
    }

    @Override
    public APIResponse<?> submit(@RequestBody() @Validated APIRequest<UserAddressRequest> request) {
        return address.submit(request);
    }

    @Override
    public APIResponse<Void> updatePassedToExpired(@RequestBody() @Validated APIRequest<UserAddressChangeStatusRequest> request) {
        address.updatePassedToExpired(request.getBody());
        return APIResponse.getOKJsonResult();
    }
}
