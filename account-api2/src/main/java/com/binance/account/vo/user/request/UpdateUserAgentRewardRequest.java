package com.binance.account.vo.user.request;

import com.binance.account.common.enums.RestoreEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by mengjuan on 2018/9/28.
 */
@ApiModel(description = "修改用户返佣比例及原因Request", value = "修改用户返佣比例及原因Request")
@Getter
@Setter
public class UpdateUserAgentRewardRequest implements Serializable{

    private static final long serialVersionUID = -8712756500722798386L;

    @ApiModelProperty(required = true, notes = "主键id")
    private Long id;
    
    @ApiModelProperty(required = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(required = true, notes = "返佣比例")
    @NotNull
    private BigDecimal agentRewardRatio;

    @ApiModelProperty(required = false, notes = "是否预计恢复时间<n:否;y:是>")
    private RestoreEnum isRestore;

    @ApiModelProperty(required = false, notes = "预计恢复时间")
    private Date expectRestoreTime;

    @ApiModelProperty(required = false, notes = "修改原因")
    private String reason;

    @ApiModelProperty(required = false, notes = "申请人id")
    private String applyId;

    @ApiModelProperty(required = false, notes = "申请人姓名")
    private String applyName;
}