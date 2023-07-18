package com.binance.account.vo.kyc.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author mikiya.chen
 * @date 2020/2/3 3:21 下午
 */

@ApiModel("修改用户填写姓名请求")
@Getter
@Setter
public class UpdateFillNameRequest extends ToString{

    private static final long serialVersionUID = 553221021882107737L;

    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

    @ApiModelProperty("fillType")
    @NotNull
    private String fillType;

    @ApiModelProperty("名")
    private String firstName;

    @ApiModelProperty("中间名")
    private String middleName;

    @ApiModelProperty("姓")
    private String lastName;
}
