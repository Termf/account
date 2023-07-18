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
@ApiModel("查询父标签组Request")
@Getter
@Setter
public class TagPIdRequest extends ToString {
    private static final long serialVersionUID = -1943401852281224588L;
    @ApiModelProperty(value = "父标签组ID", required = true)
    @NotEmpty
    private Long pid;
}
