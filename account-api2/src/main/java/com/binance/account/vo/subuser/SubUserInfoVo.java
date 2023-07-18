package com.binance.account.vo.subuser;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mengjuan on 2018/10/26.
 */
@Getter
@Setter
public class SubUserInfoVo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -573101652692618963L;

    @ApiModelProperty("子账户UserId")
    private Long subUserId;

    @ApiModelProperty("子账户邮箱")
    private String email;

    @ApiModelProperty("是否启用子账户")
    private Boolean isSubUserEnabled;

    @ApiModelProperty("是否激活")
    private Boolean isUserActive;

    @ApiModelProperty("是否启用谷歌验证")
    private Boolean isUserGoogle;

    @ApiModelProperty("子账户注册时间")
    private Date insertTime;

    @ApiModelProperty("子账户绑定的手机号")
    private String mobile;

    @ApiModelProperty("是否启用margin")
    private Boolean isMarginEnabled;

    @ApiModelProperty("是否启用future")
    private Boolean isFutureEnabled;

    @ApiModelProperty("是否是资管子账户")
    private Boolean isAssetSubUser;

    @ApiModelProperty("是否启用资管子账户")
    private Boolean isAssetSubUserEnabled;

    @ApiModelProperty("是否是无邮箱子账号")
    private Boolean isNoEmailSubUser;

    @ApiModelProperty("备注")
    private String remark;

}
