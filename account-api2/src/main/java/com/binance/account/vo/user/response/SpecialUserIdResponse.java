package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lufei
 * @date 2019/4/28
 */
@ApiModel("特殊用户Response")
@Data
public class SpecialUserIdResponse {

    @ApiModelProperty("特殊用户userId")
    private List<Long> userIds;

}
