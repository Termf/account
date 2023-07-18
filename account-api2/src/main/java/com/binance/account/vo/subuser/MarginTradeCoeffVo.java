package com.binance.account.vo.subuser;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class MarginTradeCoeffVo {
    private BigDecimal normalBar;
    private BigDecimal marginCallBar;
    private BigDecimal preLiquidationBar;
    private BigDecimal forceLiquidationBar;
    private Long lendingUid;
    private Long principalCollectionUid;
    private Long interestCollectionUid;
    /**
     * 是否能交易
     */
    private Boolean canTrade;
}
