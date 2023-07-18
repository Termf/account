package com.binance.account.service.certificate;

import org.springframework.web.bind.annotation.RequestBody;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserRiskRatingQuery;
import com.binance.account.data.entity.certificate.UserAddress;
import com.binance.account.vo.user.UserRiskRatingVo;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public interface IUseRiskRating {
       boolean checkRiskRating(Long  userId,UserAddress userAddress);
       
       APIResponse<SearchResult<UserRiskRatingVo>> getList(@RequestBody() APIRequest<UserRiskRatingQuery> request) throws Exception;
}
