package com.binance.account.service.question.options.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.binance.account.service.question.options.IOptionsService;
import com.binance.account.service.question.options.IUserTraceService;
import com.binance.account.service.question.options.QustionOptionsTypeEnum;
import com.binance.account.service.question.options.UserQuestionEnum;
import com.binance.master.utils.RedisCacheUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于用户收藏和持有的币种的问题选项
 *
 */
@Slf4j
@Service
public class CurrencySelectedGenerator implements IOptionsService {
	
	private static final String ACCOUNT_QUESTION_OPTIONS_USERID = "account:question:options:userid:";

	@Value("${application.userQuestion.optionNumber:8}")
	private Integer optionNumber;// 选项数。
	
	@Value("${application.userQuestion.userBTCAmount.stepLength:0.01;0.05;0.1;0.5;}")
	private String stepLength;// btc步长。
	
	@Value("${application.userQuestion.eachQuestionOptionThreshold:3}")
	private Integer eachAssetOptionThreshold;// 每种资产最多产生的选项数
	
	@Value("${application.userQuestion.assetPool:XRP;BCHABC;MATIC;LTC;EOS;ADA;TRX;XLM;LINK;BTT;XEM;RVN;NEO;SKY;BAT;WAVES;ONT;ETC;IOTA;MFT;XVG;CELR;DENT;VET;IOST;ATOM;ZIL;XMR;NANO;ZEC;ENJ;DASH;MANA;ICX;LINK;BAT;PAX;TUSD;HOT;AION;POLY}")
	private String assetPool; // 可以加入到问题中的资产集合。
	
	@Value("${application.userQuestion.filterAssetPool:ETH;BNB;BTC}")
	private String filterAssetPool; // 需要过滤的资产集合。
	
	@Value("${application.userQuestion.options.cache.days:30}")
	private int optionsTimeOutInDays; // 币种选项缓存时间，默认30天。
	
	@Value("${application.userQuestion.log.printcurrency:false}")
	private boolean logUserCurrency; // 币种选项缓存时间，默认30天。
	
	
	private long timeOutInSeconds = optionsTimeOutInDays * 24 * 3600;
	
	@Resource
	private IUserTraceService userTraceService;

	private void cacheOptions(Long userId,List<String> options) {
		if (CollectionUtils.isEmpty(options)) {
			return;
		}
		log.info("币种生成选项,userId:{},缓存:{}", userId, options);
		RedisCacheUtils.set(userId.toString(), JSON.toJSONString(options), timeOutInSeconds, ACCOUNT_QUESTION_OPTIONS_USERID);
	}
	
	private List<String> getCache(Long userId){
		String json = RedisCacheUtils.get(userId.toString(), String.class, ACCOUNT_QUESTION_OPTIONS_USERID);
		if (StringUtils.isNotBlank(json)) {
			return JSON.parseArray(json, String.class);
		}
		return null;
	}
	
	@Override
	public List<String> genaerateOptions(Long userId) {
		log.info("币种生成选项，userId:{}", userId);
		List<String> ops = getCache(userId);
		if (!CollectionUtils.isEmpty(ops)) {
			log.info("币种生成选项,userId:{},命中缓存:{}", userId, ops);
			return ops;
		}
		
		// 收藏币种
		List<String> userSelectedSymbolList = userTraceService.getUserSelectedSymbolList(userId);
		if (logUserCurrency) {
			log.info("币种生成选项,userId:{},收藏币种:{}", userId, JSON.toJSONString(userSelectedSymbolList));
		}
		// 持有币种
		List<String> userAssetList = userTraceService.getUserAssetList(userId);
		if (logUserCurrency) {
			log.info("币种生成选项,userId:{},持有币种:{}", userId, JSON.toJSONString(userAssetList));
		}
		// 混淆选项
		List<String> options = createUserOption(userSelectedSymbolList, userAssetList);
		// RM-481 缓存
		cacheOptions(userId,options);
		return options;
	}

	private List<String> createUserOption(List<String> userSelectedSymbolList, List<String> userAssetList) {
		Set<String> assetSet = Sets.newHashSet(splieBySemicolon(assetPool));
		List<String> assetList = Lists.newArrayList(assetSet);
		log.info("创建选项，assetPoolList:{}", JSON.toJSONString(assetList));
		
		Set<String> result =Sets.newHashSetWithExpectedSize(eachAssetOptionThreshold << 1);
		addAsset(result,userSelectedSymbolList,assetSet);
		addAsset(result,userAssetList,assetSet);

		// 剩余的还要多少个混淆选项 ，多减了一个是因为有一个“以上都不是“的选项。
		int left = optionNumber - result.size() - 1;
		log.info("创建选项，还需要{}个混淆", left);
		addResultAsset(result, assetList, left);
		log.info("创建选项，result:{}", JSON.toJSONString(result));
		
		List<String> options = Lists.newArrayList(result);
		// 打乱选项顺序
		Collections.shuffle(options);
		// 最后加上一个“以上都不是”
		options.add(NONE_OF_ABOVE);
		Assert.isTrue(options.size() == optionNumber, result + ",未达到指定数量：" + optionNumber);
		return options;
	}
	
