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

@ApiModel(description = "删除用户设备", value = "删除用户设备")
@Getter
@Setter
public class UserDeviceDeleteRequest extends ToString {

    private static final long serialVersionUID = 6177284945429888216L;

    /**
     * user.用户删除 admin.管理员后台删除
     */
    public static final String SOURCE_USER = "user";
    public static final String SOURCE_ADMIN = "admin";

    @ApiModelProperty(notes = "用户id（user.id）")
    @NotNull
    private Long userId;

    @ApiModelProperty(notes = "设备信息的主键（user_device.id），注意与content.device_id做区分！为空时，删除该用户所有设备")
    private Long devicePk;

    @ApiModelProperty("备注")
    private String memo;

    @ApiModelProperty("操作来源，默认为用户")
    private String source = SOURCE_USER;

    public boolean isFromUser(){
        return SOURCE_USER.equals(source);
    }
}
