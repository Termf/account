package com.binance.account.vo.security.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("VerificationTwoV3Response")
@Getter
@Setter
@NoArgsConstructor
public class VerificationTwoV3Response {
 	

	

	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}