package com.binance.account.controller.withdraw;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.WithdrawPolicyApi;
import com.binance.account.service.withdraw.WithdrawPolicyService;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

/**
 * Created by Fei.Huang on 2019/1/14.
 */
@Log4j2
@RestController
public class WithdrawPolicyController implements WithdrawPolicyApi {

    @Resource
    private WithdrawPolicyService withdrawPolicyService;

    @Override
    public APIResponse<List<BigDecimal>> getDailyWithdrawLimits() throws Exception {

        return APIResponse.getOKJsonResult(withdrawPolicyService.getDailyWithdrawLimits());
    }

    @Override
    public APIResponse<BigDecimal> getDailyWithdrawLimitBySecurityLevel(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception {

        UserIdRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();

        return APIResponse.getOKJsonResult(withdrawPolicyService.getDailyWithdrawLimitBySecurityLevel(userId));
    }

    @Override
    public APIResponse<BigDecimal> getWithdrawReviewQuota(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception {

        UserIdRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();

        return APIResponse.getOKJsonResult(withdrawPolicyService.getWithdrawReviewQuota(userId));
    }

}
