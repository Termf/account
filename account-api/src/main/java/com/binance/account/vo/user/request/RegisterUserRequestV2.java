package com.binance.account.vo.user.request;

import com.binance.account.vo.user.enums.RegisterationMethodEnum;
import com.binance.master.commons.ToString;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.validator.constraints.FieldMatch;
import com.binance.master.validator.groups.Add;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

@ApiModel(description = "RegisterUserRequestV2", value = "RegisterUserRequestV2")
@FieldMatch(first = "password", second = "confirmPassword")
@Getter
@Setter
public class RegisterUserRequestV2 extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 167910116116805409L;

    @ApiModelProperty(required = true, notes = "邮箱")
    private String email;

    @ApiModelProperty(required = true, notes = "密码")
    @NotEmpty(groups = Add.class)
    private String password;

    @ApiModelProperty(required = true, notes = "确认密码")
    @NotEmpty(groups = Add.class)
    private String confirmPassword;

    @ApiModelProperty(required = false, notes = "渠道")
    private String trackSource;

    @ApiModelProperty(required = false, notes = "推荐人")
    private Long agentId;

    @ApiModelProperty(required = true, notes = "终端类型")
    @NotNull(groups = Add.class)
    private TerminalEnum terminal;

    @ApiModelProperty(required = false, notes = "设备信息")
    private HashMap<String, String> deviceInfo;

    @ApiModelProperty(name = "自定义邮件链接", required = false)
    private String customEmailLink;

    @ApiModelProperty(required = false, notes = "推广code,user_agent_rate中的agentRateCode")
    private String agentRateCode;

    @ApiModelProperty(required = false, notes = "是否走新的注册流程")
    private Boolean isNewRegistrationProcess=false;


    @ApiModelProperty(required = false, notes = "是否是期货一键开户流程")
    private Boolean isFastCreatFuturesAccountProcess=false;

    @ApiModelProperty("期货返佣推荐码")
    private String futuresReferalCode;


    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("手机代码")
    private String mobileCode;

    @ApiModelProperty("注册方式(默认邮箱)")
    private RegisterationMethodEnum registerationMethod=RegisterationMethodEnum.EMAIL;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setTrackSource(String trackSource) {
        this.trackSource = trackSource == null ? null : trackSource.trim();
    }

}
