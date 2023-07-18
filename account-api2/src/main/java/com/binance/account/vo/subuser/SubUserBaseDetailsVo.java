package com.binance.account.vo.subuser;

import com.binance.account.vo.user.response.BaseDetailResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by Fei.Huang on 2018/10/10.
 */
@Data
public class SubUserBaseDetailsVo extends BaseDetailResponse {

    @ApiModelProperty(name = "子账号UserId")
    private Long subUserId;

    @ApiModelProperty(name = "子账号备注")
    private String remark;

    @ApiModelProperty("绑定母子账号时间")
    private Date bindingTime;

    @ApiModelProperty(name = "子账号创建时间")
    private Date createTime;

    private Long brokerSubAccountId;

}