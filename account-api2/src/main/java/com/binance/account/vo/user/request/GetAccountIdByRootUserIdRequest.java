package com.binance.account.vo.user.request;

import com.binance.account.vo.user.enums.AccountTypeEnum;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "根据主账户userid来获取相应类型的accountid request")
@Getter
@Setter
public class GetAccountIdByRootUserIdRequest implements Serializable {


    @ApiModelProperty(required = true,notes = "主账户的userid")
    @NotNull
    private Long rootUserId;
    @ApiModelProperty(required = true,notes = "账户类型填：SPOT,MARGIN,FUTURE")
    @NotNull
    private AccountTypeEnum accountType;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
