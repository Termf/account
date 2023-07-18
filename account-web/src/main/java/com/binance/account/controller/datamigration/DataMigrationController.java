package com.binance.account.controller.datamigration;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.DataMigrationApi;
import com.binance.account.vo.datamigration.request.DataMigrationRequest;
import com.binance.account.vo.datamigration.request.DataMigrationUserIdRequest;
import com.binance.account.vo.datamigration.response.DataMigrationResponse;
import com.binance.account.vo.datamigration.response.DataMigrationUserIdResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

@RestController
public class DataMigrationController implements DataMigrationApi {

    @Override
    public APIResponse<DataMigrationResponse> synchron(
            @Validated() @RequestBody() APIRequest<DataMigrationRequest> request) throws Exception {
        return null;
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> synchronUserId(
            @Validated() @RequestBody() APIRequest<DataMigrationUserIdRequest> request) throws Exception {
        return null;
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> registerPnk(
            @Validated() @RequestBody() APIRequest<DataMigrationUserIdRequest> request) throws Exception {
        return null;
    }

}
