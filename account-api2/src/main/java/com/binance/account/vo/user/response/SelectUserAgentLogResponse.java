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
@ApiModel(description = "推荐人记录返回", value = "返佣查询配置返回Response")
@Getter
@Setter
public class SelectUserAgentLogResponse extends ToString {

    @ApiModelProperty(required = true, notes = "推荐人")
    private Long userId;

    @ApiModelProperty(required = true, notes = "推荐码")
    private String agentCode;

    @ApiModelProperty(required = true, notes = "被推荐人")
    private Long referralUser;


}
