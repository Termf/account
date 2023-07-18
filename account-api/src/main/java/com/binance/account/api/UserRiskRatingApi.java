package com.binance.account.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserRiskRatingQuery;
import com.binance.account.vo.user.UserRiskRatingVo;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE)
@RequestMapping(value = "/user/risk/rating")
@Api(value = "风险等级")
public interface UserRiskRatingApi {

    @ApiOperation(notes = "获取风险审核列表", nickname = "getRiskRatingList", value = "获取风险审核列表")
    @PostMapping("/getList")
    APIResponse<SearchResult<UserRiskRatingVo>> getList(@RequestBody() APIRequest<UserRiskRatingQuery> request) throws Exception;

}
