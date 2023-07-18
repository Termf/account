package com.binance.account.vo.apimanage.request;


import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@ApiModel
@Getter
@Setter
public class DeleteAllApiKeyRequest extends ToString {

    private static final long serialVersionUID = -4445310547066405042L;
    @ApiModelProperty(value = "当前登陆人ID", required = true)
    @NotEmpty
    private String loginUid;

}
