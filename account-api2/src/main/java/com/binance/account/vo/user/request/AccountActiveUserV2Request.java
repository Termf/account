package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import com.binance.master.validator.groups.Edit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "账号激活Request", value = "账号激活Request")
@Getter
@Setter
public class AccountActiveUserV2Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = 4341323520253432555L;

    @ApiModelProperty(required = true)
    @NotBlank
    private Long userId;
}