	private void addAsset(final Set<String> result,final List<String> userAsset,final Set<String> assetSet) {
		if (!CollectionUtils.isEmpty(userAsset)) {
			filterAsset(userAsset, assetSet);
			addResultAsset(result, userAsset, eachAssetOptionThreshold);
		}
	}

	private void filterAsset(final List<String> userAsset,final Set<String> pool) {
		// 去除三种需要过滤的币种
		if (StringUtils.isNotEmpty(filterAssetPool)) {
			for (String str : splieBySemicolon(filterAssetPool)) {
				userAsset.remove(str);
			}
		}
		// 不在池子里面的都去掉
		Iterator<String> iterator = userAsset.iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();
			if (!pool.contains(next)) {
				iterator.remove();
			}
		}
	}


	private String[] splieBySemicolon(String source) {
		return StringUtils.split(source, ";");
	}

	private void addResultAsset(Set<String> result, List<String> source, Integer sizeThreshold) {
		if (source.size() > sizeThreshold) {
			// 选取sizeThreshold个
			addAssetToResult(result, source, sizeThreshold);
		} else {
			for (String str : source) {
				result.add(str);
			}
		}
	}

	/**
	 * Shuffle source, existed must add minRequired more elements from source
	 *
	 * @param existed  A/B/C
	 * @param source  A/B/C/D/E
	 * @param minRequired 3
	 */
	private void addAssetToResult(Set<String> existed, List<String> source, Integer minRequired) {
		int count = minRequired;
		Collections.shuffle(source);// 已经去重复了，打乱顺序
		for (String coin : source) {
			if (count <= 0) {
				break;
			}
			if (!existed.contains(coin)) {
				existed.add(coin);
				count--;
			}
		}
	}

	@Override
	public List<String> getCorrectAnswers(final Long userId,final UserQuestionEnum questionName) {
		Assert.notNull(questionName, "invalid param");
		Assert.isTrue(questionName.getOptionType() == QustionOptionsTypeEnum.CURRENCY_SELECTED,
				"wrong QustionOptionsTypeEnum");
		switch (questionName) {
		case USER_ASSET: {
			List<String> userAssetList = userTraceService.getUserAssetList(userId);
			if (CollectionUtils.isEmpty(userAssetList)) {
				return Arrays.asList(NONE_OF_ABOVE);
			}
			return userAssetList;
		}
		case USER_PORTFOLIO_ASSET: {
			List<String> userSelectedSymbolList = userTraceService.getUserSelectedSymbolList(userId);
			if (CollectionUtils.isEmpty(userSelectedSymbolList)) {
				return Arrays.asList(NONE_OF_ABOVE);
			}
			return userSelectedSymbolList;
		}
		case USER_TRADED_ASSET: {
			// RM-297 问题模块打分时增加新特征
			List<String> L_3m = userTraceService.getUserTradedAssetList(userId,IUserTraceService.USER_TRADED);// 
			List<String> L_48h = userTraceService.getUserTradedAssetList(userId,IUserTraceService.USER_TRADED_48hr);
			Set<String> set = Sets.newHashSet();
			set.addAll(L_3m);
			set.addAll(L_48h);
			if (CollectionUtils.isEmpty(set)) {
				return Arrays.asList(NONE_OF_ABOVE);
			}
			return new ArrayList<>(set);
		}
		case USER_TRADED_IN_48_H:{
			List<String> lst = userTraceService.getUserTradedAssetList(userId,IUserTraceService.USER_TRADED_48hr);
			Set<String> set = Sets.newHashSet();
			set.addAll(lst);
			if (CollectionUtils.isEmpty(set)) {
				return Arrays.asList(NONE_OF_ABOVE);
			}
			return new ArrayList<>(set);
		}
		case USER_ASSERT_OR_PORTFOLIO_ASSET: {
			Set<String> answers = Sets.newHashSet();
			List<String> userAssetList = userTraceService.getUserAssetList(userId);
			List<String> userSelectedSymbolList = userTraceService.getUserSelectedSymbolList(userId);
			answers.addAll(userAssetList);
			answers.addAll(userSelectedSymbolList);
			if (CollectionUtils.isEmpty(answers)) {
				return Arrays.asList(NONE_OF_ABOVE);
			}
			return new ArrayList<>(answers);
		}
		default:
			throw new RuntimeException("Invalid UserQuestionEnum:" + questionName);
		}
	}

	@Override
	public List<String> trimming(List<String> options, List<String> correctAnswers) {
		correctAnswers.retainAll(options);
		if (correctAnswers.isEmpty()) {
			log.info("币种选项，答案都不在选项中,options:{},correctAnswers:{}", options, correctAnswers);
			correctAnswers.add(NONE_OF_ABOVE);
		}
		return correctAnswers;
	}
}
