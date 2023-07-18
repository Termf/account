package com.binance.account.service.user.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.binance.account.vo.user.response.GenMarketMakerTransferTranIdResponse;
import com.binance.master.utils.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.data.entity.user.MarketMakerUser;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.MarketMakerUserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.assetservice.AssetApiClient;
import com.binance.account.integration.assetservice.UserAssetApiClient;
import com.binance.account.service.subuser.impl.CheckSubUserBusiness;
import com.binance.account.service.user.IMarketMakerUser;
import com.binance.account.vo.user.MarketMakerUserVo;
import com.binance.account.vo.user.request.AddMarketMakerUserRequest;
import com.binance.account.vo.user.request.GenMarketMakerTransferTranIdRequest;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.request.MarketMakerAssetQuery;
import com.binance.account.vo.user.request.MarketMakerTransferRequest;
import com.binance.account.vo.user.request.MarketMakerUserRequest;
import com.binance.assetservice.vo.response.UserAssetResponse;
import com.binance.assetservice.vo.response.asset.AssetResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.StringUtils;
import com.google.common.collect.Lists;

import lombok.extern.log4j.Log4j2;

/**
 * @author zhao chenkai
 * @date 2019/11/06
 */
@Log4j2
@Service
@Monitored
public class MarketMakerUserBusiness extends CheckSubUserBusiness implements IMarketMakerUser {

    @Value("${marketMaker.publicAccounts:}")
    private String marketMakerPublicAccounts;

    @Autowired
    private MarketMakerUserMapper marketMakerUserMapper;
    @Autowired
    private AssetApiClient assetApiClient;
    @Autowired
    private UserAssetApiClient userAssetApiClient;

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Long> add(APIRequest<AddMarketMakerUserRequest> request) {
        final AddMarketMakerUserRequest requestBody = request.getBody();
        if (StringUtils.isBlank(requestBody.getEmail()) && requestBody.getUserId() == null) {
            return APIResponse.getErrorJsonResult("用户id和邮箱不能同时为空");
        }
        Long userId = requestBody.getUserId();
        User user = null;
        if (userId == null) {
            // 校验email
            String email = requestBody.getEmail();
            user = checkAndGetUserByEmail(email);
            userId = user.getUserId();
        } else {
            // 校验userId
            user = checkAndGetUserById(userId);
        }

        MarketMakerUser existUser = marketMakerUserMapper.selectByPrimaryKey(userId);
        if (existUser != null) {
            return APIResponse.getErrorJsonResult("该账号已经是做市商账号");
        }

        MarketMakerUser marketMakerUser = new MarketMakerUser();
        marketMakerUser.setUserId(userId);
        marketMakerUser.setEmail(user.getEmail());
        marketMakerUser.setRemark(requestBody.getRemark());
        marketMakerUser.setCreateTime(new Date());
        marketMakerUserMapper.insert(marketMakerUser);

        // 开启提币白名单, enable 2fa
        final User updateDO = new User();
        updateDO.setEmail(user.getEmail());
        Long toStatus = BitUtils.enable(user.getStatus(), Constant.USER_WITHDRAW_WHITE);
        toStatus = BitUtils.enable(toStatus, Constant.USER_GOOGLE);
        updateDO.setStatus(toStatus);

        this.userMapper.updateUserStatusByEmail(updateDO);

        return APIResponse.getOKJsonResult(userId);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Void> delete(APIRequest<IdRequest> request) {
        final IdRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        if (userId == null) {
            return APIResponse.getErrorJsonResult("userId can not be null");
        }
        User user = checkAndGetUserById(userId);
        MarketMakerUser marketMakerUser = marketMakerUserMapper.selectByPrimaryKey(userId);
        if (marketMakerUser == null) {
            return APIResponse.getErrorJsonResult("该账号不是做市商账号");
        }

        // 关闭提币白名单
        final User updateDO = new User();
        updateDO.setEmail(user.getEmail());
        updateDO.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_WITHDRAW_WHITE));
        this.userMapper.updateUserStatusByEmail(updateDO);

        // 删除做市商账号
        int count = marketMakerUserMapper.deleteByPrimaryKey(userId);
        log.info("做市商账号删除成功 userId={} count={}", userId, count);
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Boolean> isMarketMaker(APIRequest<IdRequest> request) {
        final IdRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        if (userId == null) {
            return APIResponse.getErrorJsonResult("userId can not be null");
        }

        MarketMakerUser marketMakerUser = marketMakerUserMapper.selectByPrimaryKey(userId);
        Boolean exist = marketMakerUser != null;
        return APIResponse.getOKJsonResult(exist);
    }

