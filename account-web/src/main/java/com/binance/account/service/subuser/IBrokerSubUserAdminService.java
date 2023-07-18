package com.binance.account.service.subuser;

import com.binance.account.vo.subuser.BrokerUserCommisssionVo;
import com.binance.account.vo.subuser.request.AddOrUpdateBrokerUserCommissionRequest;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * Created by yangyang on 2019/8/21.
 */
public interface IBrokerSubUserAdminService {

    APIResponse<Boolean> enableBrokerSubUserFunction(APIRequest<ParentUserIdReq> request)throws Exception;

    APIResponse<Boolean> disableBrokerSubUserFunction(APIRequest<ParentUserIdReq> request)throws Exception;

    BrokerUserCommisssionVo getBrokerUserCommission(ParentUserIdReq request)throws Exception;

    Integer addOrUpdateBrokerUserCommission(AddOrUpdateBrokerUserCommissionRequest request)throws Exception;


}
