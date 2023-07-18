package com.binance.account.vo.security.request;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import org.springframework.format.annotation.DateTimeFormat;

import com.binance.master.commons.Page;
import com.binance.master.enums.OrderByEnum;
import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户日志Request")
@Getter
@Setter
public class UserSecurityRequest extends Page implements Serializable{

    private static final long serialVersionUID = -681104276061265637L;
    
    @ApiModelProperty("Id")
    private Long id;
    
    @ApiModelProperty("用户id")
    private Long userId;
    
    @ApiModelProperty("用户ip")
    private String ip;
    
    @ApiModelProperty("用户ip所在位置")
    private String ipLocation;
    
    @ApiModelProperty("客户端类型 ")
    private String clientType; //ios android web wap
    
    @ApiModelProperty("操作类型")
    private String operateType;

    @ApiModelProperty("devicePk")
    private Long devicePk;

    @ApiModelProperty("deviceId")
    private String deviceId;

    @ApiModelProperty("操作时间")
    private Date operateTime;
    
    @ApiModelProperty("操作描述")
    private String description;
    
    @ApiModelProperty("起始时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    
    @ApiModelProperty("结束时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endDate;
    
    @ApiModelProperty("排序字段")
    private String sort;
    
    @ApiModelProperty("排序")
    private OrderByEnum order;

    @ApiModelProperty("多种操作类型")
    private List<String> operateTypeList = Lists.newArrayList();
    
    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
