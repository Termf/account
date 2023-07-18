package com.binance.account.vo.subuser.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by zhao chenkai on 2019/10/29.
 */
@ApiModel("根据tranId查询子母账户划转记录request")
@Data
public class SubUserTransferByTranIdReq extends ToString {

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "tranId")
    @NotNull
    private Long tranId;

}
