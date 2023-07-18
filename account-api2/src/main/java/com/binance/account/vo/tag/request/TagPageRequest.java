package com.binance.account.vo.tag.request;

import org.hibernate.validator.constraints.NotEmpty;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/6/21
 */
@ApiModel("查询标签或标签组列表Request")
@Getter
@Setter
public class TagPageRequest extends ToString {
    private static final long serialVersionUID = -3614573973568284905L;
    @ApiModelProperty(value = "标签或标签组ID", required = true)
    @NotEmpty
    private String xId;
    @ApiModelProperty(value = "标签ID类型", required = true)
    @NotEmpty
    private String tagType;
    @ApiModelProperty(value = "偏移量")
    private Integer position;
    @ApiModelProperty(value = "条目")
    private Integer size;
}
