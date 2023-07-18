package com.binance.account.service.certificate;

import com.binance.account.common.query.TaxIdBlacklistQuery;
import com.binance.account.data.entity.certificate.TaxIdBlacklist;
import com.binance.account.vo.certificate.TaxIdBlacklistVo;
import com.binance.master.commons.SearchResult;

public interface ITaxId {

    SearchResult<TaxIdBlacklistVo> queryTaxIdBlacklist(TaxIdBlacklistQuery query);

    int pushBlacklist(String taxId, String creator, String remark);

    TaxIdBlacklist getBlacklistByTaxId(String taxId);

    int removeBlacklist(String taxId);
}
