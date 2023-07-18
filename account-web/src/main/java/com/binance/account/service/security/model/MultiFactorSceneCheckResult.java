package com.binance.account.service.security.model;

import com.binance.account.vo.security.AccountVerificationTwoBind;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * @Author: mingming.sheng
 * @Date: 2020/4/26 10:39 上午
 */
@Data
public class MultiFactorSceneCheckResult extends ToString {
    private static final long serialVersionUID = -3247595990500831823L;

    @ApiModelProperty("需绑定校验项列表")
    private Set<AccountVerificationTwoBind> needBindVerifyList;

    @ApiModelProperty("需校验验证项列表")
    private Set<AccountVerificationTwoCheck> needCheckVerifyList;
}
