package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("ChangeMobileResponse")
@Getter
@Setter
public class ChangeMobileResponse extends ToString {

    private static final long serialVersionUID = -7888190369413890154L;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
