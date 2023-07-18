package com.binance.account.service.security.model;

import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;

/**
 * @Author: mingming.sheng
 * @Date: 2020/4/26 10:39 上午
 */
@Data
@Builder
public class MultiFactorSceneCheckQuery extends ToString {
    @ApiModelProperty("userId")
    private Long userId;

    @ApiModelProperty("业务场景")
    private BizSceneEnum bizScene;


    @ApiModelProperty("流程Id-新邮箱验证场景需要")
    private String flowId;

    @ApiModelProperty("设备类型")
    private String clientType;

    @ApiModelProperty("设备信息")
    private HashMap<String, String> deviceInfo;
}
