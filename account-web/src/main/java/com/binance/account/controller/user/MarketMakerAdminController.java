package com.binance.account.controller.user;

import com.binance.account.api.MarketMakerAdminApi;
import com.binance.account.service.user.IMarketMakerUser;
import com.binance.account.vo.user.MarketMakerUserVo;
import com.binance.account.vo.user.request.AddMarketMakerUserRequest;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.request.MarketMakerUserRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhao chenkai
 * @date 2019/11/06
 */
@RestController
@Monitored
public class MarketMakerAdminController implements MarketMakerAdminApi {

    @Autowired
    private IMarketMakerUser marketMakerUser;

    @Override
    public APIResponse<Long> add(@RequestBody @Validated APIRequest<AddMarketMakerUserRequest> request) {
        return marketMakerUser.add(request);
    }

    @Override
    public APIResponse<Void> delete(@RequestBody @Validated APIRequest<IdRequest> request) {
        return marketMakerUser.delete(request);
    }

    @Override
    public APIResponse<List<MarketMakerUserVo>> marketMakerUserList(@RequestBody APIRequest<MarketMakerUserRequest> request) {
        return marketMakerUser.marketMakerUserList(request);
    }
}
