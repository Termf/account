/**
 * @Auther: zhenleisun
 * @Date: 2019-09-10 15:11
 * @Description: 出金地址校验。
 * 提供给 capital-web 使用，用于出金地址的校验。
 */
package com.binance.account.controller.withdraw;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.binance.account.api.CapitalInOutCheckApi;
import com.binance.account.service.capical.check.ICapitalCheck;
import com.binance.account.vo.withdraw.request.WithdrawAddressCheckRequest;
import com.binance.account.vo.withdraw.response.WithdrawAddressCheckResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName CapitalInOutCheckController
 * @Description
 * @Author zhenleisun
 * @Date 2019-09-10 15:11
 */
@Log4j2
@RestController
public class CapitalInOutCheckController implements CapitalInOutCheckApi {
    @Resource
    private ICapitalCheck capitalCheck;
    @Override
    @SentinelResource(value = "/capitalInOutCheck/address")
    public APIResponse<WithdrawAddressCheckResponse> checkWithdrawAddress(@Validated @RequestBody APIRequest<WithdrawAddressCheckRequest> request) {
        WithdrawAddressCheckRequest body = request.getBody();

        log.info("checkWithdrawAddress-request:[{}].", request);
        try {
            WithdrawAddressCheckResponse response = capitalCheck.getAddressBlackByAddress(body.getUserId(), body.getAsset(), body.getAddress(), body.getAmount());
            log.info("checkWithdrawAddress-response:[{}].", response);
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("withdraw address check failed " + body.getUserId(),e);
            return APIResponse.getErrorJsonResult("withdraw address check failed:" + e.getMessage());
        }
    }
}
