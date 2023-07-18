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
@ApiModel("查询标签树补充信息Request")
@Getter
@Setter
public class TagExtranInfoRequest extends ToString {
    private static final long serialVersionUID = -6979864367137853378L;
    @ApiModelProperty(value = "用户ID", required = true)
    @NotEmpty
    private String userId;

    @ApiModelProperty(value = "标签ID", required = true)
    @NotEmpty
    private String tagId;
}
