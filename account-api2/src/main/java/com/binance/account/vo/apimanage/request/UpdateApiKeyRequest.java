package com.binance.account.vo.apimanage.request;

import com.binance.master.commons.ToString;
import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel
@Getter
@Setter
public class UpdateApiKeyRequest extends ToString {
    // String id, Integer ruleId, String apiName, String ip, int status, String operationType,
    // String apiId,
    // String verifyCode, @RequestParam(value = "info", required = false) String info
    private static final long serialVersionUID = -4445310547066405042L;
    @ApiModelProperty(required = true)
    @NotEmpty
    private String userId;
    @ApiModelProperty(value = "当前登陆人ID", required = false)
    private String loginUid;
    @ApiModelProperty(required = true)
    @NotNull
    private Long id;
    @ApiModelProperty(required = true)
    @NotNull
    private Integer ruleId;
    @ApiModelProperty(required = true)
    @NotEmpty
    private String apiName;
    @ApiModelProperty(required = false)
    @Length(min = 0, max = 500)
    private String ip;
    @ApiModelProperty(required = true)
    @NotNull
    private Integer status;
    @ApiModelProperty(required = true)
    @NotNull
    private AuthTypeEnum operationType;
    @ApiModelProperty(required = true)
    @NotEmpty
    private String verifyCode;
    @ApiModelProperty
    private String info;

}
