package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("UnbindEmailResponse")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UnbindEmailResponse extends ToString {
    private static final long serialVersionUID = 4519258776967089473L;

    @ApiModelProperty("用户id")
    private Long userId;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
