package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import com.binance.master.validator.groups.Edit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@ApiModel(description = "账号激活Request", value = "账号激活Request")
@Getter
@Setter
public class AccountActiveUserRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -4972981493219743684L;

    @ApiModelProperty(required = false, notes = "userId")
    private Long userId;

    @ApiModelProperty(required = false, notes = "邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "注册令牌")
    private String registerToken;

    @ApiModelProperty(required = true, notes = "激活码")
    @NotEmpty(groups = Edit.class)
    private String code;

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase().trim();
    }
}
