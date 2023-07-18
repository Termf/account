package com.binance.account.vo.kyc.request;

import com.binance.account.common.enums.KycFillType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("获取kyc基础资料")
@Setter
@Getter
public class GetBaseInfoRequest implements Serializable {

    private static final long serialVersionUID = 3794297629099007899L;

    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

    @ApiModelProperty("资料类型")
    private KycFillType fillType;

}
