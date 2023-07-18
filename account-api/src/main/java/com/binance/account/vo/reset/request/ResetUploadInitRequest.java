package com.binance.account.vo.reset.request;

import com.binance.account.common.enums.UserSecurityResetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("邮件链接点开后的请求")
@Getter
@Setter
public class ResetUploadInitRequest implements Serializable {

    private static final long serialVersionUID = -5742068700734480085L;

    @ApiModelProperty("邮件链接中的requestId")
    @NotNull
    private String requestId;

    @ApiModelProperty("业务编号")
    @NotNull
    private String transId;

    @ApiModelProperty("业务类型")
    @NotNull
    private UserSecurityResetType type;

}
