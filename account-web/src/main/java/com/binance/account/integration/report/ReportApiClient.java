package com.binance.account.integration.report;

import com.binance.account.data.entity.agent.UserAgentRate;
import com.binance.account.data.mapper.agent.UserAgentRateMapper;
import com.binance.master.commons.SearchResult;
import com.binance.master.utils.JsonUtils;
import com.binance.report.api.IUserCommissionApi;
import com.binance.report.api.RebateAssistApi;
import com.binance.report.request.CommissionIncomeRequest;
import com.binance.report.vo.accountsnapshot.RefferalEmailVo;
import com.binance.report.vo.asset.UserCommissionDetailResponse;
import com.binance.report.vo.user.SelectUserIdAgentNumVo;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.c2c.api.FiatUserApi;
import com.binance.c2c.vo.user.request.CreateFiatUserReq;
import com.binance.margin.api.bookkeeper.request.CreateMarginAccountRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;

import lombok.extern.log4j.Log4j2;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ReportApiClient {

    @Autowired
    private IUserCommissionApi iUserCommissionApi;
    @Autowired
    private RebateAssistApi rebateAssistApi;
    @Autowired
    private UserAgentRateMapper userAgentRateMapper;


    public Map<Long,Long> selectUserAgentNumByUserIds(List<Long> userIds) throws Exception {
        log.info("ReportApiClient.selectUserAgentNumByUserIds.userIds:{}",userIds);
        if (CollectionUtils.isEmpty(userIds) || userIds.size() <= 0){
            return Maps.newHashMap();
        }
        APIResponse<Map<Long,Long>> apiResponse = iUserCommissionApi.selectUserAgentNumByUserIds(APIRequest.instance(userIds));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("ReportApiClient.selectUserAgentNumByUserIds :userIds=" + userIds + "  error"
                    + apiResponse.getErrorData());
            throw new BusinessException("selectUserAgentNumByUserIds failed");
        }
        return apiResponse.getData();
    }

    public SearchResult<String> selectRefferalEmailByAgentId(Long userId,Integer start, Integer offset) throws Exception {
        log.info("ReportApiClient.selectRefferalEmailByAgentId.userId:{},start:{},offset:{}",userId,start,offset);
        if (userId == null || start == null || offset == null){
            return new SearchResult<>();
        }
        RefferalEmailVo refferalEmailVo = new RefferalEmailVo();
        refferalEmailVo.setStart(start);
        refferalEmailVo.setOffset(offset);
        refferalEmailVo.setUserId(userId);
        APIResponse<SearchResult<String>> apiResponse = iUserCommissionApi.selectRefferalEmailByAgentId(APIRequest.instance(refferalEmailVo));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("ReportApiClient.selectRefferalEmailByAgentId :userId=" + userId + "  error"
                    + apiResponse.getErrorData());
            throw new BusinessException("selectRefferalEmailByAgentId failed");
        }
        return apiResponse.getData();
    }

    public Map<String,Long> selectUserAgentNumByAgentCodes(List<String> agentCodes) throws Exception {
        log.info("ReportApiClient.selectUserAgentNumByAgentCodes.agentCodes:{}",agentCodes);
        if (CollectionUtils.isEmpty(agentCodes) || agentCodes.size() <= 0){
            return Maps.newHashMap();
        }
        APIResponse<Map<String,Long>> apiResponse = iUserCommissionApi.selectUserAgentNumByAgentCodes(APIRequest.instance(agentCodes));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("ReportApiClient.selectUserAgentNumByAgentCodes :agentCodes=" + agentCodes + "  error"
                    + apiResponse.getErrorData());
            throw new BusinessException("selectUserAgentNumByAgentCodes failed");
        }
        return apiResponse.getData();
    }

    public Integer selectOldAgentReffrNums(Long userId) throws Exception {
        log.info("ReportApiClient.selectOldAgentReffrNums.userId:{}",userId);
        if (userId ==  null){
            return 0;
        }
        Map<String, Object> searchParam = Maps.newHashMap();
        searchParam.put("userId", userId);
        List<UserAgentRate> allUserAgentRates = userAgentRateMapper.selectByUserIdAgentCode(searchParam);
        if (CollectionUtils.isEmpty(allUserAgentRates)){
            return 0;
        }
        List<String> agentCodes = allUserAgentRates.stream().map(UserAgentRate::getAgentCode).collect(Collectors.toList());
        agentCodes.remove(String.valueOf(userId));
        SelectUserIdAgentNumVo vo = new SelectUserIdAgentNumVo();
        vo.setAgentCodes(agentCodes);
        vo.setUserId(userId);
        APIResponse<Integer> apiResponse = iUserCommissionApi.selectOldAgentReffrNums(APIRequest.instance(vo));
        log.info("ReportApiClient.selectOldAgentReffrNums :userId=" + userId + ",response="
                + JsonUtils.toJsonHasNullKey(apiResponse));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("ReportApiClient.selectOldAgentReffrNums :agentCodes=" + agentCodes + "  error"
                    + apiResponse.getErrorData());
            throw new BusinessException("selectOldAgentReffrNums failed");
        }
        return apiResponse.getData();
    }

    public SearchResult<UserCommissionDetailResponse> selectBrokerCommissionDetail(Long userId, Date startTime,Date endTime,Integer page, Integer offset,Integer accountType) throws Exception {
        log.info("ReportApiClient.selectRefferalEmailByAgentId.userId:{},startTime:{},endTime:{},offset:{}",userId,startTime,endTime,offset);
        if (userId == null || startTime == null || endTime == null || offset == null){
            return new SearchResult<>();
        }
        CommissionIncomeRequest commissionIncomeRequest = new CommissionIncomeRequest();
        commissionIncomeRequest.setStartTime(startTime);
        commissionIncomeRequest.setEndTime(endTime);
        commissionIncomeRequest.setUserId(userId);
        commissionIncomeRequest.setPage(page);
        commissionIncomeRequest.setAccountType(accountType);
        commissionIncomeRequest.setRows(offset);
        APIResponse<SearchResult<UserCommissionDetailResponse>> apiResponse = rebateAssistApi.selectUserCommissionDetail(APIRequest.instance(commissionIncomeRequest));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("ReportApiClient.selectBrokerCommissionDetail :userId=" + userId + "  error"
                    + apiResponse.getErrorData());
            throw new BusinessException("selectBrokerCommissionDetail failed");
        }
        return apiResponse.getData();
    }
}
