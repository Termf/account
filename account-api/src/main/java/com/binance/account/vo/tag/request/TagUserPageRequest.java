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
@ApiModel("查询用户标签关系列表Request")
@Getter
@Setter
public class TagUserPageRequest extends ToString {
    private static final long serialVersionUID = -1810050671392953697L;
    @ApiModelProperty(value = "标签关系ID", required = true)
    @NotEmpty
    private String tagUserid;
    @ApiModelProperty(value = "偏移量")
    private Integer position;
    @ApiModelProperty(value = "条目")
    private Integer size;
}
