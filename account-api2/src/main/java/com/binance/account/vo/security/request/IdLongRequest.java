package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("通用ID Request")
@Getter
@Setter
public class IdLongRequest implements Serializable {

    private static final long serialVersionUID = 4337520945072011095L;

    @ApiModelProperty("id, 类型为Long")
    @NotNull
    private Long id;

    @ApiModelProperty("userId, 可选字段，一般用作分表")
    private Long userId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
