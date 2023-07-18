package com.binance.account.vo.user.request;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.binance.master.commons.ToString;
import com.binance.master.validator.groups.Add;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@ApiModel(description = "第三方机构用户注册Request", value = "第三方机构用户注册Request")
@Getter
@Setter
public class ThirdPartyUserRegisterRequest extends ToString {


    private static final long serialVersionUID = -5982262167752199142L;

    @ApiModelProperty(required = true, notes = "邮箱")
    @NotEmpty(groups = Add.class)
    @Email(groups = Add.class)
    private String email;

    @ApiModelProperty(required = true, notes = "渠道")
    @NotEmpty
    private String trackSource;

    @ApiModelProperty(required = false, notes = "用户ip")
    private String userIp;

    @ApiModelProperty(required = false, notes = "设备信息")
    private HashMap<String, String> deviceInfo;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public void setTrackSource(String trackSource) {
        this.trackSource = trackSource == null ? null : trackSource.trim();
    }

}
