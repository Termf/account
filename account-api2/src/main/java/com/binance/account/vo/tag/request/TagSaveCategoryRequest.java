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
@ApiModel("保存标签组Request")
@Getter
@Setter
public class TagSaveCategoryRequest extends ToString {
    private static final long serialVersionUID = 1990557969114475433L;
    @ApiModelProperty(value = "父标签组ID", required = true)
    @NotEmpty
    private String pid;
    @ApiModelProperty(value = "标签组名称", required = true)
    @NotEmpty
    private String name;

}
