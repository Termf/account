package com.binance.account.vo.device.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel(description = "授权设备Response", value = "授权设备Response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceAuthorizeResponse implements Serializable {
    private static final long serialVersionUID = -2144286131317071262L;

    public static final UserDeviceAuthorizeResponse INVALID = new UserDeviceAuthorizeResponse(
            false, null, null, null, null, null, false, false, null, null,null,null);

    @ApiModelProperty("授权结果，true.授权成功")
    private boolean valid;
    @ApiModelProperty("UserDevice主键id")
    private Long id;
    @ApiModelProperty("设备id")
    private String deviceId;
    @ApiModelProperty("登录ip")
    private String loginIp;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("登陆所在地")
    private String locationCity;
    @ApiModelProperty("是否重复授权")
    private boolean retry = false;
    @ApiModelProperty("是否需要回答问题")
    private boolean needAnswerQuestion = false;
    @ApiModelProperty
    private String questionFlowId;
    @ApiModelProperty("授权成功后需要跳转的地址")
    private String callback;
    @ApiModelProperty("userId")
    private Long userId; // id
    @ApiModelProperty("email")
    private String email; // 账号

    public static UserDeviceAuthorizeResponse pass() {
        UserDeviceAuthorizeResponse response = new  UserDeviceAuthorizeResponse();
        response.setValid(true);

        return response;
    }

}
