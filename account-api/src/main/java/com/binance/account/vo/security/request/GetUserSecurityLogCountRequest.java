package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("获取用户安全日志总数Request")
@Getter
@Setter
public class GetUserSecurityLogCountRequest implements Serializable {

    private static final long serialVersionUID = -3611888785263673288L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("操作类型")
    private String operateType;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
