package com.binance.account.integration.capital;

import com.alibaba.fastjson.JSONObject;
import com.binance.capital.api.WithdrawApi;
import com.binance.capital.vo.withdraw.request.GetWithdrawCountRequest;
import com.binance.capital.vo.withdraw.request.UserIdRequest;
import com.binance.capital.vo.withdraw.response.GetWithdrawMessageResponse;
import com.binance.capital.vo.withdraw.vo.WithdrawVo;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class CapitalClient {

    @Autowired
    private WithdrawApi withdrawApi;

    public GetWithdrawMessageResponse getWithdrawMessage(Long userId){
        log.info("CapitalClient.getWithdrawMessage.userId:{}",userId);
        APIRequest<UserIdRequest> request = new APIRequest<>();
        UserIdRequest userIdRequest = new UserIdRequest();
        userIdRequest.setUserId(userId);
        request.setBody(userIdRequest);
        try {
            APIResponse<GetWithdrawMessageResponse> response = withdrawApi.getWithdrawMessage(request);
            if (APIResponse.Status.OK == response.getStatus()) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("CapitalClient.getWithdrawMessage error:{}",e);
        }
        return null;
    }

    public Boolean cancelUserAllWithdrawing(Long userId){
        log.info("CapitalClient.cancelUserAllWithdrawing.userId:{}",userId);
        APIRequest<UserIdRequest> request = new APIRequest<>();
        UserIdRequest userIdRequest = new UserIdRequest();
        userIdRequest.setUserId(userId);
        request.setBody(userIdRequest);
        try {
            APIResponse<Boolean> response = withdrawApi.cancelUserAllWithdrawing(request);
            if (APIResponse.Status.OK == response.getStatus()) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("CapitalClient.cancelUserAllWithdrawing error:{}",e);
        }
        return null;
    }

    public List<WithdrawVo> withdrawList(GetWithdrawCountRequest getWithdrawCountRequest){
        log.info("CapitalClient.getWithdrawPage.request:{}", JSONObject.toJSONString(getWithdrawCountRequest));
        APIRequest<GetWithdrawCountRequest> request = new APIRequest<>();
        request.setBody(getWithdrawCountRequest);
        try {
            APIResponse<List<WithdrawVo>> response = withdrawApi.getWithdrawPage(request);
            // 如果使用该方法时查询数据分页较大，可以去掉response日志
            log.info("CapitalClient.getWithdrawPage.response:{}", JSONObject.toJSONString(response));
            if (APIResponse.Status.OK == response.getStatus()) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("CapitalClient.getWithdrawPage error:{}",e);
        }
        return null;
    }

}
