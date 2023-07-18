package com.binance.account.service.certificate;

import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.certificate.UserChainAddressAudit;
import com.binance.account.vo.security.request.ChainAddressAnalyzeRequest;
import com.binance.account.vo.security.request.ChainAddressAuditRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author alex
 */
public interface IUserChainAddress {

    /**
     * 获取区块链地址审核列表
     * @param request
     * @return
     */
    APIResponse<SearchResult<UserChainAddressAudit>> getChainAddressAuditPage(APIRequest<ChainAddressAuditRequest> request);

    /**
     * 提交区块链地址审核
     * @param request
     * @return
     */
    APIResponse<?> submitChainAddressAudit(APIRequest<ChainAddressAnalyzeRequest> request);

    /**
     * 人工审核区块链地址
     * @param request
     * @return
     */
    APIResponse<?> auditChainAddress(APIRequest<ChainAddressAuditRequest> request);

    /**
     * 判断地址是否在白名单中
     * @param address
     * @return
     */
    APIResponse<Boolean> isAddressInWhitelist(@RequestParam("address") String address);

    /**
     * 刷新白名单地址缓存
     * @return
     */
    void refreshWhiteAddrCache();

    /**
     * 获取所有人工审核通过的白名单地址, 以","为分隔符
     * @return
     */
    String getWhiteAddresses();
}
