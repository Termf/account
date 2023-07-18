package com.binance.account.vo.withdraw.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhenleisun
 */
@ApiModel("出金地址检测结果")
@Data
public class WithdrawAddressCheckResponse implements Serializable {
    private static final long serialVersionUID = -3834223193102975839L;

    @ApiModelProperty("Address id")
    private String id;

    @ApiModelProperty("检测的地址")
    private String address;

    @ApiModelProperty("地址风险类型：第三方渠道检测到风险，此值会被设置为 3.")
    private Integer type;

    @ApiModelProperty("第三方渠道检测风险结果备注")
    private String remark;

    @ApiModelProperty("检测创建时间")
    private Date time;

    @ApiModelProperty("出金的用户id")
    private String userId;

    @ApiModelProperty("出金的币种")
    private String currency;

    @ApiModelProperty("出金检测更新时间")
    private Date updateTime;

    @ApiModelProperty("更新者的用户id")
    private String updatedByAdminId;
}
