package com.binance.account.service.capical.check.impl;


import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.service.capical.check.ICapitalCheck;
import com.binance.account.service.certificate.IUserChainAddress;
import com.binance.account.service.certificate.IUserChannelRiskRating;
import com.binance.account.service.certificate.impl.UserChainAddressBusiness;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.security.enums.UpdateWithdrawStatusChannelEnum;
import com.binance.account.vo.security.request.ChainAddressAnalyzeRequest;
import com.binance.account.vo.security.request.UpdateWithdrawStatusRequest;
import com.binance.account.vo.withdraw.response.WithdrawAddressCheckResponse;
import com.binance.assetservice.api.IProductApi;
import com.binance.assetservice.vo.request.product.PriceConvertRequest;
import com.binance.assetservice.vo.response.product.PriceConvertResponse;
import com.binance.fiat.payment.service.external.api.dto.kyc.UsedChannelsByIdRequest;
import com.binance.fiat.payment.service.external.api.dto.kyc.UsedChannelsByIdResponse;
import com.binance.fiat.payment.service.external.api.iface.kyc.FiatKycSupportApi;
import com.binance.inspector.api.ChainalysisApi;
import com.binance.inspector.api.CiphertraceApi;
import com.binance.inspector.api.CoinfirmApi;
import com.binance.inspector.api.EllipticApi;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.vo.chainalysis.request.UserCoinAddressRequest;
import com.binance.inspector.vo.chainalysis.request.UserTransactionRequest;
import com.binance.inspector.vo.idm.IdmTrxData;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.risk.api.RiskAddressBlackListApi;
import com.binance.risk.api.RiskAddressSourceBlackListApi;
import com.binance.risk.vo.address.blacklist.request.RiskAddressBlackListGetRequest;
import com.binance.risk.vo.address.blacklist.request.RiskAddressBlackListInsertRequest;
import com.binance.risk.vo.address.blacklist.response.RiskAddressBlackListQueryResponse;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.google.common.base.Joiner;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenleisun
 */
@Service("capitalCheckService")
@Log4j2
public class CapitalCheckServiceImpl implements ICapitalCheck {
    @Resource
    private SysConfigVarCacheService configVarCacheService;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private CoinfirmApi coinfirmApi;
    @Resource
    private IdmApi idmApi;
    @Resource
    private EllipticApi ellipticApi;
    @Resource
    private CiphertraceApi ciphertraceApi;
    @Resource
    private ChainalysisApi chainalysisApi;
    @Resource
    private IUserChainAddress userChainAddress;
    @Resource
    private RiskAddressSourceBlackListApi riskAddressSourceBlackListApi;
    @Resource
    private RiskAddressBlackListApi riskAddressBlackListApi;
    @Resource
    private IUserSecurity userSecurityApi;
    @Resource
    private IProductApi productApi;
    @Resource
    private ApolloCommonConfig apolloCommonConfig;
    @Resource
    private FiatKycSupportApi fiatKycSupportApi;
    @Resource
    private UserChainAddressBusiness userChainAddressBusiness;
    @Resource
    private IUserChannelRiskRating userChannelRiskRating;

