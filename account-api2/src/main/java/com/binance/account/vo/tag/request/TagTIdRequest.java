package com.binance.account.vo.tag.request;

import org.hibernate.validator.constraints.NotEmpty;

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
@ApiModel("查询标签Request")
@Getter
@Setter
public class TagTIdRequest extends ToString {
    private static final long serialVersionUID = -6979864367137853378L;
    @ApiModelProperty(value = "标签ID", required = true)
    @NotEmpty
    private String tid;

    @ApiModelProperty(value = "角色ID")
    private List<String> roleIds;
}