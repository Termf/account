package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.entity.broker.BrokerCommissionUpdateBak;
import com.binance.account.data.entity.broker.BrokerUserCommisssion;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.broker.BrokerCommissionUpdateBakMapper;
import com.binance.account.data.mapper.broker.BrokerCommissionWhiteMapper;
import com.binance.account.data.mapper.broker.BrokerUserCommisssionMapper;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.integration.futureengine.FutureAccountApiClient;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.subuser.impl.BrokerSubUserAdminService;
import com.binance.account.vo.subuser.response.BrokerCommissionFuturesJobResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.utils.*;
import com.binance.matchbox.api.AccountApi;
import com.binance.matchbox.vo.TradingAccountDetails;
import com.binance.memgmt.api.client.domain.general.CommissionResponse;
import com.binance.memgmt.api.client.domain.general.FeeAdjustResponse;
import com.binance.memgmt.api.client.domain.general.NewSymbolResponse;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by yangyang on 2019/6/17.
 */
@Log4j2
@JobHandler(value = "updateBrokerFeeJobHandler")
@Component
public class UpdateBrokerFeeJobHandler extends IJobHandler{

    @Resource
    private BrokerUserCommisssionMapper brokerUserCommisssionMapper;
    @Resource
    private BrokerCommissionWhiteMapper brokerCommissionWhiteMapper;
    @Resource
    private BrokerCommissionUpdateBakMapper brokerCommissionUpdateBakMapper;
    @Resource
    private SubUserBindingMapper subUserBindingMapper;
    @Autowired
    private FutureAccountApiClient futureAccountApiClient;
    @Resource
    private IMsgNotification iMsgNotification;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private AccountApi accountApi;
    @Resource
    private UserMapper userMapper;

    /**
     * 1 备份broekr_user_commisssion
     * 2.执行job-验证数据
     * 3.执行回滚job
     * @param s
     * @return
     * @throws Exception
     */


    @Override
    @Trace
    public ReturnT<String> execute(String s) throws Exception {
        TrackingUtils.saveTraceId();
        log.info("updateBrokerFeeJobHandler.execute.start,param:{}",s);
        List<Long> whiteUserIds = brokerCommissionWhiteMapper.selectAll();
        List<BrokerUserCommisssion> brokerUserCommisssions = null;
        if (StringUtils.isNumeric(s) && !whiteUserIds.contains(Long.parseLong(s))){
            BrokerUserCommisssion brokerUserCommisssion = brokerUserCommisssionMapper.selectByUserId(Long.parseLong(s));
            brokerUserCommisssions = Lists.newArrayList(brokerUserCommisssion);
        }else{
            brokerUserCommisssions = brokerUserCommisssionMapper.selectAllNotInWhite(whiteUserIds);
        }
        log.info("updateBrokerFeeJobHandler.execute.brokerUserCommisssions:{}",JsonUtils.toJsonHasNullKey(brokerUserCommisssions));
        if (CollectionUtils.isEmpty(brokerUserCommisssions) || brokerUserCommisssions.size() == 0){
            return ReturnT.SUCCESS;
        }
        for (BrokerUserCommisssion brokerUserCommisssion:brokerUserCommisssions){
            log.info("updateBrokerFeeJobHandler.brokerUserCommisssion:{}",JsonUtils.toJsonHasNullKey(brokerUserCommisssion));
            checkAndset(brokerUserCommisssion);
            //1.update broker parent
            brokerUserCommisssionMapper.updateByPrimaryKeySelective(brokerUserCommisssion);
            //2.获取所有子账户
            List<SubUserBinding> bindings = subUserBindingMapper.getSubUserBindingsByParentUserId(brokerUserCommisssion.getUserId());
            log.info("updateBrokerFeeJobHandler.brokerParentUserId:{}",bindings);
            for (SubUserBinding binding:bindings){
                try {
                    doSetSpotAndMarginCommission(binding.getSubUserId(),brokerUserCommisssion,0);
                    UserInfo userInfo = userInfoMapper.selectByPrimaryKey(binding.getSubUserId());
                    if (userInfo != null && userInfo.getMarginUserId() != null){
                        doSetSpotAndMarginCommission(userInfo.getMarginUserId(),brokerUserCommisssion,1);
                    }
                    doSetFutureCommission(binding.getSubUserId(),brokerUserCommisssion);
                }catch (Exception e){
                    log.error("updateBrokerFeeJobHandler.binding.error,userId:{},e:{}",binding.getSubUserId(),e);
                }


            }
        }
        log.info("updateBrokerFeeJobHandler.execute.end");
        return ReturnT.SUCCESS;
    }

