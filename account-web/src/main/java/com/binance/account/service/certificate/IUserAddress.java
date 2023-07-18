package com.binance.account.service.certificate;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserAddressQuery;
import com.binance.account.vo.user.UserAddressVo;
import com.binance.account.vo.user.request.AddressAuditRequest;
import com.binance.account.vo.user.request.UserAddressChangeStatusRequest;
import com.binance.account.vo.user.request.UserAddressRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface IUserAddress {

    APIResponse<SearchResult<UserAddressVo>> getList(@RequestBody() APIRequest<UserAddressQuery> request) throws Exception;

    APIResponse<?> audit(@RequestBody() APIRequest<AddressAuditRequest> request);

    APIResponse<?> submit(@RequestBody() APIRequest<UserAddressRequest> request);

    void updatePassedToExpired(@RequestBody() UserAddressChangeStatusRequest request);
}
