package com.binance.account.vo.subuser;

import com.binance.account.vo.user.request.RegisterUserRequest;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.validator.groups.Add;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

/**
 * Created by pcx
 */
@ApiModel("CreateNoEmailSubUserReq")
@Getter
@Setter
public class CreateNoEmailSubUserReq  {
    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账号name")
    @NotNull
    private String userName;

    @ApiModelProperty(required = true, notes = "密码")
    @NotNull
    private String password;

    @ApiModelProperty(required = true, notes = "确认密码")
    @NotNull
    private String confirmPassword;

    @ApiModelProperty(required = false, notes = "渠道")
    private String trackSource;

    @ApiModelProperty(required = false, notes = "推荐人")
    private Long agentId;

    @ApiModelProperty(required = true, notes = "终端类型")
    private TerminalEnum terminal;

    @ApiModelProperty(required = false, notes = "设备信息")
    private HashMap<String, String> deviceInfo;

    @ApiModelProperty(name = "自定义邮件链接", required = false)
    private String customEmailLink;

    @ApiModelProperty(required = false, notes = "推广code,user_agent_rate中的agentRateCode")
    private String agentRateCode;

    @ApiModelProperty(required = false, notes = "子账号备注")
    private String remark;

}