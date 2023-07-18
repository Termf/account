package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("BindEmailResponse")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BindEmailResponse extends ToString {
    private static final long serialVersionUID = 304966782437865593L;

    @ApiModelProperty("用户Id")
    private Long userId;



    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
