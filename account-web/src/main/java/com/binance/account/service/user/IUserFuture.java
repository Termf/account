package com.binance.account.service.user;

import com.binance.account.data.entity.futureagent.FutureUserAgent;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.vo.security.request.CreateFutureAccountRequest;
import com.binance.account.vo.security.request.FastCreateFutureAccountRequest;
import com.binance.account.vo.subuser.FuturePositionRiskVO;
import com.binance.account.vo.subuser.enums.MarginPeriodType;
import com.binance.account.vo.subuser.enums.SubAccountSummaryQueryType;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountSummaryResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountSummaryResp;
import com.binance.account.vo.user.CreateFutureUserResponse;
import com.binance.account.vo.user.FastCreateFutureUserResponse;
import com.binance.account.vo.user.request.CheckIfOpenFutureAccountRequest;
import com.binance.account.vo.user.request.GetUserIdByTradingAccountRequest;
import com.binance.account.vo.user.request.SendFutureClosePositionMsgRequest;
import com.binance.account.vo.user.request.SendFutureMarginCallRequest;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.FutureUserAgentResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.util.List;
import java.util.Map;


public interface IUserFuture {

    APIResponse<CreateFutureUserResponse> createFutureAccount(APIRequest<CreateFutureAccountRequest> request)
            throws Exception;

    APIResponse<Long> getFutureUserIdByFutureTradingAccount(APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception ;

    APIResponse<Long> getFutureUserIdByFutureDeliveryTradingAccount(APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception ;

    Boolean checkIfOpenFutureAccount(CheckIfOpenFutureAccountRequest request) throws Exception ;

    Boolean sendFutureClosePositionMsg(SendFutureClosePositionMsgRequest request) throws Exception ;

    Boolean sendFutureFundingRateMsg(SendFutureFundingRateMsgRequest request) throws Exception ;

    QuerySubAccountFutureAccountResp queryFuturesAccount(Long userId) throws Exception ;

    QuerySubAccountFutureAccountSummaryResp queryFuturesAccountSummary(UserInfo parentUserInfo, List<UserInfo> subUserInfoList, SubAccountSummaryQueryType subAccountSummaryQueryType,Integer page,Integer rows) throws Exception ;

    List<FuturePositionRiskVO> queryFuturesPositionRisk(Long userId) throws Exception ;

    QuerySubAccountMarginAccountSummaryResp queryMarginAccountSummary(UserInfo parentUserInfo, List<UserInfo> subUserInfoList,Integer page, Integer rows) throws Exception ;

    QuerySubAccountMarginAccountResp queryMarginAccount(User subUser,MarginPeriodType marginPeriodType) throws Exception ;

    Boolean sendFutureMarginCall(SendFutureMarginCallRequest request) throws Exception ;

    String createFutureUserAgent(FutureUserAgentReq futureUserAgentReq);

    FutureUserAgentResponse selectFutureUserAgent(CheckFutureAgentCodeExistReq checkFutureAgentCodeExistReq);

    void validateCretaeFutureAccount(FastCreateFutureAccountRequest request) throws Exception ;

    Map<Long,String> selectFutureAgentCodes(List<Long> body)throws Exception;

    Boolean checkIfCanUseFutreAgentCode(Long body)throws Exception;

    Boolean sendFutureCall(SendFutureCallRequest request) throws Exception ;

    APIResponse<CreateFutureUserResponse> createDeliveryAccountIfNotExist(APIRequest<IdRequest> request) throws Exception;

    Boolean fixDeliveryAccount(Long rootUserId) throws Exception;

    APIResponse<Void> syncApiKeyToDelivery(APIRequest<IdRequest> request);

}
