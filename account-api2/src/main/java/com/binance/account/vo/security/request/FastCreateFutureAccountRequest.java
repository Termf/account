package com.binance.account.vo.security.request;

import com.binance.master.enums.TerminalEnum;
import com.binance.master.utils.StringUtils;
import com.binance.master.validator.constraints.FieldMatch;
import com.binance.master.validator.groups.Add;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

@ApiModel("FastCreateFutureAccountRequest")
@Getter
@Setter
@FieldMatch(first = "password", second = "confirmPassword")
public class FastCreateFutureAccountRequest {

    @ApiModelProperty(required = true, notes = "邮箱")
    @NotEmpty
    private String email;

    @ApiModelProperty(required = true, notes = "密码")
    @NotEmpty
    private String password;

    @ApiModelProperty(required = true, notes = "确认密码")
    @NotEmpty
    private String confirmPassword;

    @ApiModelProperty(required = false, notes = "渠道")
    private String trackSource;

    @ApiModelProperty(required = true, notes = "终端类型")
    @NotNull(groups = Add.class)
    private TerminalEnum terminal;

    @ApiModelProperty(required = false, notes = "设备信息")
    private HashMap<String, String> deviceInfo;

    @ApiModelProperty(name = "自定义邮件链接", required = false)
    private String customEmailLink;

    @ApiModelProperty(required = false, notes = "是否走新的注册流程")
    private Boolean isNewRegistrationProcess=false;

    @ApiModelProperty("期货返佣推荐码")
    private String futuresReferalCode;


    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setTrackSource(String trackSource) {
        this.trackSource = trackSource == null ? null : trackSource.trim();
    }

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}