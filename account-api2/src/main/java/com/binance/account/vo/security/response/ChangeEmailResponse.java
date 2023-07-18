package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("ChangeEmailResponse")
@Getter
@Setter
public class ChangeEmailResponse extends ToString {

    private static final long serialVersionUID = 8102571391910132998L;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
