package com.binance.account.vo.subuser;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@ApiModel(description = "子账号BTC资产总值", value = "子账号BTC资产总值")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SubUserAssetBtcVo extends ToString {

    private static final long serialVersionUID = 7500066715215555420L;

    @ApiModelProperty(readOnly = true, notes = "账户id")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "账号")
    private String email;

    @ApiModelProperty(readOnly = true, notes = "是否启用子账户<true:是；false:否>")
    private Boolean isSubUserEnabled;

    @ApiModelProperty(readOnly = true, notes = "子账户BTC资产总值")
    private BigDecimal totalAsset;

    @ApiModelProperty("是否是资管子账户")
    private Boolean isAssetSubUser;

    @ApiModelProperty("是否启用资管子账户")
    private Boolean isAssetSubUserEnabled;
}
