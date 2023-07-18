package com.binance.account.vo.subuser.request;

import com.binance.master.validator.groups.Add;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by mengjuan on 2018/10/26.
 */
@ApiModel("修改子账户密码Request")
@Getter
@Setter
public class UpdatePassWordRequest extends TofaRequest implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6688845990976874102L;

	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户userIds")
    @NotNull
    private Long subUserId;

    @ApiModelProperty(required = true, notes = "密码")
    @NotEmpty(groups = Add.class)
    private String password;

    @ApiModelProperty(required = true, notes = "确认密码")
    @NotEmpty(groups = Add.class)
    private String confirmPassword;
}
