package com.binance.account.api;

import com.binance.account.vo.datamigration.request.DataMigrationRequest;
import com.binance.account.vo.datamigration.request.DataMigrationUserIdRequest;
import com.binance.account.vo.datamigration.response.DataMigrationResponse;
import com.binance.account.vo.datamigration.response.DataMigrationUserIdResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/dataMigration")
@Api(value = "用户数据同步")
public interface DataMigrationApi {

    @ApiOperation("同步用户数据")
    @PostMapping("/synchron")
    APIResponse<DataMigrationResponse> synchron(@RequestBody() APIRequest<DataMigrationRequest> request)
            throws Exception;

    @ApiOperation("迁移指定userId数据")
    @PostMapping("/synchronUserId")
    APIResponse<DataMigrationUserIdResponse> synchronUserId(
            @RequestBody() APIRequest<DataMigrationUserIdRequest> request) throws Exception;

    @ApiOperation("意外遗漏数据注册回pnk")
    @PostMapping("/registerPnk")
    APIResponse<DataMigrationUserIdResponse> registerPnk(
            @RequestBody() APIRequest<DataMigrationUserIdRequest> request) throws Exception;
}
