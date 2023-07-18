package com.binance.account.common.query;

import lombok.Data;

@Data
public class TaxIdBlacklistQuery extends Pagination {

    private String taxId;
}
