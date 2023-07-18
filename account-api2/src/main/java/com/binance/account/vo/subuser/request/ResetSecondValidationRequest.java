package com.binance.account.vo.subuser.request;

import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by mengjuan on 2018/10/22.
 */
@ApiModel("重置子账户的2faRequest")
@Getter
@Setter
public class ResetSecondValidationRequest extends TofaRequest implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5396996420418327774L;

	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户userId")
    @NotNull
    private Long subUserId;

    @ApiModelProperty(required = true, notes = "子账户重置类型,GOOGLE:重置谷歌;SMS:重置手机")
    @NotNull
    private AuthTypeEnum subType;
}
