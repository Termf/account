package com.binance.account.vo.reset.response;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author liliang1
 * @date 2019-01-14 17:17
 */
@ApiModel("重置流程最后记录结果信息")
@Setter
@Getter
public class ResetLastRet extends ToString {

    private static final long serialVersionUID = 4062686926891676041L;
    @ApiModelProperty("重置类型")
    private UserSecurityResetType type;

    @ApiModelProperty("当前重置流程状态")
    private UserSecurityResetStatus status;

    @ApiModelProperty("审核结果信息")
    private String auditMsg;

    @ApiModelProperty("审核时间")
    private Date auditTime;
}
