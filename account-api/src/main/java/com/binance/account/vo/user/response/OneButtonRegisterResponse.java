package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "OneButtonRegisterResponse", value = "OneButtonRegisterResponse")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OneButtonRegisterResponse extends ToString {

    @ApiModelProperty(readOnly = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "账号")
    private String email;

}
