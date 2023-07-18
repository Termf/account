package com.binance.account.vo.subuser.request;

import com.binance.account.vo.subuser.enums.SubAccountSummaryQueryType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;


@ApiModel("QuerySubAccountFutureAccountSummaryRequest")
@Data
public class QuerySubAccountFutureAccountSummaryRequest {


	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "查询类型")
    @NotNull
	private SubAccountSummaryQueryType subAccountSummaryQueryType;

    private Integer page;

    private Integer rows;

    @ApiModelProperty(required = false, notes = "子账户邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "子账户开启状态,1:开启; 0:未开启")
    private Integer isSubUserEnabled;

}
