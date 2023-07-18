package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author zhenlei sun
 */
@ApiModel(description = "用户返佣设置信息", value = "用户返佣设置信息")
@Getter
@Setter
public class UserReferralSettingsRequest extends ToString {

	private static final long serialVersionUID = 713412223482838171L;

	@ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(notes = "地区或者州")
    private String regionState;

    @ApiModelProperty(notes = "城市")
    private String city;

    @ApiModelProperty(notes = "邮编")
    private String postalCode;

    @ApiModelProperty(notes = "街道地址")
    private String address;

    @ApiModelProperty(notes = "税号信息")
    private String taxInfo;

    @ApiModelProperty(notes = "用户期望获取促销的方式")
    private String promotionMethods;

    @ApiModelProperty(notes = "用户期望获取促销的方式")
    private String outlets;
}
