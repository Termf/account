package com.binance.account.vo.withdraw.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-04-11 14:58
 */
@ApiModel("提币人脸识别时间比较")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawFaceInHoursRequest extends ToString {

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = false, notes = "比较的时长(小时)，如果未输入，默认使用系统配置值")
    private Integer hours;
}
