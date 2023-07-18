package com.binance.account.vo.tag.request;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/6/21
 */
@ApiModel("筛选标签关系Request")
@Getter
@Setter
public class TagIndicatorConditionPageRequest extends ToString {
    private static final long serialVersionUID = -1690568560211233002L;
    @ApiModelProperty(value = "用户ID")
    private String userId;
    @ApiModelProperty(value = "标签名称")
    private String tagName;
    @ApiModelProperty(value = "标签组名称")
    private String categoryName;
    @ApiModelProperty(value = "标签值")
    private String tagValue;
    @ApiModelProperty(value = "最小值")
    private String tagMinValue;
    @ApiModelProperty(value = "最大值")
    private String tagMaxValue;
    @ApiModelProperty(value = "备注")
    private String tagRemark;
    @ApiModelProperty(value = "遍历查询")
    private String tagEachChild;
    @ApiModelProperty(value = "偏移量")
    private Integer position;
    @ApiModelProperty(value = "条目")
    private Integer size;
}
