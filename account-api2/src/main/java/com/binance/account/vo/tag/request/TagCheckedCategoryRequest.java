package com.binance.account.vo.tag.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/10/12
 */
@ApiModel("查询标签权限Request")
@Getter
@Setter
public class TagCheckedCategoryRequest extends ToString {

    private static final long serialVersionUID = 1427665496015375850L;

    @ApiModelProperty(name = "角色ID", required = true)
    private String roleId;

}
