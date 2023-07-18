package com.binance.account.vo.device.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author: caixinning
 * @date: 2018/05/08 18:24
 **/

@ApiModel(description = "查询指定设备的操作历史", value = "查询指定设备的操作历史")
@Getter
@Setter
public class UserDeviceHistoryQueryRequest extends ToString {

    private static final long serialVersionUID = 6177284945429888216L;

    @ApiModelProperty(notes = "用户id（user.id）")
    @NotNull
    private Long userId;

    @ApiModelProperty(notes = "设备信息的主键（user_device.id），注意与content.device_id做区分！")
    @NotNull
    private Long devicePk;

}
