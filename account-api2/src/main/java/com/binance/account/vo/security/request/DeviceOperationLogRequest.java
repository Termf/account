package com.binance.account.vo.security.request;

import com.binance.master.commons.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class DeviceOperationLogRequest extends Page implements Serializable {

    private static final long serialVersionUID = -2234085423457890765L;

    @ApiModelProperty(required = true, notes = "用户ID")
    private Long userId;

    @ApiModelProperty(required = true, notes = "操作")
    private String operation;

    @ApiModelProperty(required = true, notes = "设备pk")
    private Long devicePk;

    private Date timeFrom;

    private Date timeTo;

}
