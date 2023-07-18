package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * Created by yangyang on 2019/8/22.
 */
@ApiModel(description = "返佣查询配置返回", value = "返佣查询配置返回Response")
@Getter
@Setter
public class GetUserAgentConfigResponse extends ToString {

    @ApiModelProperty(required = true, notes = "用户Id")
    private Long userId;

    @ApiModelProperty(required = false, notes = "推荐者的最大连接数")
    private Integer maxLink;

    @ApiModelProperty(required = false, notes = "推荐者的最大返佣比例")
    private BigDecimal maxAgentRate;
}
