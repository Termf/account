package com.binance.account.controller.tradelevel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.TradeLevelApi;
import com.binance.account.service.tradelevel.ITradeLevel;
import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.user.TradeLevelVo;
import com.binance.account.vo.user.request.TradeLevelRequest;
import com.binance.account.vo.user.request.TradeSingleLevelRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.validator.groups.Edit;

/**
 * @author lufei
 * @date 2018/11/16
 */
@RestController
public class TradeLevelController implements TradeLevelApi {

    @Autowired
    private ITradeLevel iTradeLevel;

    @Override
    public APIResponse<List<TradeLevelVo>> manageList() throws Exception {
        List<TradeLevelVo> list = iTradeLevel.manageList();
        return APIResponse.getOKJsonResult(list);
    }

    @Override
    public APIResponse<List<TradeLevelVo>> futuresManageList() throws Exception {
        List<TradeLevelVo> list = iTradeLevel.futuresManageList();
        return APIResponse.getOKJsonResult(list);
    }

    @Override
    public APIResponse<TradeLevelVo> manageInfo(@RequestBody @Validated APIRequest<IdLongRequest> request)
            throws Exception {
        TradeLevelVo vo = iTradeLevel.manageInfo(request.getBody());
        return APIResponse.getOKJsonResult(vo);
    }

    @Override
    public APIResponse<Void> manageAdd(@RequestBody @Validated APIRequest<TradeLevelRequest> request) throws Exception {
        iTradeLevel.manageAdd(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> manageUpdate(@RequestBody @Validated(Edit.class) APIRequest<TradeLevelRequest> request)
            throws Exception {
        iTradeLevel.manageUpdate(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> manageDelete(@RequestBody @Validated APIRequest<IdLongRequest> request) throws Exception {
        iTradeLevel.manageDelete(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<TradeLevelVo> selectByLevel(@RequestBody @Validated APIRequest<TradeSingleLevelRequest> request)
            throws Exception {
        TradeLevelVo vo = iTradeLevel.selectByLevel(request.getBody());
        return APIResponse.getOKJsonResult(vo);
    }
}
