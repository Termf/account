package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@ApiModel("批量用户编号Request")
@Getter
@Setter
public class UserIdsRequest {

    @ApiModelProperty("用户Id")
    @NotEmpty
    private List<Long> userIds;

}
