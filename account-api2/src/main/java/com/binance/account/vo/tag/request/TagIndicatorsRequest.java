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
 * @date 2018/6/26
 */
@ApiModel("保存或者更新用户标签关系Request")
@Getter
@Setter
public class TagIndicatorsRequest extends ToString {
    private static final long serialVersionUID = -12691942623603780L;
    @ApiModelProperty(value = "用户标签关系列表", required = true)
    @NotEmpty
    private List<TagIndicatorRequest> indicators;
}
