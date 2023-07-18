package com.binance.account.data.entity.tradelevel;

import java.math.BigDecimal;

import lombok.Data;

/**
 * @author lufei
 * @date 2018/11/16
 */
@Data
public class TradeLevel {

    private Long id;

    private Integer level;

    private BigDecimal bnbFloor;

    private BigDecimal bnbCeil;

    private BigDecimal btcFloor;

    private BigDecimal btcCeil;

    private BigDecimal makerCommission;

    private BigDecimal takerCommission;

    private BigDecimal buyerCommission;

    private BigDecimal sellerCommission;

}
