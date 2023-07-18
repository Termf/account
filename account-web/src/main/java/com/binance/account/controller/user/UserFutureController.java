package com.binance.account.controller.user;

import com.binance.account.api.UserFutureApi;
import com.binance.account.constants.AccountConstants;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.IUserFuture;
import com.binance.account.vo.security.request.CreateFutureAccountRequest;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.security.request.FastCreateFutureAccountRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.CreateFutureUserResponse;
import com.binance.account.vo.user.FastCreateFutureUserResponse;
import com.binance.account.vo.user.request.CheckIfOpenFutureAccountRequest;
import com.binance.account.vo.user.request.GetUserIdByTradingAccountRequest;
import com.binance.account.vo.user.request.SendFutureClosePositionMsgRequest;
import com.binance.account.vo.user.request.SendFutureMarginCallRequest;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.CreateMarginUserResponse;
import com.binance.account.vo.user.response.FutureUserAgentResponse;
import com.binance.account.vo.user.response.RegisterUserResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import com.binance.master.utils.WebUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Monitored
@Log4j2
public class UserFutureController implements UserFutureApi {

    @Autowired
    private IUserFuture userFuture;

    @Resource
    private IUser iUser;


    @Override
    public APIResponse<CreateFutureUserResponse> createFutureAccount(@RequestBody @Validated APIRequest<CreateFutureAccountRequest> request) throws Exception {
        return userFuture.createFutureAccount(request);
    }

    @Override
    public APIResponse<Long> getFutureUserIdByFutureTradingAccount(@RequestBody @Validated APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception {
        return  userFuture.getFutureUserIdByFutureTradingAccount(request);
    }

    @Override
    public APIResponse<Long> getFutureUserIdByFutureDeliveryTradingAccount(@RequestBody @Validated APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception {
        return  userFuture.getFutureUserIdByFutureDeliveryTradingAccount(request);
    }

    @Override
    public APIResponse<Boolean> checkIfOpenFutureAccount(@RequestBody @Validated APIRequest<CheckIfOpenFutureAccountRequest> request) throws Exception {
        Boolean flag=userFuture.checkIfOpenFutureAccount(request.getBody());
        return APIResponse.getOKJsonResult(flag);
    }

    @Override
    public APIResponse<Boolean> sendFutureClosePositionMsg(@RequestBody @Validated APIRequest<SendFutureClosePositionMsgRequest> request) throws Exception {
        Boolean flag=userFuture.sendFutureClosePositionMsg(request.getBody());
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<Boolean> sendFutureMarginCall(@RequestBody @Validated APIRequest<SendFutureMarginCallRequest> request) throws Exception {
        Boolean flag=userFuture.sendFutureMarginCall(request.getBody());
        return APIResponse.getOKJsonResult(true);
    }
    public APIResponse<String> createFutureUserAgent(@RequestBody @Validated APIRequest<FutureUserAgentReq> request)
            throws Exception {
        String futureUserRefferCode = userFuture.createFutureUserAgent(request.getBody());
        return APIResponse.getOKJsonResult(futureUserRefferCode);
    }

    @Override
    public APIResponse<FutureUserAgentResponse> selectFutureUserAgent(@RequestBody @Validated APIRequest<CheckFutureAgentCodeExistReq> request)
            throws Exception {
        FutureUserAgentResponse result = userFuture.selectFutureUserAgent(request.getBody());
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<Boolean> sendFutureFundingRateMsg(@RequestBody @Validated APIRequest<SendFutureFundingRateMsgRequest> request)
            throws Exception{
        Boolean flag=userFuture.sendFutureFundingRateMsg(request.getBody());
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<Map<Long,String>> selectFutureAgentCodes(@RequestBody @Validated APIRequest<List<Long>> request)
            throws Exception {
        Map<Long,String> result = userFuture.selectFutureAgentCodes(request.getBody());
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<Boolean> checkIfCanUseFutreAgentCode(@RequestBody @Validated APIRequest<Long> request)
            throws Exception {
        Boolean result = userFuture.checkIfCanUseFutreAgentCode(request.getBody());
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<Boolean> sendFutureCall(@RequestBody @Validated APIRequest<SendFutureCallRequest> request) throws Exception {
        Boolean flag=userFuture.sendFutureCall(request.getBody());
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<CreateFutureUserResponse> createDeliveryAccountIfNotExist(@RequestBody @Validated APIRequest<IdRequest> request) throws Exception {
        return userFuture.createDeliveryAccountIfNotExist(request);
    }

    @Override
    public APIResponse<Void> syncApiKeyToDelivery(@RequestBody @Validated APIRequest<IdRequest> request) throws Exception {
        return userFuture.syncApiKeyToDelivery(request);
    }

}
