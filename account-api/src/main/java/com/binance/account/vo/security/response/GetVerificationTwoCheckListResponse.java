package com.binance.account.vo.security.response;

import com.binance.account.vo.security.AccountVerificationTwoBind;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@ApiModel("GetVerificationTwoCheckListResponse")
@Getter
@Setter
@NoArgsConstructor
public class GetVerificationTwoCheckListResponse {

    @ApiModelProperty("需绑定校验项列表")
    private Set<AccountVerificationTwoBind> needBindVerifyList;

    @ApiModelProperty("需校验验证项列表")
	private Set<AccountVerificationTwoCheck> needCheckVerifyList;
	
	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}