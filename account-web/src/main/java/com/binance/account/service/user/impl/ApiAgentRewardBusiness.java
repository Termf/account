package com.binance.account.service.user.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.data.entity.agent.ApiAgentRewardConfig;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.agent.ApiAgentRewardConfigMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.service.subuser.impl.CheckSubUserBusiness;
import com.binance.account.vo.apiagentreward.enums.ApiAgentRewardToEnum;
import com.binance.account.vo.apiagentreward.request.ApiAgentRewardQuery;
import com.binance.account.vo.apiagentreward.request.ApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.request.IfNewUserRequest;
import com.binance.account.vo.apiagentreward.response.ApiAgentRewardAdminVo;
import com.binance.account.vo.apiagentreward.response.IfNewUserResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.commons.SearchResult;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.service.user.IApiAgentReward;
import com.binance.account.vo.apiagentreward.request.SelectApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.response.SelectApiAgentRewardResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class ApiAgentRewardBusiness extends CheckSubUserBusiness implements IApiAgentReward {

    // 给予返现的返佣推荐人id
    private final static Long WHITELIST_USER = 10000001l;

    private static final String[] AGENT_CODE_ARR =
            {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                    "M", "L", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ApiAgentRewardConfigMapper apiAgentRewardConfigMapper;

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, rollbackFor = Exception.class)
    public APIResponse<SelectApiAgentRewardResponse> selectApiAgentReward(APIRequest<SelectApiAgentRewardRequest> request) {
        final SelectApiAgentRewardRequest requestBody = request.getBody();

        // 校验用户
        User user = checkAndGetUserById(requestBody.getUserId());

        ApiAgentRewardConfig apiAgentRewardConfig = apiAgentRewardConfigMapper.selectByAgentCode(requestBody.getAgentRewardCode());
        if (apiAgentRewardConfig == null) {
            log.info("selectApiAgentReward 返回返佣比例为null，原因: 无该返佣码 request:{}", JSONObject.toJSONString(requestBody));
            return APIResponse.getOKJsonResult();
        }

        // 是否满足限制条件
        Boolean meetRebateCondition = meetRebateCondition(apiAgentRewardConfig, user, JSONObject.toJSONString(requestBody));
        if (!meetRebateCondition) {
            return APIResponse.getOKJsonResult();
        }

        // 返佣对象，根据配置判断返佣给Broker还是交易人
        Long rewardToUserId = apiAgentRewardConfig.getAgentId();
        if (apiAgentRewardConfig.getRewardTo() == 0) {
            rewardToUserId = requestBody.getUserId();
        }

        BigDecimal agentRewardRatio = null;
        // 判断用户注册时间
        Date userRegisterTime = user.getInsertTime();
        if (userRegisterTime.compareTo(apiAgentRewardConfig.getStartTime()) < 0) {
            agentRewardRatio = apiAgentRewardConfig.getOldUserRatio();
        } else {
            agentRewardRatio = apiAgentRewardConfig.getNewUserRatio();
        }

        SelectApiAgentRewardResponse response = new SelectApiAgentRewardResponse();
        response.setAgentRewardRatio(agentRewardRatio);
        response.setAgentId(rewardToUserId);
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<IfNewUserResponse> ifNewUser(APIRequest<IfNewUserRequest> request) {
        final IfNewUserRequest requestBody = request.getBody();

        // 校验用户
        User user = checkAndGetUserById(requestBody.getUserId());

        ApiAgentRewardConfig apiAgentRewardConfig = apiAgentRewardConfigMapper.selectByAgentCode(requestBody.getApiAgentCode());
        if (apiAgentRewardConfig == null) {
            log.info("ifNewUser 返回返佣比例为null，原因: 无该返佣码 request:{}", JSONObject.toJSONString(requestBody));
            return APIResponse.getOKJsonResult();
        }

        // 是否满足限制条件
        Boolean meetRebateCondition = meetRebateCondition(apiAgentRewardConfig, user, JSONObject.toJSONString(requestBody));
        // 判断用户注册时间
        Date userRegisterTime = user.getInsertTime();
        Boolean ifNewUser = userRegisterTime.compareTo(apiAgentRewardConfig.getStartTime()) > 0;

        IfNewUserResponse response = new IfNewUserResponse();
        response.setApiAgentCode(requestBody.getApiAgentCode());
        response.setRebateWorking(meetRebateCondition);
        response.setIfNewUser(ifNewUser);
        return APIResponse.getOKJsonResult(response);
    }

    /**
     * 是否满足api现货返佣的限制条件
     * @param apiAgentRewardConfig
     * @param user
     * @param requestBodyStr
     * @return
     */
    public Boolean meetRebateCondition(ApiAgentRewardConfig apiAgentRewardConfig, User user,  String requestBodyStr) {
        // 在开始时间之前的交易不予返现
        if (apiAgentRewardConfig.getStartTime().compareTo(new Date()) > 0) {
            log.info("meetRebateCondition 返回返佣比例为null，原因: 在返佣码开始时间之前 request:{}", requestBodyStr);
            return false;
        }

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        // 返佣推荐人为10000001的交易才予返现
        if (!userInfo.getAgentId().equals(WHITELIST_USER)) {
            log.info("meetRebateCondition 返回返佣比例为null，原因: 交易人的返佣推荐人不为10000001 request:{}", requestBodyStr);
            return false;
        }

        // vip5-vip9的账号不予返现
        Integer tradeLevel = userInfo.getTradeLevel();
        if (tradeLevel > 4 && tradeLevel <= 9) {
            log.info("meetRebateCondition 返回返佣比例为null，原因: 交易人vip等级为{} request:{}", tradeLevel, requestBodyStr);
            return false;
        }

        // broker子账户的交易不予返现
        if (isBrokerSubUser(user)) {
            log.info("meetRebateCondition 返回返佣比例为null，原因: 交易人为broker子账号 request:{}", requestBodyStr);
            return false;
        }

        // 子账号交易，不允许返佣给其母账号
        if (isSubUser(user)) {
            SubUserBinding subUserBinding = subUserBindingMapper.selectBySubUserId(user.getUserId());
            if (subUserBinding != null && apiAgentRewardConfig.getAgentId().equals(subUserBinding.getParentUserId())) {
                log.info("meetRebateCondition 返回返佣比例为null，原因: 交易人为子账号且返佣对象为其母账号 request:{}", requestBodyStr);
                return false;
            }
        }
        return true;
    }

    @Override
    public SearchResult<ApiAgentRewardAdminVo> queryApiAgentReward(ApiAgentRewardQuery query) {
        // 分页
        Integer page = query.getPage();
        Integer rows = query.getRows();
        page = page == null || page < 1 ? 1 : page;
        rows = rows == null || rows < 1 ? 20 : rows;

        PageHelper.startPage(page, rows);
        ApiAgentRewardConfig queryDO = new ApiAgentRewardConfig();
        queryDO.setAgentId(query.getAgentId());
        queryDO.setAgentRewardCode(query.getAgentRewardCode());
        Page<ApiAgentRewardConfig> apiAgentRewardConfigs = apiAgentRewardConfigMapper.selectPage(queryDO);

        List<ApiAgentRewardAdminVo> list = apiAgentRewardConfigs.stream().map(x -> {
            ApiAgentRewardAdminVo apiAgentRewardAdminVo = new ApiAgentRewardAdminVo();
            BeanUtils.copyProperties(x, apiAgentRewardAdminVo);
            apiAgentRewardAdminVo.setStartTime(x.getStartTime().getTime());
            apiAgentRewardAdminVo.setCreateTime(x.getCreateTime().getTime());
            apiAgentRewardAdminVo.setUpdateTime(x.getUpdateTime().getTime());
            return apiAgentRewardAdminVo;
        }).collect(Collectors.toList());

        return new SearchResult(list, apiAgentRewardConfigs.getTotal());
    }

    @Override
    public void addApiAgentReward(ApiAgentRewardRequest request) {
        // 校验用户
        checkAndGetUserById(request.getAgentId());

        ApiAgentRewardConfig apiAgentRewardConfig = new ApiAgentRewardConfig();
        BeanUtils.copyProperties(request, apiAgentRewardConfig);
        apiAgentRewardConfig.setStartTime(new Date(request.getStartTime()));
        apiAgentRewardConfig.setAgentRewardCode(getRandomAgentCode());
        apiAgentRewardConfigMapper.insertSelective(apiAgentRewardConfig);
    }

    @Override
    public ApiAgentRewardAdminVo apiAgentRewardInfo(Long id) {
        ApiAgentRewardConfig agentRewardConfig = apiAgentRewardConfigMapper.selectByPrimaryKey(id);
        ApiAgentRewardAdminVo adminVo = new ApiAgentRewardAdminVo();
        BeanUtils.copyProperties(agentRewardConfig, adminVo);
        adminVo.setStartTime(agentRewardConfig.getStartTime().getTime());
        adminVo.setCreateTime(agentRewardConfig.getCreateTime().getTime());
        adminVo.setUpdateTime(agentRewardConfig.getUpdateTime().getTime());
        return adminVo;
    }

    @Override
    public void updateApiAgentReward(ApiAgentRewardRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        ApiAgentRewardConfig agentRewardConfig =  new ApiAgentRewardConfig();
        agentRewardConfig.setId(request.getId());
        agentRewardConfig.setNewUserRatio(request.getNewUserRatio());
        agentRewardConfig.setOldUserRatio(request.getOldUserRatio());
        agentRewardConfig.setRewardTo(request.getRewardTo());
        agentRewardConfig.setStartTime(new Date(request.getStartTime()));
        apiAgentRewardConfigMapper.updateByPrimaryKeySelective(agentRewardConfig);
    }

    @Override
    public void deleteApiAgentReward(ApiAgentRewardRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        apiAgentRewardConfigMapper.delete(request.getId(), request.getUpdateBy());
    }



    /**
     * 创建一个8位String,第一个是字母，后续7个为字母或数字
     *
     * @return
     */
    private String getRandomAgentCode() {
        StringBuilder randomStr = new StringBuilder();
        String first = AGENT_CODE_ARR[(int) (Math.random() * 26) + 10];
        randomStr.append(first);
        for (int i = 0; i < 7; i++) {
            randomStr.append(AGENT_CODE_ARR[(int) (Math.random() * 36)]);
        }
        if (apiAgentRewardConfigMapper.ifAgentCodeExist(randomStr.toString()) == null) {
            return randomStr.toString();
        }
        return getRandomAgentCode();
    }
}
