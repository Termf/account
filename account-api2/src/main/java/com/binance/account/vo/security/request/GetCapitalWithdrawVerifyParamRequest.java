package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("GetCapitalWithdrawVerifyParamRequest")
@Getter
@Setter
public class GetCapitalWithdrawVerifyParamRequest {


    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("mobileCode")
    private String mobileCode;

     @ApiModelProperty("emailCode")
     private String emailCode;




    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
