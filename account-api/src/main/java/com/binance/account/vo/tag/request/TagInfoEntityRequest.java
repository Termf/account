package com.binance.account.vo.tag.request;

import com.binance.account.vo.tag.TagDetailVo;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author lufei
 * @date 2018/6/21
 */
@ApiModel("标签实体Request")
@Getter
@Setter
public class TagInfoEntityRequest extends ToString {
    private static final long serialVersionUID = 8432117020208022947L;
    @ApiModelProperty(value = "标签ID")
    private String tagId;
    @ApiModelProperty(value = "标签名称")
    private String tagName;
    @ApiModelProperty(value = "标签组ID")
    private String tagCategoryId;
    @ApiModelProperty(value = "标签最大值")
    private String tagMax;
    @ApiModelProperty(value = "标签最小值")
    private String tagMin;
    @ApiModelProperty(value = "标签值")
    private String tagValue;
    @ApiModelProperty(value = "父标签名称")
    private String tagParentTagName;
    @ApiModelProperty(value = "父标签组名称")
    private String tagParentCategoryName;
    @ApiModelProperty(value = "标签详情")
    private List<TagDetailVo> tagDetail;
}
