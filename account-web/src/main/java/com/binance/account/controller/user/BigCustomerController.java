package com.binance.account.controller.user;

import com.binance.account.api.BigCustomerApi;
import com.binance.account.service.user.IBigCustomer;
import com.binance.account.vo.tag.response.EmailAndUserIdVo;
import com.binance.account.vo.tag.response.StatusAndUserIdVo;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BigCustomerController implements BigCustomerApi {

    @Autowired
    private IBigCustomer bigCustomer;

    @Override
    public APIResponse<List<EmailAndUserIdVo>> emailAndTag(@RequestBody() @Validated() APIRequest<GetUserListRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(bigCustomer.emailAndTag(request.getBody()));
    }

    @Override
    public APIResponse<List<StatusAndUserIdVo>> findUserStatus(@RequestBody() @Validated() APIRequest<GetUserListRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(bigCustomer.findUserStatus(request.getBody()));
    }

}
