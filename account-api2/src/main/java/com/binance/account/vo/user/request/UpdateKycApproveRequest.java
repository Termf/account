package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "更新kyc通过信息", value = "更新kyc通过信息")
@Getter
@Setter
public class UpdateKycApproveRequest extends ToString {

	@ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(notes = "名")
    @NotNull
    private String firstName;

    @ApiModelProperty(notes = "姓")
    @NotNull
    private String lastName;

    @ApiModelProperty(notes = "国家")
    @NotNull
    private String country;


}