    private void doSetFutureCommission(Long subUserId, BrokerUserCommisssion brokerUserCommisssion) {
        log.info("UpdateBrokerFeeJobHandler.doSetFutureCommission.subUserId:{},brokerUserCommisssion:{}",subUserId,JsonUtils.toJsonHasNullKey(brokerUserCommisssion));
        if (subUserId == null || brokerUserCommisssion == null){
            return;
        }
        UserInfo subUserInfo = userInfoMapper.selectByPrimaryKey(subUserId);
        if (subUserInfo == null || subUserInfo.getTradingAccount() == null || subUserInfo.getFutureUserId() == null) {
            return;
        }
        Long futureUserId=subUserInfo.getFutureUserId();
        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
        if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
            log.info("UpdateBrokerFeeJobHandler.doSetFutureCommission.futureUserInfo or meTradingAccount is null,subUserId:{},futureUserId:{}",subUserId,futureUserId);
            return;
        }
        Long futureTradingAccount=futureUserInfo.getMeTradingAccount();
        List<CommissionResponse> commissionResponseList= futureAccountApiClient.getCommissionByFutureAccountId(futureTradingAccount,true);
        Map<Integer,NewSymbolResponse> newSymbolResponseMap= futureAccountApiClient.getAllSymbolMap();
        List<BrokerCommissionFuturesJobResponse> brokerQueryCommissionFuturesResponseList=Lists.newArrayList();
        for(CommissionResponse commissionResponse:commissionResponseList){
            BrokerCommissionFuturesJobResponse brokerCommissionFuturesJobResponse=new BrokerCommissionFuturesJobResponse();
            brokerCommissionFuturesJobResponse.setSubUserId(subUserId);
            NewSymbolResponse newSymbolResponse=newSymbolResponseMap.get(commissionResponse.getSymbolId());
            brokerCommissionFuturesJobResponse.setSymbol(newSymbolResponse.getSymbol());
            brokerCommissionFuturesJobResponse.setMakerAdjustment(commissionResponse.getMakerCommissionAdjust());
            brokerCommissionFuturesJobResponse.setTakerAdjustment(commissionResponse.getTakerCommissionAdjust());
            brokerCommissionFuturesJobResponse.setMakerCommission(commissionResponse.getMakerCommission());
            brokerCommissionFuturesJobResponse.setTakerCommission(commissionResponse.getTakerCommission());
            brokerQueryCommissionFuturesResponseList.add(brokerCommissionFuturesJobResponse);
        }
        log.info("UpdateBrokerFeeJobHandler.doSetFutureCommission.subUserId:{},brokerQueryCommissionFuturesResponseList:{}",subUserId,JsonUtils.toJsonHasNullKey(brokerQueryCommissionFuturesResponseList));
        if (CollectionUtils.isEmpty(brokerQueryCommissionFuturesResponseList) || brokerQueryCommissionFuturesResponseList.size() <= 0){
            return;
        }
        for (BrokerCommissionFuturesJobResponse symbol:brokerQueryCommissionFuturesResponseList){
            checkBrokerCommisssionFutures(subUserId,futureTradingAccount,symbol,brokerUserCommisssion);
        }
    }

    private void checkBrokerCommisssionFutures(Long subUserId, Long futureTradingAccount, BrokerCommissionFuturesJobResponse symbol, BrokerUserCommisssion brokerUserCommisssion) {
        if (symbol == null || subUserId == null || brokerUserCommisssion == null){
            return;
        }
        Integer takerAdjustmentBak = symbol.getTakerAdjustment();
        Integer makerAdjustmentBak = symbol.getMakerAdjustment();
        boolean futureCommissChange = false;
        if (symbol.getMakerAdjustment() != null){
            if (symbol.getMakerAdjustment() < brokerUserCommisssion.getMinFuturesMakerCommiss().intValue()){
                symbol.setMakerAdjustment(brokerUserCommisssion.getMinFuturesMakerCommiss().intValue());
                futureCommissChange = true;
            }else if(symbol.getMakerAdjustment() > brokerUserCommisssion.getMaxFuturesMakerCommiss().intValue()){
                symbol.setMakerAdjustment(brokerUserCommisssion.getMaxFuturesMakerCommiss().intValue());
                futureCommissChange = true;
            }
        }else{
            //todo 如果等于null
            symbol.setMakerAdjustment(brokerUserCommisssion.getMinFuturesMakerCommiss().intValue());
            futureCommissChange = true;
        }

        if (symbol.getTakerAdjustment() != null){
            if (symbol.getTakerAdjustment() < brokerUserCommisssion.getMinFuturesTakerCommiss().intValue()){
                symbol.setTakerAdjustment(brokerUserCommisssion.getMinFuturesTakerCommiss().intValue());
                futureCommissChange = true;
            }else if(symbol.getTakerAdjustment() > brokerUserCommisssion.getMaxFuturesTakerCommiss().intValue()){
                symbol.setTakerAdjustment(brokerUserCommisssion.getMaxFuturesTakerCommiss().intValue());
                futureCommissChange = true;
            }
        }else{
            //todo 如果等于null
            symbol.setTakerAdjustment(brokerUserCommisssion.getMinFuturesTakerCommiss().intValue());
            futureCommissChange = true;
        }
        log.info("futureAccountApiClient.checkBrokerCommisssionFutures.subUserId:{},symbol:{},futureCommissChange:{}",subUserId,JsonUtils.toJsonHasNullKey(symbol),futureCommissChange);
        if (futureCommissChange){
            FeeAdjustResponse feeAdjustResponse= futureAccountApiClient.feeAdjust(futureTradingAccount,symbol.getSymbol(),symbol.getMakerAdjustment(),symbol.getTakerAdjustment());
            if (feeAdjustResponse == null){
                log.error("futureAccountApiClient.feeAdjust error,subUserId:{},symbol:{}",subUserId,JsonUtils.toJsonHasNullKey(symbol));
            }
            insertOrupdateBrokerCommissionUpdateBak(subUserId,futureTradingAccount,new BigDecimal(String.valueOf(takerAdjustmentBak)),new BigDecimal(String.valueOf(makerAdjustmentBak)),symbol.getSymbol(),2);
        }
    }

    private void doSetSpotAndMarginCommission(Long subUserId, BrokerUserCommisssion brokerUserCommisssion,Integer source){
        log.info("UpdateBrokerFeeJobHandler.doSetSpotAndMarginCommission.subUserId:{},brokerUserCommisssion:{}",subUserId,JsonUtils.toJsonHasNullKey(brokerUserCommisssion));
        if (subUserId == null || brokerUserCommisssion == null){
            return;
        }
        UserInfo subUserInfo = this.userInfoMapper.selectByPrimaryKey(subUserId);

        if (null != subUserInfo) {
            BigDecimal takerCommissionBak = subUserInfo.getTakerCommission();
            BigDecimal makerCommissionBak = subUserInfo.getMakerCommission();
            if (null != subUserInfo.getTradingAccount()) {
                TradingAccountDetails tradingAccountDetails= accountApi.getDetailsByTradingAccountId(subUserInfo.getTradingAccount());
                log.info("UpdateBrokerFeeJobHandler.getDetailsByTradingAccountId.userId={},tradingAccountDetails={},userInfo={}",subUserId,JsonUtils.toJsonNotNullKey(tradingAccountDetails),
                        JsonUtils.toJsonNotNullKey(subUserInfo));
                boolean changeResult = checkSubUserCommission(subUserInfo, brokerUserCommisssion);
                //如果需要变更
                if(changeResult){
                    accountApi.setCommission(subUserInfo.getTradingAccount(),
                            CouplingCalculationUtils.feeLong(subUserInfo.getBuyerCommission()),
                            CouplingCalculationUtils.feeLong(subUserInfo.getSellerCommission()),
                            CouplingCalculationUtils.feeLong(subUserInfo.getTakerCommission()),
                            CouplingCalculationUtils.feeLong(subUserInfo.getMakerCommission()));
                    // 读写延迟
                    UserInfo updateUserInfo = new UserInfo();
                    updateUserInfo.setUserId(subUserInfo.getUserId());
                    updateUserInfo.setBuyerCommission(subUserInfo.getBuyerCommission());
                    updateUserInfo.setSellerCommission(subUserInfo.getSellerCommission());
                    updateUserInfo.setTakerCommission(subUserInfo.getTakerCommission());
                    updateUserInfo.setMakerCommission(subUserInfo.getMakerCommission());
                    userInfoMapper.updateByPrimaryKeySelective(updateUserInfo);
                    try {
                        // 临时代码
                        Map<String, Object> dataMsg = new HashMap<>();
                        dataMsg.put(UserConst.USER_ID, subUserId);
                        dataMsg.put("buyerCommission", subUserInfo.getBuyerCommission());
                        dataMsg.put("sellerCommission", subUserInfo.getSellerCommission());
                        dataMsg.put("takerCommission", subUserInfo.getTakerCommission());
                        dataMsg.put("makerCommission", subUserInfo.getMakerCommission());
                        dataMsg.put("modifyReason", "updateBrokerFeeJobHandler");
                        dataMsg.put("expectedRestoreTime", null);
                        MsgNotification msg =
                                new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.SET_COMMISSION, dataMsg);
                        log.info("iMsgNotification setCommission:{}", JSON.toJSONString(msg));
                        this.iMsgNotification.send(msg);
                    } catch (Exception e) {
                        log.error("iMsgNotification.send failed:", e);
                    }
                    //保存备份
                    insertOrupdateBrokerCommissionUpdateBak(subUserId,subUserInfo.getTradingAccount(),takerCommissionBak,makerCommissionBak,null,source);
                }
            } else {
                UserIndex userIndex = userIndexMapper.selectByPrimaryKey(subUserId);
                User user = userMapper.queryByEmail(userIndex.getEmail());
                if (BitUtils.isTrue(user.getStatus(), Constant.USER_ACTIVE)) {
                    log.error("UpdateBrokerFeeJobHandler.doSetSpotAndMarginCommission user activated, but tradingAccount is null, userId:{}", subUserId);
                } else {
                    log.warn("UpdateBrokerFeeJobHandler.doSetSpotAndMarginCommission user not activated and tradingAccount is null, userId:{}", subUserId);
                }
            }
        } else {
            log.error("UpdateBrokerFeeJobHandler.userInfo is null, userId:{}", subUserId);
        }

    }

    /**
     *
     * @param subUserId
     * @param takerCommission
     * @param makerCommission
     * @param symbol
     * @param source 0-sportmargin,1-future
     */
    private void insertOrupdateBrokerCommissionUpdateBak(Long subUserId,Long tradeAccountId, BigDecimal takerCommission, BigDecimal makerCommission, String symbol, Integer source) {
        if (subUserId == null){
            return;
        }
        BrokerCommissionUpdateBak bak = new BrokerCommissionUpdateBak();
        bak.setTakerCommiss(takerCommission);
        bak.setMakerCommiss(makerCommission);
        bak.setUserId(subUserId);
        bak.setSource(source);
        bak.setSymbol(symbol);
        bak.setTradingAccount(tradeAccountId);
        brokerCommissionUpdateBakMapper.insertSelective(bak);
    }

    private boolean checkSubUserCommission(UserInfo subUserInfo, BrokerUserCommisssion brokerUserCommisssion) {
        boolean change = false;
        if(subUserInfo != null){
            if (subUserInfo.getTakerCommission() != null){
                if (subUserInfo.getTakerCommission().compareTo(brokerUserCommisssion.getMinTakerCommiss()) < 0){
                    subUserInfo.setTakerCommission(brokerUserCommisssion.getMinTakerCommiss());
                    change = true;
                } else if (subUserInfo.getTakerCommission().compareTo(brokerUserCommisssion.getMaxTakerCommiss()) > 0){
                    subUserInfo.setTakerCommission(brokerUserCommisssion.getMaxTakerCommiss());
                    change = true;
                }
            }
            if (subUserInfo.getMakerCommission() != null){
                if (subUserInfo.getMakerCommission().compareTo(brokerUserCommisssion.getMinMakerCommiss()) < 0){
                    subUserInfo.setMakerCommission(brokerUserCommisssion.getMinMakerCommiss());
                    change = true;
                }else if (subUserInfo.getMakerCommission().compareTo(brokerUserCommisssion.getMaxMakerCommiss()) >0){
                    subUserInfo.setMakerCommission(brokerUserCommisssion.getMaxMakerCommiss());
                    change = true;
                }
            }

        }
        return change;
    }

    private void checkAndset(BrokerUserCommisssion brokerUserCommisssion) {
        if (brokerUserCommisssion.getMinTakerCommiss() == null || brokerUserCommisssion.getMinTakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MIN) < 0){
            brokerUserCommisssion.setMinTakerCommiss(BrokerSubUserAdminService.BROKER_COMMISSION_MIN);
        }
        if (brokerUserCommisssion.getMaxTakerCommiss() == null || brokerUserCommisssion.getMaxTakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MAX) > 0
        || brokerUserCommisssion.getMaxTakerCommiss().compareTo(brokerUserCommisssion.getMinTakerCommiss()) <= 0){
            brokerUserCommisssion.setMaxTakerCommiss(BrokerSubUserAdminService.BROKER_COMMISSION_MAX);
        }
        if (brokerUserCommisssion.getMinMakerCommiss() == null || brokerUserCommisssion.getMinMakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MIN) < 0){
            brokerUserCommisssion.setMinMakerCommiss(BrokerSubUserAdminService.BROKER_COMMISSION_MIN);
        }
        if (brokerUserCommisssion.getMaxMakerCommiss() == null || brokerUserCommisssion.getMaxMakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MAX) > 0
                || brokerUserCommisssion.getMaxMakerCommiss().compareTo(brokerUserCommisssion.getMinMakerCommiss()) <= 0){
            brokerUserCommisssion.setMaxMakerCommiss(BrokerSubUserAdminService.BROKER_COMMISSION_MAX);
        }

        if (brokerUserCommisssion.getMinFuturesTakerCommiss() == null || brokerUserCommisssion.getMinFuturesTakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MIN) < 0){
            brokerUserCommisssion.setMinFuturesTakerCommiss(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MIN);
        }
        if (brokerUserCommisssion.getMaxFuturesTakerCommiss() == null || brokerUserCommisssion.getMaxFuturesTakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MAX_TAKER) > 0
                || brokerUserCommisssion.getMaxFuturesTakerCommiss().compareTo(brokerUserCommisssion.getMinFuturesTakerCommiss()) <= 0){
            brokerUserCommisssion.setMaxFuturesTakerCommiss(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MAX_TAKER);
        }
        if (brokerUserCommisssion.getMinFuturesMakerCommiss() == null || brokerUserCommisssion.getMinFuturesMakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MIN) < 0){
            brokerUserCommisssion.setMinFuturesMakerCommiss(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MIN);
        }
        if (brokerUserCommisssion.getMaxFuturesMakerCommiss() == null || brokerUserCommisssion.getMaxFuturesMakerCommiss().compareTo(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MAX_MAKER) > 0
                || brokerUserCommisssion.getMaxFuturesMakerCommiss().compareTo(brokerUserCommisssion.getMinFuturesMakerCommiss())<=0){
            brokerUserCommisssion.setMaxFuturesMakerCommiss(BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MAX_MAKER);
        }
    }
}
