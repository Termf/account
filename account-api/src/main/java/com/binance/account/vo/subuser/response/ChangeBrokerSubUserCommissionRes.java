package com.binance.account.vo.subuser.response;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@Data
public class ChangeBrokerSubUserCommissionRes {

    private String subAccountId;

    private BigDecimal makerCommission;

    private BigDecimal takerCommission;

    private BigDecimal marginMakerCommission;

    private BigDecimal marginTakerCommission;
}