package com.binance.account.vo.reset.request;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-01-24 11:50
 */
@Setter
@Getter
public class ResetAuditArg extends ToString {

    private static final long serialVersionUID = -6036456442689982584L;

    @ApiModelProperty("重置流程ID")
    @NotNull
    private String id;

    @ApiModelProperty("重置状态，只认passed/refused")
    @NotNull
    private UserSecurityResetStatus status;

    @ApiModelProperty("审核意见信息")
    @NotNull
    private String auditMsg;

}
