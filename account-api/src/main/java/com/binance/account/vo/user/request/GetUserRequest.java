package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

@ApiModel("获取用户Request")
@Getter
@Setter
public class GetUserRequest implements Serializable {

    private static final long serialVersionUID = 8585540730891807713L;

    @ApiModelProperty(name = "账号", required = true)
    @NotEmpty
    private String email;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
