package com.binance.account.vo.user.response;

import com.binance.account.vo.user.enums.CommonStatusEnum;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("绑定oauth")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class BindOauthResponse extends ToString {

    private static final long serialVersionUID = 7744316408622128596L;
    @ApiModelProperty(notes = "用户id")
    private Long userId;
    @ApiModelProperty(notes = "I:初始化，S：已激活成功，C：已撤销")
    private CommonStatusEnum status;
}