    @Override
    public boolean detectAddressSourceBlackByAddress(String userId, String coin, String txHash, String sourceAddresses, String targetAddress, String chargeId) {
        boolean hasFraud = false;

        if(StringUtils.isBlank(sourceAddresses)){
            return hasFraud;
        }


        if(isAddressInWhiteList(sourceAddresses)){
            return hasFraud;
        }

        // 是否开启人工复核
        boolean manualCheckEnabled = "1".equalsIgnoreCase(configVarCacheService.getValue("chainalysis_manual_check_deposit"));
        // 过滤标准，默认highRisk
        String blockCriteria = configVarCacheService.getValue("chainalysis_block_criteria");
        if (StringUtils.isBlank(blockCriteria)) {
            blockCriteria = "highRisk";
        }

        String coinfirmSupportedAssets = configVarCacheService.getValue("coinfirm_supported_assets");
        // 渠道Coinfirm 默认支持XRP
        if (StringUtils.isBlank(coinfirmSupportedAssets)) {
            coinfirmSupportedAssets = "XRP";
            log.warn("capitalCheckService-coinfirm_supported_assets not configured, default support 'XRP'!");
        }

        String chainalysisSupportedAssets = configVarCacheService.getValue("chainalysis_supported_assets");

        Set<String> totalSupportedAssets = new HashSet<>(Arrays.asList(coinfirmSupportedAssets.split(",")));

        if (StringUtils.isNotBlank(chainalysisSupportedAssets)) {
            totalSupportedAssets.addAll(Arrays.asList(chainalysisSupportedAssets.split(",")));
        }

        // elliptic 审查
        // BTC, BCH, LTC, ETH, XRP, ZIL and ERC-20 (BNB, BAT, USDT, USDC, GUSD, ZRX, PAX
        String ellipticSupportedAssets = configVarCacheService.getValue("elliptic_supported_assets");
        if (StringUtils.isBlank(ellipticSupportedAssets)) {
            ellipticSupportedAssets = "";
            log.warn("elliptic_supported_assets not configured!");
        }
        if (StringUtils.isNotBlank(ellipticSupportedAssets)) {
            totalSupportedAssets.addAll(Arrays.asList(ellipticSupportedAssets.split(",")));
        }

        // ciphertrace 审核 bep2
        // AERGO,ANKR,ARN,ARPA,ART,BCH,BCHABC,BCPT,BEAR,BKRW,BLINK,BNB,BOLT,BTC,BTCB,BTT,BULL,BUSD,CAN,CBM,CHZ,COS,COTI,
        // DUSK,ENTRP,EOSBEAR,EOSBULL,ERD,ETH,ETHBEAR,ETHBULL,FTM,GTO,HNST,KAVA,LBA,LTC,LTO,MATIC,MITH,ONE,PHB,TOMO,TROY,
        // TRX,TUSD,TUSDB,UND,USDS,USDSB,VRAB,WIN,WRX,XRP,XRPBEAR,XRPBULL,XTX
        String ciphertraceSupportedAssets = configVarCacheService.getValue("ciphertrace_supported_assets");
        if (StringUtils.isBlank(ciphertraceSupportedAssets)) {
            ciphertraceSupportedAssets = "";
            log.warn("ciphertrace_supported_assets not configured!");
        }
        if (StringUtils.isNotBlank(ciphertraceSupportedAssets)) {
            totalSupportedAssets.addAll(Arrays.asList(ciphertraceSupportedAssets.split(",")));
        }

        if(totalSupportedAssets.contains(coin.toUpperCase())){
            boolean isNeedBep2AddressVerify = "1".equalsIgnoreCase(configVarCacheService.getValue("chainalysis_bep2_address_verify"));
            if (sourceAddresses.startsWith("bnb")) {
                if (!isNeedBep2AddressVerify) {
                    return hasFraud;
                }
            }

            //兼容切换使用不同的CA服务
            //法币站点chainalysis,elliptic,coinfirm
            //主站 elliptic,ciphertrace
            String caChannelStrategy=configVarCacheService.getValue("ca_channel_strategy");

            log.info("capitalCheckService - load sysconfig check_deposit_strategy: {}",caChannelStrategy);

            // 无需任何审查？
            if ("none".equalsIgnoreCase(caChannelStrategy)) {
                return hasFraud;
            }

            UserTransactionRequest userTransactionRequest = new UserTransactionRequest();
            userTransactionRequest.setAsset(coin);
            userTransactionRequest.setTxHash(txHash);
            userTransactionRequest.setUserId(userId);

            if ("BTC".equalsIgnoreCase(coin)||"LTC".equalsIgnoreCase(coin)) {
                userTransactionRequest.setOutputAddress(targetAddress);
            }
            log.info("capitalCheckService - UserTransactionRequest: {}",JSON.toJSONString(userTransactionRequest));

            APIResponse<?> apiResponse = null;
            // 没有配置渠道策略？ 走默认流程
            if (StringUtils.isBlank(caChannelStrategy)) {
                apiResponse = chainalysisApi.addReceivedOutput(APIRequest.instance(userTransactionRequest));
                return checkFraud(userId, coin, sourceAddresses, targetAddress, chargeId, manualCheckEnabled, blockCriteria, apiResponse,"Chainalysis");
            }

            UserCoinAddressRequest userCoinAddressRequest = new UserCoinAddressRequest();
            userCoinAddressRequest.setAsset(coin);
            userCoinAddressRequest.setAddress(sourceAddresses);
            userCoinAddressRequest.setUserId(userId);

            // 需要Coinfirm审查
            if (Arrays.asList(coinfirmSupportedAssets.split(",")).contains(coin.toUpperCase())
                    && caChannelStrategy.toLowerCase().contains("coinfirm")) {
                apiResponse = coinfirmApi.checkAddress(APIRequest.instance(userCoinAddressRequest));
                hasFraud = checkFraud(userId, coin, sourceAddresses, targetAddress, chargeId, manualCheckEnabled, blockCriteria, apiResponse,"Coinfirm");
                if (hasFraud) {
                    return true;
                }
            }

            // 主站，checkout充值用户用户进行校验
            String isFiatStr = StringUtils.isEmpty(configVarCacheService.getValue("isFiat")) ? "true" : configVarCacheService.getValue("isFiat");
            Boolean isFiat = Boolean.valueOf(isFiatStr);
            if (!isFiat) {
                try {
                    UsedChannelsByIdRequest usedChannelsByIdRequest = new UsedChannelsByIdRequest();
                    usedChannelsByIdRequest.setUserId(userId);
                    APIResponse<UsedChannelsByIdResponse> usedChannelsByIdResponseApiResponse = fiatKycSupportApi.getUsedChannelsByUserId(APIRequest.instance(usedChannelsByIdRequest));
                    if (usedChannelsByIdResponseApiResponse != null && APIResponse.Status.OK == usedChannelsByIdResponseApiResponse.getStatus()) {
                        UsedChannelsByIdResponse usedChannelsByIdResponse = usedChannelsByIdResponseApiResponse.getData();
                        log.info("fiatKycSupportApi.getUsedChannelsByUserId, userId:{}, response:{}", userId, usedChannelsByIdResponseApiResponse);
                        if (usedChannelsByIdResponse != null) {
                            if (!usedChannelsByIdResponse.isUsed()) {
                                return false;
                            } else {
                                UserChannelRiskRatingVo userChannelRiskRatingVo = userChannelRiskRating.getUserChannelRiskRating(Long.valueOf(userId), UserRiskRatingChannelCode.CHECKOUT.name());
                                if (userChannelRiskRatingVo != null && UserRiskRatingStatus.DISABLE.name().equalsIgnoreCase(userChannelRiskRatingVo.getStatus())) {
                                    return false;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("fiatKycSupportApi.getUsedChannelsByUserId warn", e);
                }
            }

            // 需要elliptic审查
            if (Arrays.asList(ellipticSupportedAssets.split(",")).contains(coin.toUpperCase())
                    && caChannelStrategy.toLowerCase().contains("elliptic")) {

                UserTransactionRequest localRequest = new UserTransactionRequest();
                localRequest.setAsset(coin.toUpperCase());
                localRequest.setTxHash(txHash);
                localRequest.setUserId(userId);
                localRequest.setOutputAddress(targetAddress);

                apiResponse = ellipticApi.checkTransaction(APIRequest.instance(localRequest));
                hasFraud = checkFraud(userId, coin, sourceAddresses, targetAddress, chargeId, manualCheckEnabled, blockCriteria, apiResponse,"Elliptic");
                if (hasFraud) {
                    return true;
                }
            }


            // 需要ciphertrace审查
            if (Arrays.asList(ciphertraceSupportedAssets.split(",")).contains(coin.toUpperCase())
                    && caChannelStrategy.toLowerCase().contains("ciphertrace")) {
                if (sourceAddresses.startsWith("bnb")) {//bep2支持此类型
                    UserCoinAddressRequest userCoinAddressRequest1 = new UserCoinAddressRequest();
                    userCoinAddressRequest1.setAddress(sourceAddresses);
                    userCoinAddressRequest1.setAsset(coin);
                    userCoinAddressRequest1.setUserId(userId);
                    apiResponse = ciphertraceApi.checkAddress(APIRequest.instance(userCoinAddressRequest1));
                    hasFraud = checkFraud(userId, coin, sourceAddresses, targetAddress, chargeId, manualCheckEnabled, blockCriteria, apiResponse, "Ciphertrace");
                    if (hasFraud) {
                        return true;
                    }
                }
            }

            // chainalysis 审查？
            if (caChannelStrategy.toLowerCase().contains("chainalysis")
                    && Arrays.asList(chainalysisSupportedAssets.split(",")).contains(coin.toUpperCase())) {
                apiResponse = chainalysisApi.addReceivedOutput(APIRequest.instance(userTransactionRequest));
                hasFraud = checkFraud(userId, coin, sourceAddresses, targetAddress, chargeId, manualCheckEnabled, blockCriteria, apiResponse, "Chainalysis");
                if (hasFraud) {
                    return true;
                }
            }
        }

        return hasFraud;
    }

    @Override
    public WithdrawAddressCheckResponse getAddressBlackByAddress(String userId, String asset, String address, BigDecimal amount) throws Exception{
        if(isAddressInWhiteList(address)){
            return null;
        }

        WithdrawAddressCheckResponse response = checkWithdrawAddress(userId, asset, address);

        if (StringUtils.equalsAnyIgnoreCase(apolloCommonConfig.getEnableReport(),"1","on","true")) {
            sendToIdm(userId, asset, amount, response);
        }

        return response;
    }


    boolean isAddressInWhiteList(String address){
        String whiteList = RedisCacheUtils.get(AccountConstants.GLOBAL_RISK_CHECK_WHITE_ADDRESSES);
        if(StringUtils.isBlank(whiteList)){
            userChainAddressBusiness.refreshWhiteAddrCache();
            whiteList = RedisCacheUtils.get(AccountConstants.GLOBAL_RISK_CHECK_WHITE_ADDRESSES);
        }
        if(StringUtils.isBlank(whiteList)){
            log.warn("the whiteList is blank, the address is:{}", address);
            return false;
        }else {
            log.info("judge if address is in white list, the address is:{}, the whitelist is:{} ", address, whiteList);
            return whiteList.contains(address);
        }
    }


    /**
     * 交易相关信息发送至 IDM
     *
     * @param userId
     * @param asset
     * @param amount
     * @param response
     * @throws Exception
     */
    private void sendToIdm(String userId, String asset, BigDecimal amount, WithdrawAddressCheckResponse response) throws Exception {
        // 通知 提现 及 地址检测 结果给 IDM.
        // 1 获取该币种对USD的价格
        PriceConvertRequest request = new PriceConvertRequest();
        request.setFrom(asset);
        request.setTo("USD");
        request.setAmount(amount);
        BigDecimal coinToUsdAmount = new BigDecimal(1);
        APIResponse<PriceConvertResponse> responseAPIResponse = productApi.priceConvert(APIRequest.instance(request));
        if (responseAPIResponse!=null
                && responseAPIResponse.getStatus()== APIResponse.Status.OK
                && responseAPIResponse.getData()!=null) {
            coinToUsdAmount = responseAPIResponse.getData().getAmount();
        }
        else {
            log.warn("capitalCheckService - Cannot get USD amount for [{}] [{}].", amount, asset);
        }

        IdmTrxData idmTrxData = IdmTrxData.builder()
                .amt(String.valueOf(coinToUsdAmount))
                .tti(String.valueOf(System.currentTimeMillis()/1000))
                .man(userId)
                .memo1(asset)
                .memo2(amount==null?"0":amount.toString())
                .memo3(response == null?"lowRisk":"highRisk")
                .build();

        log.info("capitalCheckService - withdraw tx:[{}] will be sent to idm.", idmTrxData);

        // 2 发送给IDM
        idmApi.consumerSingleWithdrawEvaluation(APIRequest.instance(idmTrxData));
    }

    private WithdrawAddressCheckResponse checkWithdrawAddress(String userId, String asset, String address) {
        // Check DB first
        WithdrawAddressCheckResponse withdrawAddressCheckResponse =  this.getBlackListAddressByAddress(address);
        if (withdrawAddressCheckResponse != null) {
            return withdrawAddressCheckResponse;
        }

        if(StringUtils.isBlank(address)){
            return null;
        }

        // 第三方渠道地址检测未打开
        if (!"1".equalsIgnoreCase(configVarCacheService.getValue("chainalysis_check_withdrawal"))) {
            return null;
        }

        String coinfirmSupportedCoins = configVarCacheService.getValue("coinfirm_supported_assets");
        // coinfirm默认支持XRP
        if (StringUtils.isBlank(coinfirmSupportedCoins)) {
            coinfirmSupportedCoins = "XRP";
            log.warn("capitalCheckService - coinfirm_supported_assets not configured, set to default: XRP.");
        }

        List<String> coinfirmSupportedCoinsList = Arrays.asList(coinfirmSupportedCoins.split(","));
        Set<String> totalSupportedCoins = new HashSet<>(coinfirmSupportedCoinsList);

        String chainalysisSupportedCoins = configVarCacheService.getValue("chainalysis_supported_assets");
        if (StringUtils.isBlank(chainalysisSupportedCoins)) {
            chainalysisSupportedCoins = "";
            log.warn("capitalCheckService - chainalysis_supported_assets not configured!");
        }

        List<String> chainalysisSupportedCoinsList = Arrays.asList(chainalysisSupportedCoins.split(","));
        totalSupportedCoins.addAll(chainalysisSupportedCoinsList);

        // elliptic 审查
        // BTC, BCH, LTC, ETH, XRP, ZIL and ERC-20 (BNB, BAT, USDT, USDC, GUSD, ZRX, PAX
        String ellipticSupportedAssets = configVarCacheService.getValue("elliptic_supported_assets");
        if (StringUtils.isBlank(ellipticSupportedAssets)) {
            ellipticSupportedAssets = "";
            log.warn("elliptic_supported_assets not configured!");
        }
        List<String> ellipticSupportedAssetList = Arrays.asList(ellipticSupportedAssets.split(","));
        if (StringUtils.isNotBlank(ellipticSupportedAssets)) {
            totalSupportedCoins.addAll(ellipticSupportedAssetList);
        }

        // ciphertrace 审核 bep2
        // AERGO,ANKR,ARN,ARPA,ART,BCH,BCHABC,BCPT,BEAR,BKRW,BLINK,BNB,BOLT,BTC,BTCB,BTT,BULL,BUSD,CAN,CBM,CHZ,COS,COTI,
        // DUSK,ENTRP,EOSBEAR,EOSBULL,ERD,ETH,ETHBEAR,ETHBULL,FTM,GTO,HNST,KAVA,LBA,LTC,LTO,MATIC,MITH,ONE,PHB,TOMO,TROY,
        // TRX,TUSD,TUSDB,UND,USDS,USDSB,VRAB,WIN,WRX,XRP,XRPBEAR,XRPBULL,XTX
        String ciphertraceSupportedAssets = configVarCacheService.getValue("ciphertrace_supported_assets");
        if (StringUtils.isBlank(ciphertraceSupportedAssets)) {
            ciphertraceSupportedAssets = "";
            log.warn("ciphertrace_supported_assets not configured!");
        }
        List<String> ciphertraceSupportedAssetsList = Arrays.asList(ciphertraceSupportedAssets.split(","));
        if (StringUtils.isNotBlank(ciphertraceSupportedAssets)) {
            totalSupportedCoins.addAll(ciphertraceSupportedAssetsList);
        }

        if (totalSupportedCoins.contains(asset.toUpperCase())) {

            boolean isNeedBep2AddressVerify = "1".equalsIgnoreCase(configVarCacheService.getValue("chainalysis_bep2_address_verify"));
            if (address.startsWith("bnb")) {
                if (!isNeedBep2AddressVerify) {
                    return null;
                }
            }
            boolean manualCheckEnabled = "1".equalsIgnoreCase(configVarCacheService.getValue("chainalysis_manual_check_withdrawal"));
            String blockCriteria = configVarCacheService.getValue("chainalysis_block_criteria");
            if (StringUtils.isBlank(blockCriteria)) {
                blockCriteria = "highRisk";
            }

            UserCoinAddressRequest request = new UserCoinAddressRequest();
            request.setAddress(address);
            request.setUserId(userId);
            request.setAsset(asset);

            String caChannelStrategy=configVarCacheService.getValue("ca_channel_strategy");
            log.info("capitalCheckService - load sysconfig check_deposit_strategy: {}, blockCriteria:{}, input Query:{}",caChannelStrategy, blockCriteria, request);

            // 无需任何审查？直接返回
            if ("none".equalsIgnoreCase(caChannelStrategy)) {
                return null;
            }

            //兼容切换使用不同的CA服务
            APIResponse<?> apiResponse =null;

            // 没有配置 或者 配置为 chainalysis 去 chainalysis 检测
            if (StringUtils.isBlank(caChannelStrategy)
                    || (chainalysisSupportedCoinsList.contains(asset.toUpperCase()) && caChannelStrategy.toLowerCase().contains("chainalysis"))) {
                apiResponse = chainalysisApi.createWithdrawalAddress(APIRequest.instance(request));
                WithdrawAddressCheckResponse blackListModel = getWithdrawAddressCheckResponse(userId, asset, address, manualCheckEnabled, blockCriteria, apiResponse, "Chainalysis");
                if (blackListModel != null) {
                    return blackListModel;
                }
            }

            // 需要coinfirm审查
            if (coinfirmSupportedCoinsList.contains(asset.toUpperCase())
                    && caChannelStrategy.toLowerCase().contains("coinfirm")) {
                apiResponse = coinfirmApi.checkAddress(APIRequest.instance(request));
                WithdrawAddressCheckResponse blackListModel = getWithdrawAddressCheckResponse(userId, asset, address, manualCheckEnabled, blockCriteria, apiResponse, "Coinfirm");
                if (blackListModel != null) {
                    return blackListModel;
                }
            }

            // 主站，checkout充值用户用户进行校验
            String isFiatStr = StringUtils.isEmpty(configVarCacheService.getValue("isFiat")) ? "true" : configVarCacheService.getValue("isFiat");
            Boolean isFiat = Boolean.valueOf(isFiatStr);
            if (!isFiat) {
                try {
                    UsedChannelsByIdRequest usedChannelsByIdRequest = new UsedChannelsByIdRequest();
                    usedChannelsByIdRequest.setUserId(userId);
                    APIResponse<UsedChannelsByIdResponse> usedChannelsByIdResponseApiResponse = fiatKycSupportApi.getUsedChannelsByUserId(APIRequest.instance(usedChannelsByIdRequest));
                    log.info("fiatKycSupportApi.getUsedChannelsByUserId, userId:{}, response:{}", userId, usedChannelsByIdResponseApiResponse);
                    if (usedChannelsByIdResponseApiResponse != null && APIResponse.Status.OK == usedChannelsByIdResponseApiResponse.getStatus()) {
                        UsedChannelsByIdResponse usedChannelsByIdResponse = usedChannelsByIdResponseApiResponse.getData();
                        if (usedChannelsByIdResponse != null) {
                            if (!usedChannelsByIdResponse.isUsed()) {
                                return null;
                            } else {
                                UserChannelRiskRatingVo userChannelRiskRatingVo = userChannelRiskRating.getUserChannelRiskRating(Long.valueOf(userId), UserRiskRatingChannelCode.CHECKOUT.name());
                                if (userChannelRiskRatingVo != null && UserRiskRatingStatus.DISABLE.name().equalsIgnoreCase(userChannelRiskRatingVo.getStatus())) {
                                    return null;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("fiatKycSupportApi.getUsedChannelsByUserId warn", e);
                }
            }

            // 需要elliptic审查
            if (ellipticSupportedAssetList.contains(asset.toUpperCase())
                    && caChannelStrategy.toLowerCase().contains("elliptic")) {

                apiResponse = ellipticApi.checkAddress(APIRequest.instance(request));

                WithdrawAddressCheckResponse blackListModel = getWithdrawAddressCheckResponse(userId, asset, address, manualCheckEnabled, blockCriteria, apiResponse, "Elliptic");
                if (blackListModel != null) {
                    return blackListModel;
                }
            }


            // 需要ciphertrace审查
            if (ciphertraceSupportedAssetsList.contains(asset.toUpperCase())
                    && caChannelStrategy.toLowerCase().contains("ciphertrace")) {
                if (address.startsWith("bnb")) {//bep2支持此类型
                    apiResponse = ciphertraceApi.checkAddress(APIRequest.instance(request));
                    WithdrawAddressCheckResponse blackListModel = getWithdrawAddressCheckResponse(userId, asset, address, manualCheckEnabled, blockCriteria, apiResponse, "Ciphertrace");
                    if (blackListModel != null) {
                        return blackListModel;
                    }
                }
            }

        }

        return null;
    }

    private WithdrawAddressCheckResponse getBlackListAddressByAddress(String address) {
        WithdrawAddressCheckResponse addressBlackListModel = null;
        try {
            RiskAddressBlackListGetRequest request = new RiskAddressBlackListGetRequest();
            request.setAddress(address);
            APIResponse<RiskAddressBlackListQueryResponse> apiResponse = riskAddressBlackListApi.getAddressBlackByAddress(APIRequest.instance(request));
            if (isOk(apiResponse)) {
                RiskAddressBlackListQueryResponse responseData = apiResponse.getData();
                if (responseData != null) {
                    addressBlackListModel = new WithdrawAddressCheckResponse();
                    addressBlackListModel.setUserId(String.valueOf(responseData.getUserId()));
                    addressBlackListModel.setAddress(String.valueOf(responseData.getAddress()));
                    addressBlackListModel.setType(responseData.getType());
                    addressBlackListModel.setRemark(responseData.getRemark());
                }
            }
        } catch (Exception e) {
            log.error("capitalCheckService - getBlackListAddressByAddress error,", e);
        }
        return addressBlackListModel;
    }

    private WithdrawAddressCheckResponse getWithdrawAddressCheckResponse(String userId, String asset, String address, boolean manualCheckEnabled,
            String blockCriteria, APIResponse apiResponse, String channel) {
        if (isOk(apiResponse) && apiResponse.getData() != null) {
            Map<String, Object> responseData = (Map<String, Object>) apiResponse.getData();
            Object rating = responseData.get("rating");
            log.info("capitalCheckService - userId:{} withdrawal address:{}, chainalysis score:{}", userId, address, rating);
            // 主站，checkout充值用户用户进行校验
            String isFiatStr = StringUtils.isEmpty(configVarCacheService.getValue("isFiat")) ? "true" : configVarCacheService.getValue("isFiat");
            Boolean isFiat = Boolean.valueOf(isFiatStr);
            if (rating != null && blockCriteria.toLowerCase().contains(((String) rating).toLowerCase())) {
                RiskAddressBlackListInsertRequest request = new RiskAddressBlackListInsertRequest();
                request.setUserId(userId);
                request.setAddress(address);
                request.setType(3);
                request.setRemark("asset:" + asset + " userId:" + userId + " CA:" + rating);
                request.setCurrency(asset);
//                try {
//                    riskAddressBlackListApi.addAddressBlackList(APIRequest.instance(request));
//                } catch (Exception e) {
//                    log.error("capitalCheckService - Failed to call addAddressBlackList via RiskAddressBlackListApi.", e);
//                }

                log.info("capitalCheckService - 禁用自动提币，等待人工复核, userId={}", userId);
                // 用户禁止提币


                if (isFiat) {//主站默认不禁用用户提币
                    updateWithdrawStatus(userId, null, 1);
                }

                WithdrawAddressCheckResponse WithdrawAddressCheckResponse = new WithdrawAddressCheckResponse();
                WithdrawAddressCheckResponse.setAddress(address);
                // 1欺诈地址 2其他地址 3CA检查
                WithdrawAddressCheckResponse.setType(3);
                WithdrawAddressCheckResponse.setRemark("asset:" + asset + " userId:" + userId + " CA:" + rating);
                WithdrawAddressCheckResponse.setTime(new Date());
                WithdrawAddressCheckResponse.setUserId(userId);
                WithdrawAddressCheckResponse.setCurrency(asset);

                // 发到user_addr_chain_audit人工复审
                if (manualCheckEnabled) {
                    String detail = JSON.toJSONString(responseData);
                    //表字段长度限制 250
                    detail=detail.substring(0,Math.min(detail.length(),250));
                    address=address.substring(0,Math.min(address.length(),150));

                    // 人工审核，添加到user_chain_addr_audit表
                    ChainAddressAnalyzeRequest chainAddressAnalyzeRequest = new ChainAddressAnalyzeRequest();
                    chainAddressAnalyzeRequest.setUserId(Long.valueOf(userId));
                    chainAddressAnalyzeRequest.setChainAddress(address);
                    chainAddressAnalyzeRequest.setAnalyzeResult(detail);
                    chainAddressAnalyzeRequest.setCoin(asset);
                    chainAddressAnalyzeRequest.setChannel(channel);
                    chainAddressAnalyzeRequest.setDirection(ChainAddressAnalyzeRequest.Direction.WITHDRAW);

                    //主站默认生成豁免用户
                    chainAddressAnalyzeRequest.setStatus(isFiat?0:2);

                    APIResponse<?> submitResponse = userChainAddress.submitChainAddressAudit(APIRequest.instance(chainAddressAnalyzeRequest));
                    if (!isOk(submitResponse)) {
                        log.error("capitalCheckService - Failed to submit chain address for manual audit, errorMsg={}", submitResponse.getCode());
                    }
                }
                if (!isFiat) {
                    return null;
                }
                return WithdrawAddressCheckResponse;
            }
        } else {
            log.warn("capitalCheckService - Failed to do CA query:{}", getErrorMsg(apiResponse));
        }

        return null;
    }

    private boolean checkFraud(String userId, String coin, String sourceAddresses, String targetAddress, String chargeId, boolean manualCheckEnabled,
            String blockCriteria,APIResponse apiResponse, String channel) {
        if(isOk(apiResponse) && apiResponse.getData() != null){
            Map<String, Object> responseData = (Map<String, Object>) apiResponse.getData();
            Object rating = responseData.get("rating");
            // 主站，checkout充值用户用户进行校验
            String isFiatStr = StringUtils.isEmpty(configVarCacheService.getValue("isFiat")) ? "true" : configVarCacheService.getValue("isFiat");
            Boolean isFiat = Boolean.valueOf(isFiatStr);
            // 检测到风险，进入审核逻辑
            if (rating != null && blockCriteria.toLowerCase().contains(((String) rating).toLowerCase())) {
                // 添加入金地址到入金黑名单
                String remark = "asset:" + coin + " userId:" + userId + " CA:" + rating;
                addToSourceBlackList(userId, sourceAddresses, remark);

                // 禁止用户自动提币
                log.info("capitalCheckService - 禁用自动提币，等待人工复核, userId={}", userId);
                if(isFiat) {
                    updateWithdrawStatus(userId, null, 1);
                }

                // 人工审核，添加到user_chain_addr_audit表
                if (manualCheckEnabled) {
                    String detail = JSON.toJSONString(responseData);
                    //表字段长度限制 250
                    detail = detail.substring(0, Math.min(detail.length(), 250));

                    ChainAddressAnalyzeRequest chainAddressAnalyzeRequest = new ChainAddressAnalyzeRequest();
                    chainAddressAnalyzeRequest.setUserId(Long.valueOf(userId));
                    chainAddressAnalyzeRequest.setCoin(coin);
                    chainAddressAnalyzeRequest.setChainAddress(sourceAddresses.split(",")[0]);
                    chainAddressAnalyzeRequest.setAnalyzeResult(detail);
                    chainAddressAnalyzeRequest.setDirection(ChainAddressAnalyzeRequest.Direction.DEPOSIT);
                    chainAddressAnalyzeRequest.setBizId(chargeId);
                    chainAddressAnalyzeRequest.setChannel(channel);

                    //主站默认生成豁免用户
                    chainAddressAnalyzeRequest.setStatus(isFiat?0:2);

                    APIResponse<?> submitResponse = userChainAddress.submitChainAddressAudit(APIRequest.instance(chainAddressAnalyzeRequest));
                    if (!isOk(submitResponse)) {
                        log.error("capitalCheckService - Failed to submit chain address for manual audit, errorMsg={}", submitResponse.getCode());
                    }
                }
                return true;
            }
        } else {
            log.error("capitalCheckService - Failed to checkFraud:{}", JSON.toJSONString(apiResponse));
        }

        return false;
    }

    /**
     * 获取用户提现风控状态
     *
     * @param userId
     * @param withSecurityStatus
     * @param withSecurityAutoStatus
     */
    public void updateWithdrawStatus(String userId, Integer withSecurityStatus, Integer withSecurityAutoStatus) {

        try {
            log.info("capitalCheckService - updateWithdrawStatus 同步user_security");
            UpdateWithdrawStatusRequest updateWithdrawStatusRequest = new UpdateWithdrawStatusRequest();
            updateWithdrawStatusRequest.setUserId(Long.valueOf(userId));
            updateWithdrawStatusRequest.setWithdrawSecurityAutoStatus(withSecurityAutoStatus);
            updateWithdrawStatusRequest.setWithdrawSecurityStatus(withSecurityStatus);
            updateWithdrawStatusRequest.setChannel(UpdateWithdrawStatusChannelEnum.OTHER);
            updateWithdrawStatusRequest.setReason("CA校验命中");

            APIResponse<Integer> resp = userSecurityApi.updateWithdrawStatusByUserId(APIRequest.instance(updateWithdrawStatusRequest));
            // 不应该有失败的返回
            if (!isOk(resp)) {
                //retry 1次
                resp = userSecurityApi.updateWithdrawStatusByUserId(APIRequest.instance(updateWithdrawStatusRequest));
                if (!isOk(resp)) {
                    log.error("capitalCheckService - updateWithdrawStatusByUserId 同步 user_security 数据失败, userId:{}", userId);
                }
            }

            log.info("capitalCheckService - updateWithdrawStatusByUserId 同步 user_security 成功");
        } catch (Exception e) {
            log.error("capitalCheckService - updateWithdrawStatusByUserId 同步 user_security 异常.", e);
        }
    }

    private boolean isOk(APIResponse resp) {
        return resp !=null && resp.getStatus() == APIResponse.Status.OK;
    }

    private String getErrorMsg(APIResponse resp) {
        if (resp.getStatus() == APIResponse.Status.ERROR) {
            if (resp.getType() == APIResponse.Type.VALID) {
                Map<String, Object> objMap = (Map<String, Object>) resp.getErrorData();
                return Joiner.on("&").join(objMap.values().iterator());
            } else {
                return resp.getErrorData().toString();
            }
        }
        return null;
    }
    /**
     * 添加地址到入金黑名单
     * @param userId
     * @param sourceAddresses
     * @param remark
     */
    private void addToSourceBlackList(String userId, String sourceAddresses, String remark) {
//        if (StringUtils.isBlank(sourceAddresses)) {
//            return;
//        }
//        String[] sourceAddressList = sourceAddresses.split(",");
//        for (String address : sourceAddressList) {
//            log.info("capitalCheckService - 添加入金地址到入金黑名单, userId={}, address={}", userId, address);
//            try {
//                RiskAddressSourceBlackListInsertRequest request = new RiskAddressSourceBlackListInsertRequest();
//                request.setAddress(address.trim());
//                request.setRemark(remark);
//                // 1 欺诈地址 2 其他地址 3 CA验证
//                request.setType(3);
//                request.setUserId(userId);
//                riskAddressSourceBlackListApi.addAddressSourceBlackList(APIRequest.instance(request));
//            } catch (Exception e) {
//                log.warn("capitalCheckService -  - 添加入金地址到入金黑名单失败", e);
//            }
//        }
    }
}
