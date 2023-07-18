package com.binance.account.vo.user.request;

import com.binance.master.validator.constraints.FieldMatch;
import com.binance.master.validator.groups.Edit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel(description = "修改密码Request", value = "修改密码Request")
@FieldMatch(first = "password", second = "confirmPassword")
@Getter
@Setter
public class UpdatePwdUserV2Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = 167910116116805409L;

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull(groups = Edit.class)
    private Long userId;

    @ApiModelProperty(required = true, notes = "原始密码")
    @NotEmpty(groups = Edit.class)
    private String oldPassword;

    @ApiModelProperty(required = true, notes = "新的密码")
    @NotEmpty(groups = Edit.class)
    private String newPassword;

    @ApiModelProperty(required = true, notes = "原始Safe密码")
    @NotEmpty(groups = Edit.class)
    private String oldSafePassword;

    @ApiModelProperty(required = true, notes = "新的Safe密码")
    @NotEmpty(groups = Edit.class)
    private String newSafePassword;

    // 格式须满足例如：http://binance.com/resetPassword.html?vc={vc}&email={email}
    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customForbiddenLink;

    @ApiModelProperty(readOnly = true, notes = "设备信息")
    private Map<String, String> deviceInfo;

}
