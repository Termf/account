package com.binance.account.vo.reset.request;

import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-01-14 17:14
 */
@ApiModel("重置2FA查询最后记录参数")
@Setter
@Getter
public class ResetLastArg extends ToString {
    private static final long serialVersionUID = 6266941579371341841L;

    @ApiModelProperty("邮箱")
    @NotNull
    private String email;

    @ApiModelProperty("重置类型")
    @NotNull
    private UserSecurityResetType type;


}
