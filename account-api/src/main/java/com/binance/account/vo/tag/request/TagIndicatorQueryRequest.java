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
@ApiModel("查询用户标签关系Request")
@Getter
@Setter
public class TagIndicatorQueryRequest extends ToString {
    private static final long serialVersionUID = 1938208613026167792L;
    @ApiModelProperty(value="用户ID")
    private String userId;
    @ApiModelProperty(value="标签名称")
    private String tagName;
    @ApiModelProperty(value="标签组名称")
    private String categoryName;
    @ApiModelProperty(value="标签值")
    private String tagValue;
    @ApiModelProperty(value="标签最小值")
    private String tagMinValue;
    @ApiModelProperty(value="标签最大值")
    private String tagMaxValue;
    @ApiModelProperty(value="标签备注")
    private String tagRemark;
    @ApiModelProperty(value="递归查询")
    private String tagEachChild;
    @ApiModelProperty(value="偏移量")
    private Integer position;
    @ApiModelProperty(value="条数")
    private Integer size;
}
