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
@ApiModel("查询标签Request")
@Getter
@Setter
public class TagNameRequest extends ToString {
    private static final long serialVersionUID = 164269231164979999L;
    @ApiModelProperty(value = "标签名称", required = true)
    @NotEmpty
    private String tagName;
}
