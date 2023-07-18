package com.binance.account.vo.user.request;

import com.binance.account.vo.user.enums.RegisterationMethodEnum;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "OneButtonRegisterRequest", value = "OneButtonRegisterRequest")
@Getter
@Setter
public class OneButtonRegisterRequest extends ToString {

    @ApiModelProperty(required = true, notes = "邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "渠道")
    private String trackSource;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("手机代码")
    private String mobileCode;

    @ApiModelProperty("注册方式(默认邮箱)")
    private RegisterationMethodEnum registerationMethod=RegisterationMethodEnum.EMAIL;

    @ApiModelProperty(required = false, notes = "推荐人")
    private Long agentId;    

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public void setTrackSource(String trackSource) {
        this.trackSource = trackSource == null ? null : trackSource.trim();
    }

}
