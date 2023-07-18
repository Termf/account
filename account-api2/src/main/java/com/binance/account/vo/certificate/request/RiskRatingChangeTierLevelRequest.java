package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author mikiya.chen
 * @date 2020/3/3 3:27 下午
 */
@Data
public class RiskRatingChangeTierLevelRequest extends ToString {

    private static final long serialVersionUID = 143873964296412176L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("渠道编码")
    private String channelCode;

    @ApiModelProperty("tier等级")
    private String tierLevel;
}
