package com.binance.account.vo.security.response;

import com.binance.account.vo.security.AccountVerificationTwoBind;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 7:13 下午
 */
@Data
public class MultiFactorSceneCheckResponse {

    @ApiModelProperty("需绑定校验项列表")
    private Set<AccountVerificationTwoBind> needBindVerifyList;

    @ApiModelProperty("需校验验证项列表")
    private Set<AccountVerificationTwoCheck> needCheckVerifyList;
}
