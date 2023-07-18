package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel("CreateFutureAccountRequest")
@Getter
@Setter
public class CreateFutureAccountRequest {
    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("邀请码")
    private String invitationCode;

    @ApiModelProperty(required = true, notes = "母账号userId")
    private Long parentUserId;

    @ApiModelProperty("推荐码")
    private String agentCode;

    @ApiModelProperty(required = false, notes = "是否是期货一键开户流程")
    private Boolean isFastCreatFuturesAccountProcess=false;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
