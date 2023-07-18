package com.binance.account.api;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.user.TradeLevelVo;
import com.binance.account.vo.user.request.TradeLevelRequest;
import com.binance.account.vo.user.request.TradeSingleLevelRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author lufei
 * @date 2018/11/16
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/tradeLevel")
@Api(value = "交易等级")
public interface TradeLevelApi {

    @ApiOperation(notes = "查询交易等级列表", nickname = "list", value = "查询交易等级列表")
    @PostMapping("/manage/list")
    APIResponse<List<TradeLevelVo>> manageList() throws Exception;

    @ApiOperation(notes = "查询期货交易等级列表", nickname = "list", value = "查询期货交易等级列表")
    @PostMapping("/futuresManage/list")
    APIResponse<List<TradeLevelVo>> futuresManageList() throws Exception;

    @ApiOperation(notes = "查询交易等级详情", nickname = "info", value = "查询交易等级详情")
    @PostMapping("/manage/info")
    APIResponse<TradeLevelVo> manageInfo(@RequestBody APIRequest<IdLongRequest> request) throws Exception;

    @ApiOperation(notes = "添加交易等级", nickname = "add", value = "添加交易等级")
    @PostMapping("/manage/add")
    APIResponse<Void> manageAdd(@RequestBody APIRequest<TradeLevelRequest> request) throws Exception;

    @ApiOperation(notes = "修改交易等级", nickname = "update", value = "修改交易等级")
    @PostMapping("/manage/update")
    APIResponse<Void> manageUpdate(@RequestBody APIRequest<TradeLevelRequest> request) throws Exception;

    @ApiOperation(notes = "删除交易等级", nickname = "delete", value = "删除交易等级")
    @PostMapping("/manage/delete")
    APIResponse<Void> manageDelete(@RequestBody APIRequest<IdLongRequest> request) throws Exception;

    @ApiOperation(notes = "查询交易等级", nickname = "delete", value = "查询交易等级")
    @PostMapping("/web/selectByLevel")
    APIResponse<TradeLevelVo> selectByLevel(@RequestBody APIRequest<TradeSingleLevelRequest> request) throws Exception;

}
