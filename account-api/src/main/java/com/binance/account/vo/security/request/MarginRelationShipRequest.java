package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("margin relationship Request")
@Getter
@Setter
public class MarginRelationShipRequest implements Serializable {


    private static final long serialVersionUID = 1370404444445775791L;
    @ApiModelProperty("第一个userId")
    @NotNull
    private Long firstUserId;

    @ApiModelProperty("第二个userId")
    @NotNull
    private Long secondUserId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
