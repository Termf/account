package com.binance.account.vo.apimanage.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
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
public class UpdateApiKeyV3Request extends BaseMultiCodeVerifyRequest {
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

    @ApiModelProperty
    private String info;
}
