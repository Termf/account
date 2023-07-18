package com.binance.account.vo.tag.request;

import java.util.Date;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 标签用户表
 */
@ApiModel("保存或者更新用户标签关系Request")
@Getter
@Setter
public class TagIndicatorRequest extends ToString {
    private static final long serialVersionUID = -3682602813658211128L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "标签ID")
    private Long tagId;

    @ApiModelProperty(value = "标签类名称")
    private String categoryName;

    @ApiModelProperty(value = "标签名称")
    private String tagName;

    @ApiModelProperty(value = "值")
    private String value;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "最后修改人")
    private String lastUpdatedBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

}
