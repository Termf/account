package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@ApiModel(description = "用户身份认证检测Request", value = "用户身份认证检测Request")
@Getter
@Setter
public class UserDetectCertificateRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 599570556547934159L;

    @ApiModelProperty(required = true, notes = "用户id")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "证件号码")
    @NotBlank
    private String number;

    @ApiModelProperty(required = true, notes = "国家")
    private String country;

}
