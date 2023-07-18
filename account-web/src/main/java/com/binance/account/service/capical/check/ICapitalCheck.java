package com.binance.account.service.capical.check;

import com.binance.account.vo.withdraw.response.WithdrawAddressCheckResponse;

import java.math.BigDecimal;

/**
 * 虚拟货币出入金 合规检测接口
 *
 * @author zhenleisun
 */
public interface ICapitalCheck {
    /**
     * 检测CA是否含有入金地址黑名单，如有加入address_source_black_list
     *
     * @param userId
     * @param coin 币种，例如BTC
     * @param txHash transactionHash
     * @param sourceAddresses 入金来源地址
     * @param targetAddress 入金地址
     * @param chargeId user_charge  id
     * @return true表示入金地址在黑名单; false表示入金地址安全
     */
    boolean detectAddressSourceBlackByAddress(String userId, String coin, String txHash, String sourceAddresses, String targetAddress,
            String chargeId);

    /**
     * 出金地址风险 检测
     * @param userId 用户id
     * @param asset 币种，e.g. BTC
     * @param address 提币地址
     * @param amount 提币数量
     *
     * @return WithdrawAddressCheckResponse, null表示检测未打开 或 检测未发现风险
     *
     * @throws Exception
     */
    WithdrawAddressCheckResponse getAddressBlackByAddress(String userId, String asset, String address, BigDecimal amount) throws Exception;
}
