package com.binance.account.vo.security.request;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("锁定用户Request")
@Getter
@Setter
public class UserLockRequest implements Serializable {


    private static final long serialVersionUID = -7782617179756731702L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("锁定结束时间")
    private Date lockEndTime;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
