package com.binance.account.service.question.options.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.binance.account.service.question.Utils;
import com.binance.account.service.question.options.IUserTraceService;
import com.binance.assetservice.api.IPortfolioApi;
import com.binance.assetservice.api.IProductApi;
import com.binance.assetservice.api.IUserAssetApi;
import com.binance.assetservice.vo.ProductItemVO;
import com.binance.assetservice.vo.request.GetPrivateUserAssetRequest;
import com.binance.assetservice.vo.request.UserAssetTransferBtcRequest;
import com.binance.assetservice.vo.request.portfolio.GetPortfoliosRequest;
import com.binance.assetservice.vo.response.UserAssetResponse;
import com.binance.assetservice.vo.response.UserAssetTransferBtcResponse;
import com.binance.assetservice.vo.response.UserAssetTransferBtcResponse.AssetTransferBtc;
import com.binance.assetservice.vo.response.product.ProductItemResponse;
import com.binance.featureservice.api.IFeatureValueApi;
import com.binance.featureservice.vo.feature.FeatureRequest;
import com.binance.featureservice.vo.feature.FeatureResponse;
import com.binance.featureservice.vo.feature.Variable;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户历史痕迹查询服务
 *
 */
@Slf4j
@Service
public class UserTraceService implements IUserTraceService {
	private final BigDecimal BTC_VALUE = new BigDecimal("0.001");
	private final String BASE_ASSET = "ETH;BTC;BNB;USDT;USDC;PAX;TUSD;USDC;USDS;ALTS;";

	@Value("${application.userQuestion.filterAssetPool:ETH;BNB;BTC}")
	private String filterAssetPool; // 需要过滤的资产集合。
	
    @Value("${application.feature.isMock:false}")
    private boolean isMock;
	
	@Resource
	private IUserTraceService userTraceService;
	@Resource
	private IUserAssetApi userAssetApi;
	@Resource
	private IPortfolioApi portfolioApi;
	@Resource
	private IProductApi productApi;
	@Resource
    private IFeatureValueApi featureValueApi;
	@Resource
    private FeatrueMockService mockService;
	
	/**
	 * 获取用户收藏币种
	 * 
	 * @param userId
	 * @return
	 */
	@Override
	public List<String> getUserSelectedSymbolList(Long userId) {
		List<String> userSelectedSymbolList = this.getPortfolios(userId);
		Set<String> selectedAsset = filterBaseAsset(userSelectedSymbolList);
		List<String> tmp = filterRightAnswer(new ArrayList<>(selectedAsset));
		return tmp;
	}
	
	private List<String> filterRightAnswer(List<String> collect) {
		if (StringUtils.isNotEmpty(filterAssetPool)) {
			for (String str : Utils.splieBySemicolon(filterAssetPool)) {
				collect.remove(str);
			}
		}
		return collect;
	}
	
	private Set<String> filterBaseAsset(final List<String> userSelectedSymbolList) {
		Set<String> selectedAsset = Sets.newHashSet();
		if (CollectionUtils.isEmpty(userSelectedSymbolList)) {
			return selectedAsset;
		}
		for (String baseAsset : this.getBaseAssetList()) {
			userSelectedSymbolList.forEach(sy -> {
				if (sy.endsWith(baseAsset)) {
					selectedAsset.add(sy.substring(0, sy.lastIndexOf(baseAsset)));
				}
			});
		}
		return selectedAsset;
	}
	
	/**
	 * 用户持有币种
	 * 
	 * @param userId
	 * @return
	 */
	@Override
	public List<String> getUserAssetList(Long userId) {
		List<AssetTransferBtc> assetTransferBtcList = this.userAssetTransferBtc(userId);
		List<String> options = new ArrayList<>(0);
		if (!CollectionUtils.isEmpty(assetTransferBtcList)) {
			List<String> collect = assetTransferBtcList.stream()
					.filter(asset -> asset.getTransferBtc().compareTo(BTC_VALUE) > 0)
					.map(asset -> asset.getAsset())
					.distinct().collect(Collectors.toList());
			options = filterRightAnswer(collect);
		}
		return options;
	}

	private APIRequestHeader getRequestHeader() {
		APIRequestHeader originRequest = new APIRequestHeader();
		originRequest.setLanguage(LanguageEnum.ZH_CN);
		originRequest.setTerminal(TerminalEnum.WEB);
		originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
		return originRequest;
	}
	
	// 查询用户自选资产
	private List<String> getPortfolios(Long userId) {
		log.info("获取用户自选资产，userId：{}", userId);
		GetPortfoliosRequest request = new GetPortfoliosRequest();
		request.setUserId(userId.toString());
		APIResponse<List<String>> response = null;
		try {
			response = portfolioApi.getPortfolios(APIRequest.instance(getRequestHeader(), request));
			log.info("获取用户自选资产,userId:{},response:{}", userId, JSON.toJSONString(response));
		} catch (Exception e) {
			log.error("获取用户自选资产，userId:" + userId, e);
		}
		Utils.CheckResponse(response);
		return response.getData();
	}
	
