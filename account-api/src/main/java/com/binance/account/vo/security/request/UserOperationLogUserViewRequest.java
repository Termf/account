package com.binance.account.vo.security.request;

import com.binance.master.commons.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class UserOperationLogUserViewRequest extends Page implements Serializable {

    private static final long serialVersionUID = -2358755512717229786L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("operations")
    private List<String> operations;

    @ApiModelProperty("status. " +
            "0/null -- all " +
            "1 -- only responseStatus == true" +
            "2 -- only responseStatus == false and failReason is not null")
    private Integer status;

    @ApiModelProperty("请求时间（查询范围开始）")
    private Date requestTimeFrom;

    @ApiModelProperty("请求时间（查询范围结束）")
    private Date requestTimeTo;


}
