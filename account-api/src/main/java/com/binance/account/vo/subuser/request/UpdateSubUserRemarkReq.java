package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by Fei.Huang on 2018/10/10.
 */
@Data
public class UpdateSubUserRemarkReq {

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账号UserId")
    @NotNull
    private Long subUserId;

    @ApiModelProperty(required = true, notes = "子账号备注")
    @NotEmpty
    private String remark;

}