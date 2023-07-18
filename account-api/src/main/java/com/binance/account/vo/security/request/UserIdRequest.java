package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("用户编号Request")
@Getter
@Setter
public class UserIdRequest implements Serializable {

    private static final long serialVersionUID = 4337520945072011095L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
