package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("关闭提币白名单Response")
@Getter
@Setter
@NoArgsConstructor
public class CloseWithdrawWhiteStatusV2Response extends ToString {
    private static final long serialVersionUID = 7106033986252531972L;

}
