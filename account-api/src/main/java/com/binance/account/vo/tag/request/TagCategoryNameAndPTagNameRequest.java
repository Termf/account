package com.binance.account.vo.tag.request;

import org.hibernate.validator.constraints.NotEmpty;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/6/26
 */
@ApiModel("根据标签组和父标签名称查询标签列表Request")
@Getter
@Setter
public class TagCategoryNameAndPTagNameRequest extends ToString {
    private static final long serialVersionUID = 6221691490481197621L;
    @ApiModelProperty(value = "标签组名", required = true)
    @NotEmpty
    private String categoryName;
    @ApiModelProperty(value = "父标签名", required = true)
    @NotEmpty
    private String pTagName;
}
