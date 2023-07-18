package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("禁用用户")
@Getter
@Setter
public class UserForbidRequest implements Serializable {

    private static final long serialVersionUID = 4337520945072011095L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("禁用code")
    @NotBlank
    private String code;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