	// 获取用户持有资产
	private List<AssetTransferBtc> userAssetTransferBtc(Long userId) {
		log.info("获取用户持有资产，userId：{}", userId);
		APIResponse<UserAssetTransferBtcResponse> response = null;
		try {
			UserAssetTransferBtcRequest request = new UserAssetTransferBtcRequest();
			request.setUserId(userId.toString());
			response = userAssetApi.userAssetTransferBtc(APIRequest.instance(getRequestHeader(), request));
			log.info("获取用户持有资产，userId:{},response:{}", userId, JSON.toJSONString(response));
		} catch (Exception e) {
			log.error("获取用户持有资产，userId:" + userId, e);
		}
		Utils.CheckResponse(response);
		return response.getData().getAssetTransferBtcList();
	}
	
	/**
	 * 获取先上可交易币种
	 * 
	 * @return
	 */
	@Override
	public Set<String> getBaseAssetList() {
		Set<String> baseAssets = Sets.newHashSet();
		APIResponse<ProductItemResponse> response = null;
		try {
			response = productApi.getAllProducts(APIRequest.instanceBodyNull());
			log.info("quoteAsset:{}",response);
		} catch (Exception e) {
			String[] split = Utils.splieBySemicolon(BASE_ASSET);
			for (String str : split) {
				baseAssets.add(str);
			}
			return baseAssets;
		}
		Utils.CheckResponse(response);
		ProductItemResponse data = response.getData();
		if (data != null) {
			List<ProductItemVO> productItems = data.getProductItems();
			if (!CollectionUtils.isEmpty(productItems)) {
				productItems.forEach(pi -> {
					if (!baseAssets.contains(pi.getQuoteAsset())) {
						baseAssets.add(pi.getQuoteAsset());
					}
				});
			}
		}
		return baseAssets;
	}

	
	/**
	 * 获取最近交易的币种
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<String> getUserTradedAssetList(Long userId,String featureVariable) {
		log.info("获取最近交易的币种,userId:{}", userId);
		
		APIResponse<List<FeatureResponse>> response = null;
		try {
			FeatureRequest req = new FeatureRequest();
			Map<String, String> param = new HashMap<>(1);
			param.put("uid", userId.toString());
			req.setParameters(param);
			req.setVariable(featureVariable);
			List<FeatureRequest> reqs = Arrays.asList(req);
			if (isMock) {
				response = mockService.listFeature(reqs);
			} else {
				response = featureValueApi.listFeature(APIRequest.instance(reqs));
			}
			log.info("获取最近交易的币种,userId:{},isMock:{}, request :{}, response: {}", userId, isMock, req, response);
		} catch (Exception ex) {
			log.error("获取最近交易的币种异常,userId:" + userId, ex);
		}
		Utils.CheckResponse(response);
		List<FeatureResponse> featureResponses = response.getData();

		List<String> options =new ArrayList<>(0);
		if (!CollectionUtils.isEmpty(featureResponses)) {
			for (FeatureResponse featureResponse : featureResponses) {
				Variable variable = featureResponse.getVariable();
				if(featureVariable.equals(variable.getName())) {
					List<String> tradedAssets = Arrays.asList(variable.getValue().toString().split(","));
					Set<String> userTradedAsset = filterBaseAsset(tradedAssets);
					options  = filterRightAnswer(Lists.newArrayList(userTradedAsset));
					break;
				}
			}
		}
		return options;
	}
	
	@Override
	public BigDecimal getUserAssetAmount(final Long userId,final String asset){
		APIResponse<UserAssetResponse> response = null;
		try {
			GetPrivateUserAssetRequest re = new GetPrivateUserAssetRequest();
			re.setAsset(asset);
			re.setUserId(userId.toString());
			response = userAssetApi.getPrivateUserAsset(APIRequest.instance(getRequestHeader(), re));
		} catch (Exception e) {
			log.error("获取最近交易的币种,userId:"+userId, e);
		}

		Utils.CheckResponse(response);
		UserAssetResponse data = response.getData();
		if (!CollectionUtils.isEmpty(data.getUserAssetList())) {
			UserAssetResponse.UserAsset userAsset = data.getUserAssetList().get(0);
			if (userAsset != null) {
				return (new BigDecimal(String.valueOf(userAsset.getFree()))
						.add(new BigDecimal(String.valueOf(userAsset.getFreeze())))
						.add(new BigDecimal(String.valueOf(userAsset.getLocked())))
						.add(new BigDecimal(String.valueOf(userAsset.getWithdrawing()))));
			}
		}
		return new BigDecimal(0);
	}
}
