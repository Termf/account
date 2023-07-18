package com.binance.account.vo.reset.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2019-01-15 18:56
 */
@ApiModel("答题结果信息")
@Setter
@Getter
public class ResetAnswerRet extends ToString {

    private static final long serialVersionUID = 4494070917915800792L;

    @ApiModelProperty("是否成功处理")
    private Boolean success;

    @ApiModelProperty("错误信息描述语")
    private String message;

    @ApiModelProperty("错误时的一些特定状态信息，与前端约定")
    private String status;

    @ApiModelProperty("是否回答问题通过")
    private Boolean pass;

    @ApiModelProperty("用户是否被锁定")
    private Boolean lock;

    @ApiModelProperty("用户ID[主要是做行为日志用，不能返回前端]")
    private Long userId;

}
