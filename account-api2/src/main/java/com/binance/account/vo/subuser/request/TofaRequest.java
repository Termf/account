package com.binance.account.vo.subuser.request;

import com.binance.master.commons.ToString;
import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by mengjuan on 2018/10/23.
 */
@ApiModel("2fa验证的2faRequest")
@Getter
@Setter
public class TofaRequest extends ToString {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3234175514823147964L;

	@ApiModelProperty("母账户2fa验证码")
    private String parentCode;

    @ApiModelProperty("母账户的2fa认证类型,GOOGLE:谷歌验证;SMS:手机验证")
    private AuthTypeEnum parentAuthType;
}
