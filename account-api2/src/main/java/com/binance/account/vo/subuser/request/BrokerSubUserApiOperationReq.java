package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by yangyang on 2019/8/20.
 */
@ApiModel("freze、unfreeze-brokersubacount的api")
@Getter
@Setter
public class BrokerSubUserApiOperationReq implements Serializable {

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "freeze|unfreeze")
    @NotBlank
    private String operation;
}
