package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class UserRiskInfoResponse extends ToString {

    private Long userId;
    @ApiModelProperty("email")
    private String email;
    @ApiModelProperty("手机号")
    private String mobile;
    @ApiModelProperty("手机国家编码")
    private String mobileCode;
    @ApiModelProperty("推荐人ID")
    private Long agentId;
    @ApiModelProperty("是否绑定了google 2fa")
    private boolean googleAuth;
    @ApiModelProperty("是否绑定了mobile 2fa")
    private boolean mobileAuth;
    @ApiModelProperty("注册时的ip")
    private String registerIp;
    @ApiModelProperty("注册时ip的国籍")
    private String registerIpCountry;
    @ApiModelProperty("注册时间")
    private Date registerTime;
    @ApiModelProperty("交易等级")
    private Integer tradeLevel;
    @ApiModelProperty("最后一次登录使用的语言")
    private String lastLoginLang;


}
