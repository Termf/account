package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * SAQ (short answer questions) 调查问卷
 * 
 * @author Men Huatao (alex.men@binance.com)
 * @date 2020/8/18
 */
@Data
@ApiModel("用户调查问卷状态查询 Response")
public class UserSAQResponse {

    @ApiModelProperty(value = "userId")
    private Long userId;

    @ApiModelProperty(value = "问卷种类")
    private String type;

    @ApiModelProperty(value = "状态")
    private String status;

}
