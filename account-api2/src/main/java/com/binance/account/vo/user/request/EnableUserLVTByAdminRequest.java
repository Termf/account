package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "后台开启关闭用户LVT状态Request", value = "后台开启关闭用户LVT状态Request")
@Getter
@Setter
public class EnableUserLVTByAdminRequest extends ToString {

    private static final long serialVersionUID = 6788802556522969441L;
    
    @ApiModelProperty(required = true, notes = "userId")
    private Long userId;

    @ApiModelProperty(required = true, notes = "是否开启LVT")
    private Boolean enableLVT;

}
