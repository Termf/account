package com.binance.account.vo.subuser.response;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("更新brokersubacount的res")
@Data
public class BrokerUserCommissionRes  {


    private BigDecimal maxMakerCommission;

    private BigDecimal minMakerCommission;

    private BigDecimal maxTakerCommission;

    private BigDecimal minTakerCommission;

    private Long subAccountQty;

    private Integer maxSubAccountQty;
}