    @Override
    public APIResponse<List<MarketMakerUserVo>> marketMakerUserList(APIRequest<MarketMakerUserRequest> request) {
        final MarketMakerUserRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        final String email = requestBody.getEmail();
        // 查询做市商
        MarketMakerUser queryDO = new MarketMakerUser();
        queryDO.setUserId(userId);
        queryDO.setEmail(email);
        List<MarketMakerUser> marketMakerUsers = marketMakerUserMapper.selectDynamic(queryDO);
        if (CollectionUtils.isEmpty(marketMakerUsers)) {
            return APIResponse.getOKJsonResult(new ArrayList<>());
        }

        List<MarketMakerUserVo> userVoList = marketMakerUsers.stream().map(x -> {
            MarketMakerUserVo userVo = new MarketMakerUserVo();
            BeanUtils.copyProperties(x, userVo);
            return userVo;
        }).collect(Collectors.toList());

        return APIResponse.getOKJsonResult(userVoList);
    }

    @Override
    public void marketMakerEnable2fa(Long userId) {
        // 如果不是做市商，不处理
        MarketMakerUser existUser = marketMakerUserMapper.selectByPrimaryKey(userId);
        if (existUser == null) {
            return;
        }

        // 校验user
        User user = checkAndGetUserById(userId);

        User updateDO = new User();
        updateDO.setEmail(user.getEmail());
        updateDO.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_GOOGLE));
        this.userMapper.updateUserStatusByEmail(updateDO);
        log.info("marketMakerEnable2fa success, userId={}", userId);
    }

    @Override
    public APIResponse<Void> transferToMarketMaker(APIRequest<MarketMakerTransferRequest> request) throws Exception {
        final MarketMakerTransferRequest requestBody = request.getBody();
        final Long publicAccount = requestBody.getPublicAccount();
        log.info("transferToMarketMaker formatRequest={}", JsonUtils.toJsonNotNullKey(requestBody));
        requestBody.setAmount(requestBody.getAmount().setScale(8,BigDecimal.ROUND_DOWN));

        // 做市商账号校验
        validateMarketMakerAccount(requestBody.getMarketMakerUserId(), publicAccount);

        // 账号及资产校验
        validateMarketMakerTransfer(publicAccount, requestBody.getMarketMakerUserId(), requestBody.getAsset(), requestBody.getAmount());
        // 调用划转接口
        Date tranTime = new Date(requestBody.getTranTime());
        userAssetApiClient.walletAssetTransferAdmin(publicAccount.toString(),requestBody.getMarketMakerUserId().toString(),requestBody.getAsset(),requestBody.getAmount(),requestBody.getTranId(), tranTime);
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<BigDecimal> assetQuery(APIRequest<MarketMakerAssetQuery> request) throws Exception {
        final MarketMakerAssetQuery requestBody = request.getBody();
        final Long publicAccount = requestBody.getPublicAccount();
        if (StringUtils.isBlank(requestBody.getAsset())) {
            throw new BusinessException("asset can not be null");    
        }

        // 是否是对公账号
        if (StringUtils.isBlank(marketMakerPublicAccounts)) {
            log.error("validateMarketMakerTransfer error , no public account exist");
            throw new BusinessException("marketMaker validate failed, no public account exist");
        }
        String[] publicAccounts = marketMakerPublicAccounts.split("[,，;；]");
        List publicAccountList = Lists.newArrayList(publicAccounts);
        if (!publicAccountList.contains(publicAccount.toString())) {
            log.error("assetQuery error , {} is not a public account", publicAccount);
            throw new BusinessException("assetQuery validate failed");
        }
        
        //查询资产
        UserAssetResponse userAssetResponse=userAssetApiClient.getPrivateUserAsset(publicAccount.toString(),requestBody.getAsset());
        if(null==userAssetResponse||CollectionUtils.isEmpty(userAssetResponse.getUserAssetList())){
            throw new BusinessException(AccountErrorCode.USER_HAVE_NO_ASSET);
        }
        BigDecimal free = userAssetResponse.getUserAssetList().get(0).getFree();

        return APIResponse.getOKJsonResult(free);
    }

    @Override
    public APIResponse<GenMarketMakerTransferTranIdResponse> genMarketMakerTransferTranId(APIRequest<GenMarketMakerTransferTranIdRequest> request) throws Exception {
        final GenMarketMakerTransferTranIdRequest requestBody = request.getBody();
        log.info("genMarketMakerTransferTranId formatRequest={}", JsonUtils.toJsonNotNullKey(requestBody));
        final Long marketMakerUserId = requestBody.getMarketMakerUserId();
        final Long publicAccount = requestBody.getPublicAccount();
        
        // 做市商账号校验
        validateMarketMakerAccount(marketMakerUserId, publicAccount);

        try{
            Date transferTime = DateUtils.getNewUTCDate();
            // 创建流水号
            Long recipientUserId = requestBody.getTransferFrom() == 0 ? marketMakerUserId : publicAccount; 
            Long transactionId = tranApiClient.getTransIdForMarketMakerTransfer(recipientUserId.toString(), transferTime);

            GenMarketMakerTransferTranIdResponse response = new GenMarketMakerTransferTranIdResponse();
            response.setTranId(transactionId);
            response.setTransferTime(transferTime.getTime());
            return APIResponse.getOKJsonResult(response);
        }catch (Exception e){
            log.error("genMarketMakerTransferTranId error", e);
            throw new BusinessException("genMarketMakerTransferTranId.failed");
        }
    }

    @Override
    public APIResponse<Void> transferFromMarketMaker(APIRequest<MarketMakerTransferRequest> request) throws Exception {
        final MarketMakerTransferRequest requestBody = request.getBody();
        final Long publicAccount = requestBody.getPublicAccount();
        log.info("transferFromMarketMaker formatRequest={}", JsonUtils.toJsonNotNullKey(requestBody));
        requestBody.setAmount(requestBody.getAmount().setScale(8,BigDecimal.ROUND_DOWN));

        // 做市商账号校验
        validateMarketMakerAccount(requestBody.getMarketMakerUserId(), publicAccount);

        // 账号及资产校验
        validateMarketMakerTransfer(requestBody.getMarketMakerUserId(), publicAccount, requestBody.getAsset(), requestBody.getAmount());
        // 调用划转接口
        Date tranTime = new Date(requestBody.getTranTime());
        userAssetApiClient.walletAssetTransferAdmin(requestBody.getMarketMakerUserId().toString(),publicAccount.toString(),requestBody.getAsset(),requestBody.getAmount(),requestBody.getTranId(), tranTime);
        return APIResponse.getOKJsonResult();
    }

    private void validateMarketMakerAccount(Long marketMakerUserId, Long publicAccount) {
        // 是否是做市商账号
        MarketMakerUser marketMakerUser = marketMakerUserMapper.selectByPrimaryKey(marketMakerUserId);
        if (marketMakerUser == null) {
            log.error("validateMarketMakerTransfer error , {} is not a marketMaker user", marketMakerUserId);
            throw new BusinessException(String.format("marketMaker validate failed, %s is not a marketMaker user", marketMakerUserId));
        }

        // 是否是对公账号
        if (StringUtils.isBlank(marketMakerPublicAccounts)) {
            log.error("validateMarketMakerTransfer error , no public account exist");
            throw new BusinessException("marketMaker validate failed, no public account exist");    
        }
        String[] publicAccounts = marketMakerPublicAccounts.split("[,，;；]");
        List publicAccountList = Lists.newArrayList(publicAccounts);
        if (!publicAccountList.contains(publicAccount.toString())) {
            log.error("validateMarketMakerTransfer error , {} is not a public account", publicAccount);
            throw new BusinessException(String.format("marketMaker validate failed, %s is not a public account", publicAccount));
        }    
    }

    private void validateMarketMakerTransfer(Long senderUserId, Long recipientUserId, String asset, BigDecimal amount)throws Exception {
        //常规非空校验
        if (StringUtils.isAnyBlank(senderUserId.toString(), recipientUserId.toString(), asset)
                || null == amount || amount.compareTo(BigDecimal.ZERO) < 1) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 校验用户
        User senderUser =checkAndGetUserById(senderUserId);
        User recipientUser =checkAndGetUserById(recipientUserId);

        //获取当前资产信息是否存在
        AssetResponse assetResponse=assetApiClient.getAssetByCode(asset);
        if(null==assetResponse){
            throw new BusinessException(AccountErrorCode.ASSET_IS_NOT_EXIST);
        }
        //查询当前用户的资产是否存在
        UserAssetResponse userAssetResponse=userAssetApiClient.getPrivateUserAsset(senderUserId.toString(),asset);
        if(null==userAssetResponse||CollectionUtils.isEmpty(userAssetResponse.getUserAssetList())){
            throw new BusinessException(AccountErrorCode.USER_HAVE_NO_ASSET);
        }
        //当前用户的资产
        if(amount.compareTo(userAssetResponse.getUserAssetList().get(0).getFree()) > 0){
            throw new BusinessException(AccountErrorCode.USER_HAVE_NO_AVALIABLE_AMOUNT);
        }
    }
    

}
