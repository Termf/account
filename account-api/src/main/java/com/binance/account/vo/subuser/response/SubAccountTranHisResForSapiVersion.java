package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("SubAccountTranHisResForSapiVersion")
@Data
public class SubAccountTranHisResForSapiVersion {

    private String counterParty;
    private String email;
    private int type;
    private String asset;
    private String qty;
    private Long time;
}
