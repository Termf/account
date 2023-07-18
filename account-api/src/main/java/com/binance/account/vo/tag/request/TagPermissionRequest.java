package com.binance.account.vo.tag.request;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/10/11
 */
@ApiModel("保存标签权限Request")
@Getter
@Setter
public class TagPermissionRequest extends ToString {

    private static final long serialVersionUID = -8649456711730506482L;

    @ApiModelProperty(value = "角色ID", required = true)
    @NotEmpty
    private String roleId;

    @ApiModelProperty(value = "标签组ID", required = true)
    @NotEmpty
    private List<Long> categoryIds;

}
