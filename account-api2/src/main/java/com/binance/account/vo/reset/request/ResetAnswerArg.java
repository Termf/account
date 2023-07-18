package com.binance.account.vo.reset.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-01-15 18:45
 */
@ApiModel("重置流程回答问题的请求")
@Setter
@Getter
public class ResetAnswerArg extends ToString {
    private static final long serialVersionUID = -8830527223338753485L;

    @ApiModelProperty("重置流程的ID编号")
    @NotNull
    private String id;

    @ApiModelProperty("答题的序号")
    @NotNull
    private Integer question;

    @ApiModelProperty("是否忽略")
    private String ignore;

    @ApiModelProperty("最后一次重置的地址")
    private String address;

    @ApiModelProperty("持有数量/充值数量/BTC总价值")
    private String amount;

    @ApiModelProperty("最后一次充值日期/创建账号的日期")
    private Long date;




}
