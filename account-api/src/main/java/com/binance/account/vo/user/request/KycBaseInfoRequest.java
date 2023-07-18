package com.binance.account.vo.user.request;

import com.binance.account.vo.user.UserKycVo;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "kyc个人信息", value = "kyc个人信息")
@Getter
@Setter
public class KycBaseInfoRequest extends ToString {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6047675130226244474L;

	@ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "个人信息")
    @NotNull
    private UserKycVo.BaseInfo baseInfo;
    
    private boolean isOldApi = true;

}
