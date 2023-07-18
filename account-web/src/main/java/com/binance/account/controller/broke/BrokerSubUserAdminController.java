package com.binance.account.controller.broke;

import com.binance.account.api.BrokerSubUserAdminApi;
import com.binance.account.service.subuser.IBrokerSubUserAdminService;
import com.binance.account.vo.subuser.BrokerUserCommisssionVo;
import com.binance.account.vo.subuser.request.AddOrUpdateBrokerUserCommissionRequest;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yangyang on 2019/8/21.
 */
@RestController
public class BrokerSubUserAdminController implements BrokerSubUserAdminApi {

    @Autowired
    private IBrokerSubUserAdminService brokerSubUserAdminService;

    @Override
    public APIResponse<Boolean> enableBrokerSubUserFunction(@RequestBody @Validated APIRequest<ParentUserIdReq> request) throws Exception {
        return brokerSubUserAdminService.enableBrokerSubUserFunction(request);
    }

    @Override
    public APIResponse<Boolean> disableBrokerSubUserFunction(@RequestBody @Validated APIRequest<ParentUserIdReq> request) throws Exception {
        return brokerSubUserAdminService.disableBrokerSubUserFunction(request);
    }

    @Override
    public APIResponse<BrokerUserCommisssionVo> getBrokerUserCommission(@RequestBody @Validated APIRequest<ParentUserIdReq> request) throws Exception {
        ParentUserIdReq requestBody=request.getBody();
        BrokerUserCommisssionVo resultVo=brokerSubUserAdminService.getBrokerUserCommission(requestBody);
        return APIResponse.getOKJsonResult(resultVo);
    }

    @Override
    public APIResponse<Integer> addOrUpdateBrokerUserCommission(@RequestBody @Validated APIRequest<AddOrUpdateBrokerUserCommissionRequest> request) throws Exception {
        AddOrUpdateBrokerUserCommissionRequest requestBody=request.getBody();
        Integer result=brokerSubUserAdminService.addOrUpdateBrokerUserCommission(requestBody);
        return APIResponse.getOKJsonResult(result);
    }
}
