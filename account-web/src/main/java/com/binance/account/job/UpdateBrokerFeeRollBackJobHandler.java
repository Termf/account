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
import com.google.common.collect.Maps;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by yangyang on 2019/6/17.
 */
@Log4j2
@JobHandler(value = "updateBrokerFeeRollBackJobHandler")
@Component
public class UpdateBrokerFeeRollBackJobHandler extends IJobHandler{

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
        log.info("updateBrokerFeeRollBackJobHandler.execute.start.param:{}",s);
        //自定义个数
        if (StringUtils.isNumeric(s)){
            Map<String,Object> param = Maps.newHashMap();
            param.put("userId",Long.parseLong(s));
            List<BrokerCommissionUpdateBak> brokerCommissionUpdateBaks = brokerCommissionUpdateBakMapper.selectByPage(param);
            doRollBackAll(brokerCommissionUpdateBaks);
            log.info("updateBrokerFeeRollBackJobHandler.execute.end");
            return ReturnT.SUCCESS;
        }
        Long total = brokerCommissionUpdateBakMapper.countAllBak();
        if (total == null || total == 0){
            log.info("updateBrokerFeeRollBackJobHandler.execute.countAllBak.result.total is null");
            return SUCCESS;
        }
        if (total <= 500){
            rollList(0L,total);
            return SUCCESS;
        }
        Long len = total%500==0?total/500:(total/500+1);
        for (int i=0;i<len;i++){
            if ((i+1)*500 <= total){
                rollList(i*500L,500L);
            }else{
                rollList(i*500L,total-i*500+1);
            }
        }
        log.info("updateBrokerFeeRollBackJobHandler.execute.end");
        return ReturnT.SUCCESS;
    }

    private void rollList(Long start,Long offset){
        Map<String,Object> param = Maps.newHashMap();
        param.put("start",start);
        param.put("offset",offset);
        List<BrokerCommissionUpdateBak> brokerCommissionUpdateBaks = brokerCommissionUpdateBakMapper.selectByPage(param);
        log.info("updateBrokerFeeRollBackJobHandler.rollAll.brokerCommissionUpdateBaks:{}",JsonUtils.toJsonHasNullKey(brokerCommissionUpdateBaks));
        if (CollectionUtils.isEmpty(brokerCommissionUpdateBaks)){
            return;
        }
        doRollBackAll(brokerCommissionUpdateBaks);
    }

    private void doRollBackAll(List<BrokerCommissionUpdateBak> brokerCommissionUpdateBaks) {
        for (BrokerCommissionUpdateBak bak:brokerCommissionUpdateBaks){
            log.info("doRollBackAll.bak:{}",JsonUtils.toJsonHasNullKey(bak));
            UserInfo subUserInfo = userInfoMapper.selectByPrimaryKey(bak.getUserId());
            if (subUserInfo == null) {
                return;
            }
            try {
                if (bak.getSource()!= null && bak.getSource() == 2){
                    FeeAdjustResponse feeAdjustResponse= futureAccountApiClient.feeAdjust(bak.getTradingAccount(),bak.getSymbol(),bak.getMakerCommiss().intValue(),bak.getTakerCommiss().intValue());
                    log.info("doRollBackAll.feeAdjust:{},feeAdjustResponse:{}",JsonUtils.toJsonHasNullKey(feeAdjustResponse));
                    if (feeAdjustResponse == null){
                        log.error("updateBrokerFeeRollBackJobHandler.rollAll error,subUserId:{},bak:{}",bak.getUserId(),JsonUtils.toJsonHasNullKey(bak));
                    }
                }else{
                    subUserInfo.setMakerCommission(bak.getMakerCommiss());
                    subUserInfo.setTakerCommission(bak.getTakerCommiss());
                    doBakSpotAndMarginCommission(subUserInfo);
                }
            }catch (Exception e){
                log.error("updateBrokerFeeRollBackJobHandler.doRollBackAll error,bak:{},e:{}",JsonUtils.toJsonHasNullKey(bak),e);
            }


        }
    }


    private void doBakSpotAndMarginCommission(UserInfo subUserInfo){
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
            dataMsg.put(UserConst.USER_ID, subUserInfo.getUserId());
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
    }
}
