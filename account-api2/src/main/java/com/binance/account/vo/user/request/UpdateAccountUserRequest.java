package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import com.binance.master.validator.constraints.FieldMatch;
import com.binance.master.validator.groups.Edit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "修改账号Request", value = "修改账号Request")
@FieldMatch(first = "email", second = "confirmEmail")
@Getter
@Setter
public class UpdateAccountUserRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -4972981493219743684L;

    @ApiModelProperty(required = true, notes = "用户id")
    @NotNull(groups = Edit.class)
    private Long userId;

    @ApiModelProperty(required = true, notes = "账号")
    @NotEmpty(groups = Edit.class)
    @Email(groups = Edit.class)
    private String email;

    @ApiModelProperty(required = true, notes = "确认账号")
    @NotEmpty(groups = Edit.class)
    @Email(groups = Edit.class)
    private String confirmEmail;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail == null ? null : confirmEmail.trim().toLowerCase();
    }

}
