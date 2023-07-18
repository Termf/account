package com.binance.account.vo.subuser.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("查询子账号列表及子账号BTC资产总值request")
@Data
public class SubUserAssetBtcRequest extends ToString {

    private static final long serialVersionUID = 7066326798561015363L;

    @ApiModelProperty(required = true, notes = "母账号id")
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "子账号email")
    private String email;

    private String isSubUserEnabled;

    @NotNull
    private Integer page;

    @NotNull
    private Integer limit;
}