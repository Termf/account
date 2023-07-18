package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

@ApiModel("用户邮箱Request")
@Getter
@Setter
public class UserEmailRequest implements Serializable {

    private static final long serialVersionUID = -1161512061052855393L;

    @ApiModelProperty("用户邮箱")
    @NotEmpty
    private String email;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
