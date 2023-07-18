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
@ApiModel("查询标签或标签组Request")
@Getter
@Setter
public class TagIdRequest extends ToString {
    private static final long serialVersionUID = -1810050671392953697L;
    @ApiModelProperty(value = "标签或标签组ID", required = true)
    @NotEmpty
    private String id;
}
