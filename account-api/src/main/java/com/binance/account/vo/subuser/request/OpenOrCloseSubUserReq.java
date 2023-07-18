package com.binance.account.vo.subuser.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by mengjuan on 2018/10/22.
 */
@ApiModel("启用或禁用子账户Request")
@Getter
@Setter
public class OpenOrCloseSubUserReq extends ToString {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1817544105985245847L;

	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户userIds")
    @NotNull
    private List<Long> userIds;
}
