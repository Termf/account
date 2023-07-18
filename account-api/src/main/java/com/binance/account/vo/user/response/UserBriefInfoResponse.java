package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserBriefInfoResponse extends ToString {


    @ApiModelProperty(
            name = "status: ENABLE/DISABLE")
    private String status;

    @ApiModelProperty(name = "国家编码. eg. CN")
    private String countryCode;

    @ApiModelProperty(name = "手机国家编码. eg. 86")
    private String mobileCode;

    @ApiModelProperty(name = "手机")
    private String mobile;

    @ApiModelProperty(name = "firstName")
    private String firstName;
    
    @ApiModelProperty(name = "middleName")
    private String middleName;

    @ApiModelProperty(name = "lastName")
    private String lastName;

    @ApiModelProperty(name = "是否通过kyc")
    private Boolean isKycPass;
}
