package com.binance.account.vo.user.request;

import com.binance.account.common.enums.RestoreEnum;
import com.binance.master.commons.Page;
import com.binance.master.enums.OrderByEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by mengjuan on 2018/9/26.
 */
@ApiModel("用户返佣列表Request")
@Getter
@Setter
public class AgentRewardRequest extends Page implements Serializable{

    private static final long serialVersionUID = 4575663915123717521L;

    @ApiModelProperty(required = false, notes = "用户Id")
    private Long userId;

    @ApiModelProperty(required = false, notes = "邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "推荐人")
    private Long agentId;

    @ApiModelProperty(required = false, notes = "返佣比例(最新的返佣比例值)")
    private BigDecimal agentRewardRatio;

    @ApiModelProperty(required = false, notes = "注册渠道")
    private String trackSource;
    
    @ApiModelProperty(required = false, notes = "是否预计恢复时间<n:否;y:是>")
    private RestoreEnum isRestore;
    
    @ApiModelProperty(required = false, notes = "修改原因")
    private String reason;

    @ApiModelProperty(required = false, notes = "是否排除 默认推荐人")
    private String excludeDefaultAgent;
    @ApiModelProperty(required = false, notes = "默认推荐人")
    private String defaultAgent;

    @ApiModelProperty(required = false, notes = "注册起始时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty(required = false, notes = "注册结束时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

   /* @ApiModelProperty(required = false, notes = "预计恢复开始时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startExpectRestoreTime;

    @ApiModelProperty(required = false, notes = "预计恢复结束时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endExpectRestoreTime;

    @ApiModelProperty("排序字段")
    private String sort;

    @ApiModelProperty("排序")
    private OrderByEnum order;*/
}
