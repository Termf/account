package com.binance.account.service.question.options;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface IUserTraceService {
	// 大数据，所有交易过的币种
	String USER_TRADED = "user_trade_symbol_list";
	// 大数据，最近三个月交易币种
	String USER_TRADED_48hr = "user_trade_symbol_list_lt_48hr";
	
	/**
	 * 获取用户收藏币种
	 * 
	 * @param userId
	 * @return
	 */
	List<String> getUserSelectedSymbolList(Long userId);

	/**
	 * 用户持有币种
	 * 
	 * @param userId
	 * @return
	 */
	List<String> getUserAssetList(Long userId);

	/**
	 * 获取先上可交易币种
	 * 
	 * @return
	 */
	Set<String> getBaseAssetList();

	/**
	 * 获取最近交易的币种
	 * 
	 * @param userId
	 * @param featureVariable 大数据特征
	 * @return
	 * @throws Exception
	 */
	List<String> getUserTradedAssetList(Long userId,String featureVariable);

	/**
	 * 查询指定用户的指定币种的交易数量
	 * 
	 * @param userId
	 * @param asset 币种
	 * @return
	 */
	BigDecimal getUserAssetAmount(Long userId, String asset);
	
	
}