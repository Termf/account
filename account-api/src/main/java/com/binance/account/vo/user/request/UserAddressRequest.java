package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author alex
 */
@ApiModel(description = "地址认证信息", value = "地址认证信息")
@Getter
@Setter
public class UserAddressRequest extends ToString {

	private static final long serialVersionUID = 913412723482738171L;

	@ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "地址认证文件")
    @NotNull
    private byte[] uploadFile;

    @ApiModelProperty(required = true, notes = "地址认证文件名")
    @NotNull
    private String originalFileName;

    @ApiModelProperty(notes = "街道地址")
    private String street;

    @ApiModelProperty(notes = "国家")
    private String country;

    @ApiModelProperty(notes = "城市")
    private String city;

    @ApiModelProperty(notes = "邮编")
    private String postalCode;

    /**
     * 理论上应该在别的地方存，由于设计稿放在地址审核页面，有没有特别表存这类信息
     */
    @ApiModelProperty(notes = "资金来源")
    private String sourceOfFund;

    @ApiModelProperty(notes = "预计每月交易额")
    private String estimatedTradeVolume;
}
