package com.binance.account.service.certificate.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.common.constant.RiskRatingConst;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserRiskRatingQuery;
import com.binance.account.data.entity.certificate.UserAddress;
import com.binance.account.data.entity.certificate.UserRiskRating;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.UserRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserWckAuditMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.certificate.IUseRiskRating;
import com.binance.account.vo.certificate.UserWckAuditVo;
import com.binance.account.vo.certificate.UserWckAuditVo.UserWckAuditLogVo;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.UserKycApproveVo;
import com.binance.account.vo.user.UserRiskRatingVo;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UserRiskRatingBusiness implements IUseRiskRating {

    @Autowired
    private UserWckAuditMapper wckAuditMapper;

    @Autowired
    private UserKycBusiness userKycBusiness;

    @Autowired
    private UserRiskRatingMapper userRiskRatingMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserIndexMapper userIndexMapper;



    @Override
    public boolean checkRiskRating(Long userId, UserAddress userAddress) {
        boolean result = false;
        try {
            int customerRisk1Score = 1;
            int customerRisk2Score = 1;
            int nationalityRiskScore = 10;
            int residenceRiskScore = 10;
            int behaviourRiskScore = 1;
            int transactionValueScore = 20;
            List<Map<String, Object>> rawList =
                    wckAuditMapper.selectWckAuditInfo(userId, WckStatus.PASSED, null, null, null);
            if (CollectionUtils.isNotEmpty(rawList)) {
                UserWckAuditVo vo = new UserWckAuditVo(rawList.get(0));
                List<UserWckAuditLogVo> logList = vo.getAuditLogs();
                if (CollectionUtils.isNotEmpty(logList)) {
                    UserWckAuditLogVo lastedAuditLog = logList.get(logList.size() - 1);
                    if(null != lastedAuditLog.getIsPep()) {
                    customerRisk1Score = 1 == lastedAuditLog.getIsPep() ? 20 : 1;
                    }
                }
            }

            UserIdRequest request = new UserIdRequest();
            request.setUserId(userId);
            APIResponse<UserKycApproveVo> response = userKycBusiness.getApproveUser(APIRequest.instance(request));
            if (response.getData() != null) {
                String nationality = StringUtils.isNotEmpty(response.getData().getCheckInfo().getIssuingCountry())
                        ? response.getData().getCheckInfo().getIssuingCountry()
                        : response.getData().getBaseInfo().getCountry();
                nationalityRiskScore = this.getScoreByCountry(nationality);
            }

            residenceRiskScore = this.getScoreByCountry(userAddress.getCountry());

            transactionValueScore = StringUtils.isNotBlank(userAddress.getEstimatedTradeVolume())
                    ? this.getScoreByTransactionValue(userAddress.getEstimatedTradeVolume())
                    : 20;

            int totalValue = customerRisk1Score + customerRisk2Score + nationalityRiskScore + residenceRiskScore
                    + behaviourRiskScore + transactionValueScore;

            if (totalValue >= 81) {
                result = true;
            }

            UserRiskRating record = new UserRiskRating();
            record.setBehaviourRiskScore(behaviourRiskScore);
            record.setCreateTime(DateUtils.getNewUTCDate());
            record.setCustomerRisk1Score(customerRisk1Score);
            record.setCustomerRisk2Score(customerRisk2Score);
            record.setNationalityRiskScore(nationalityRiskScore);
            record.setResidenceRiskScore(residenceRiskScore);
            record.setTotalScore(totalValue);
            record.setUserId(userId);
            record.setTransactionValueScore(transactionValueScore);

            userRiskRatingMapper.insertSelective(record);
            
        } catch (Exception e) {
            log.info("check risk rating error:{}", e);
        }
        return result;
    }

    private int getScoreByCountry(String country) {
        int nationalityRiskScore = 10;
        switch (RiskRatingConst.transfer(country)) {
            case "L":
                nationalityRiskScore = 1;
                break;
            case "M":
                nationalityRiskScore = 5;
                break;
            case "H":
                nationalityRiskScore = 10;
                break;
        }
        return nationalityRiskScore;
    }

    private int getScoreByTransactionValue(String value) {
        int transactionValueScore = 20;
        switch (value) {
            case "0-1,000 GBP":
            case "1,000-10,000 GBP":
                transactionValueScore = 5;
                break;
            case "10,000-100,000 GBP":
                transactionValueScore = 10;
                break;
            case "Above 100,000 GBP":
                transactionValueScore = 20;
                break;
        }
        return transactionValueScore;
    }

    @Override
    public APIResponse<SearchResult<UserRiskRatingVo>> getList(APIRequest<UserRiskRatingQuery> request)
            throws Exception {
        UserRiskRatingQuery userRiskRatingQuery = request.getBody();
        if (StringUtils.isNotBlank(userRiskRatingQuery.getEmail())) {
            User user = userMapper.queryByEmail(userRiskRatingQuery.getEmail());
            if (user != null) {
                userRiskRatingQuery.setUserId(user.getUserId());
            } else {
                return APIResponse.getOKJsonResult(null);
            }
        }

        List<UserRiskRatingVo> UserRiskRatingVos = new ArrayList<>();
        List<UserRiskRating> userRiskRatingList = userRiskRatingMapper.selectByPage(userRiskRatingQuery);
        if (userRiskRatingList != null && !userRiskRatingList.isEmpty()) {
            // 获得UserId <-> Email 映射
            Set<Long> userIds = userRiskRatingList.stream().map(UserRiskRating::getUserId).collect(Collectors.toSet());
            List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
            Map<Long, String> userEmailMapping =
                    userIndices.stream().collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));

            for (UserRiskRating riskRating : userRiskRatingList) {
                UserRiskRatingVo userRiskRatingVo = new UserRiskRatingVo();
                BeanUtils.copyProperties(riskRating, userRiskRatingVo);
                userRiskRatingVo.setEmail(userEmailMapping.get(userRiskRatingVo.getUserId()));
                UserRiskRatingVos.add(userRiskRatingVo);
            }
        }

        SearchResult<UserRiskRatingVo> searchResult = new SearchResult<>();
        searchResult.setRows(UserRiskRatingVos);
        searchResult.setTotal(userRiskRatingMapper.getListCount(userRiskRatingQuery));
        return APIResponse.getOKJsonResult(searchResult);
    }



}
