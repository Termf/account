package com.binance.account.service.subuser.impl;

import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.data.entity.broker.BrokerCommissionWhite;
import com.binance.account.data.entity.broker.BrokerUserCommisssion;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.broker.BrokerCommissionWhiteMapper;
import com.binance.account.data.mapper.broker.BrokerUserCommisssionMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.message.MsgApiClient;
import com.binance.account.service.subuser.IBrokerSubUserAdminService;
import com.binance.account.service.user.impl.UserInfoBusiness;
import com.binance.account.vo.subuser.BrokerUserCommisssionVo;
import com.binance.account.vo.subuser.request.AddOrUpdateBrokerUserCommissionRequest;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Created by yangyang on 2019/8/21.
 */
@Log4j2
@Service
public class BrokerSubUserAdminService extends CheckSubUserBusiness implements IBrokerSubUserAdminService {

    @Autowired
    private MsgApiClient msgApiClient;

    @Autowired
    private BrokerUserCommisssionMapper brokerUserCommisssionMapper;

    @Autowired
    private UserInfoBusiness userInfoBusiness;

    @Autowired
    private BrokerCommissionWhiteMapper brokerCommissionWhiteMapper;

    public static final BigDecimal BROKER_COMMISSION_MIN = new BigDecimal("0.001");
    public static final BigDecimal BROKER_COMMISSION_MAX = new BigDecimal("0.002");
    public static final BigDecimal BROKER_FUTURE_COMMISSION_MAX_TAKER = new BigDecimal("400");
    public static final BigDecimal BROKER_FUTURE_COMMISSION_MAX_MAKER = new BigDecimal("200");
    public static final BigDecimal BROKER_FUTURE_COMMISSION_MIN = new BigDecimal("0");

    public static final BigDecimal BROKER_DELIVERY_COMMISSION_MAX_TAKER = new BigDecimal("400");
    public static final BigDecimal BROKER_DELIVERY_COMMISSION_MAX_MAKER = new BigDecimal("150");
    public static final BigDecimal BROKER_DELIVERY_COMMISSION_MIN = new BigDecimal("0");


    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
//    @UserPermissionValidate(userId = "#request.body.parentUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_BROKER_PARENT_ACCOUNT)
    public APIResponse<Boolean> enableBrokerSubUserFunction(APIRequest<ParentUserIdReq> request) {
        ParentUserIdReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();
        log.info("enableBrokerSubUserFunction start, parentUserId:{}", parentUserId);

        final User parentUser = checkAndGetUserById(parentUserId);
        if(checkAssetSubUserFunctionEnabled(parentUser.getStatus())){
            throw new BusinessException(AccountErrorCode.NORMAL_PARENT_TO_ASSET_PARENT_IS_VALID);
        }
        // 确保至少一项2FA开打
        assertUser2FaAtLeastOneEnabled(parentUser.getStatus());
        // 确保已经开通母账号功能
        assertSubUserFunctionEnabled(parentUser.getStatus());
        // 确保不是子用户
        assertIsNotSubUser(parentUser);
        // 确保未开通broker母子账户功能
        assertBrokerSubUserFunctionDisabled(parentUser.getStatus());

        // 若存在子账户，设置 isSubUserEnabled = true
        List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(parentUserId);
        for (SubUserBinding subUserBinding : subUserBindings) {
            final Long subUserId = subUserBinding.getSubUserId();
            enableBrokerSubUser(subUserId);
            if(Objects.isNull(subUserBinding.getBrokerSubAccountId())){
                subUserBinding.setBrokerSubAccountId(keyGenerator.generateKey().longValue());
                subUserBindingMapper.updateBySubUserIdSelective(subUserBinding);
            }
        }

        // 更新用户状态
        Long status = parentUser.getStatus();
        status = BitUtils.enable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        status = BitUtils.disable(status, Constant.USER_IS_SUBUSER);
        status = BitUtils.enable(status, Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED);
        status = BitUtils.disable(status, Constant.USER_IS_BROKER_SUBUSER);
        User updateParentUser = new User();
        updateParentUser.setEmail(parentUser.getEmail());
        updateParentUser.setStatus(status);
        int result = userMapper.updateUserStatusByEmail(updateParentUser);
        log.info("enableBrokerSubUserFunction result:{}, parentUserId:{}", result, parentUser.getUserId());
        if (result != 1) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        try {
            //TODO 需要重新
            msgApiClient.sendEnableSubAccountEmail(parentUserId, LanguageEnum.EN_US.getCode(), TerminalEnum.WEB.getCode());
            log.info("sendEnableBrokerSubAccountEmail parentUserId:{}", parentUserId);
        } catch (Exception e) {
            log.error(String.format("sendEnableBrokerSubAccountEmail error :parentUserId:%s",parentUserId), e);
        }
        try {
            int updateResult = userInfoBusiness.updateAgentId(parentUserId, parentUserId,false);
            log.info("updateAgentId end, userid={},updateResult={}",parentUserId,updateResult);
        } catch (Exception e) {
            log.error(String.format("updateAgentId error, userid:%s.", parentUserId), e);
        }

        return APIResponse.getOKJsonResult(true);
    }

    @Monitored
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Boolean> disableBrokerSubUserFunction(APIRequest<ParentUserIdReq> request) throws Exception {

        ParentUserIdReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();
        log.info("disableSubUserFunction start, parentUserId:{}", parentUserId);

        final User parentUser = checkAndGetUserById(parentUserId);
        // 确保已开通母子账号功能
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());

        // 设置 isSubUserEnabled = false
        List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(parentUserId);
        for (SubUserBinding subUserBinding : subUserBindings) {
            final Long subUserId = subUserBinding.getSubUserId();
            rollbackBrokerSubuer(subUserId);
        }

        // 更新用户状态
        User updateParentUser = new User();
        updateParentUser.setEmail(parentUser.getEmail());
        updateParentUser.setStatus(BitUtils.disable(parentUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED));
        int result = userMapper.updateUserStatusByEmail(updateParentUser);
        log.info("disableSubUserFunction result:{}, parentUserId:{}", result, parentUser.getUserId());
        if (result != 1) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }

        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public BrokerUserCommisssionVo getBrokerUserCommission(ParentUserIdReq request) throws Exception {
        Long parentUserId =request.getParentUserId();
        BrokerUserCommisssion brokerUserCommisssion= brokerUserCommisssionMapper.selectByUserId(parentUserId);
        if(null==brokerUserCommisssion){
            return null;
        }
        BrokerUserCommisssionVo vo=new BrokerUserCommisssionVo();
        BeanUtils.copyProperties(brokerUserCommisssion,vo);
        return vo;
    }

    @Override
    public Integer addOrUpdateBrokerUserCommission(AddOrUpdateBrokerUserCommissionRequest request) throws Exception {
        Long userId=request.getUserId();
        BrokerUserCommisssion brokerUserCommisssion= brokerUserCommisssionMapper.selectByUserId(userId);
        if(null==brokerUserCommisssion){
            validInsertOrUpdateBrokerCommission(request,true,null);
            //insert
            BrokerUserCommisssion insertObj=new BrokerUserCommisssion();
            insertObj.setUserId(request.getUserId());
            insertObj.setMaxMakerCommiss(request.getMaxMakerCommiss());
            insertObj.setMinMakerCommiss(request.getMinMakerCommiss());
            insertObj.setMaxTakerCommiss(request.getMaxTakerCommiss());
            insertObj.setMinTakerCommiss(request.getMinTakerCommiss());
            insertObj.setMaxSubAccount(request.getMaxSubAccount());
            insertObj.setDayMaxSubAccount(request.getDayMaxSubAccount());
            insertObj.setMaxFuturesMakerCommiss(request.getMaxFuturesMakerCommiss());
            insertObj.setMinFuturesMakerCommiss(request.getMinFuturesMakerCommiss());
            insertObj.setMaxFuturesTakerCommiss(request.getMaxFuturesTakerCommiss());
            insertObj.setMinFuturesTakerCommiss(request.getMinFuturesTakerCommiss());
            insertObj.setDayWithdrawLimit(request.getDayWithdrawLimit());
            insertObj.setDayWithdrawPer(request.getDayWithdrawPer());
            insertObj.setDayWithdrawSwitch(request.getDayWithdrawSwitch());

            insertObj.setMaxDeliveryMakerCommiss(request.getMaxDeliveryMakerCommiss());
            insertObj.setMinDeliveryMakerCommiss(request.getMinDeliveryMakerCommiss());
            insertObj.setMaxDeliveryTakerCommiss(request.getMaxDeliveryTakerCommiss());
            insertObj.setMinDeliveryTakerCommiss(request.getMinDeliveryTakerCommiss());
            return brokerUserCommisssionMapper.insertSelective(insertObj);
        }
        validInsertOrUpdateBrokerCommission(request,false,brokerUserCommisssion);
        //update
        BrokerUserCommisssion updateObj=new BrokerUserCommisssion();
        updateObj.setUserId(request.getUserId());
        updateObj.setMaxMakerCommiss(request.getMaxMakerCommiss());
        updateObj.setMinMakerCommiss(request.getMinMakerCommiss());
        updateObj.setMaxTakerCommiss(request.getMaxTakerCommiss());
        updateObj.setMinTakerCommiss(request.getMinTakerCommiss());
        updateObj.setMaxSubAccount(request.getMaxSubAccount());
        updateObj.setDayMaxSubAccount(request.getDayMaxSubAccount());
        updateObj.setUpdateTime(DateUtils.getNewUTCDate());
        updateObj.setMaxFuturesMakerCommiss(request.getMaxFuturesMakerCommiss());
        updateObj.setMinFuturesMakerCommiss(request.getMinFuturesMakerCommiss());
        updateObj.setMaxFuturesTakerCommiss(request.getMaxFuturesTakerCommiss());
        updateObj.setMinFuturesTakerCommiss(request.getMinFuturesTakerCommiss());
        updateObj.setDayWithdrawLimit(request.getDayWithdrawLimit());
        updateObj.setDayWithdrawPer(request.getDayWithdrawPer());
        updateObj.setDayWithdrawSwitch(request.getDayWithdrawSwitch());

        updateObj.setMaxDeliveryMakerCommiss(request.getMaxDeliveryMakerCommiss());
        updateObj.setMinDeliveryMakerCommiss(request.getMinDeliveryMakerCommiss());
        updateObj.setMaxDeliveryTakerCommiss(request.getMaxDeliveryTakerCommiss());
        updateObj.setMinDeliveryTakerCommiss(request.getMinDeliveryTakerCommiss());
        return brokerUserCommisssionMapper.updateByUserIdSelective(updateObj);
    }
    //true-insert false-update,insert、update区别在于insert设置默认值+校验，而update只是校验
    private void validInsertOrUpdateBrokerCommission(AddOrUpdateBrokerUserCommissionRequest request, boolean insertOrupdate, BrokerUserCommisssion brokerUserCommisssion) {
        if ((request.getMaxMakerCommiss() != null && request.getMinMakerCommiss() != null && request.getMaxMakerCommiss().compareTo(request.getMinMakerCommiss()) < 0)
        ||(request.getMaxTakerCommiss() != null && request.getMinTakerCommiss() != null && request.getMaxTakerCommiss().compareTo(request.getMinTakerCommiss()) < 0)
        ||(request.getMinFuturesMakerCommiss() != null && request.getMaxFuturesMakerCommiss() != null && request.getMaxFuturesMakerCommiss().compareTo(request.getMinFuturesMakerCommiss())<0)
        ||(request.getMinFuturesTakerCommiss() != null && request.getMaxFuturesTakerCommiss() != null && request.getMaxFuturesTakerCommiss().compareTo(request.getMinFuturesTakerCommiss())<0)
        ||(request.getMinDeliveryTakerCommiss() != null && request.getMaxDeliveryTakerCommiss() != null && request.getMaxDeliveryTakerCommiss().compareTo(request.getMinDeliveryTakerCommiss())<0)
        ||(request.getMinDeliveryMakerCommiss() != null && request.getMaxDeliveryMakerCommiss() != null && request.getMaxDeliveryMakerCommiss().compareTo(request.getMinDeliveryMakerCommiss())<0)){
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_ERROR_SET_ERROR);
        }
        if (request.getDayWithdrawLimit() != null && request.getDayWithdrawLimit() <= 0){
            throw new BusinessException(AccountErrorCode.BROKER_DAY_WITHDRAW_LIMIT_ERROR);
        }
        if (request.getDayWithdrawPer() != null && (request.getDayWithdrawPer().doubleValue() < 0 && request.getDayWithdrawPer().doubleValue() > 1)){
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_ERROR_SET_ERROR);
        }
        if (request.getDayWithdrawSwitch() != null && (request.getDayWithdrawSwitch() != 0 && request.getDayWithdrawSwitch() != 1)){
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_ERROR_SET_ERROR);
        }

        //更新时比较插入值的min与库中已有的max比较
        if (!insertOrupdate){
            if(request.getMaxMakerCommiss() != null){
                brokerUserCommisssion.setMaxMakerCommiss(request.getMaxMakerCommiss());
            }
            if(request.getMinMakerCommiss() != null){
                brokerUserCommisssion.setMinMakerCommiss(request.getMinMakerCommiss());
            }
            if(request.getMaxTakerCommiss() != null){
                brokerUserCommisssion.setMaxTakerCommiss(request.getMaxTakerCommiss());
            }
            if(request.getMinTakerCommiss() != null){
                brokerUserCommisssion.setMinTakerCommiss(request.getMinTakerCommiss());
            }
            if(request.getMaxFuturesMakerCommiss() != null){
                brokerUserCommisssion.setMaxFuturesMakerCommiss(request.getMaxFuturesMakerCommiss());
            }
            if(request.getMinFuturesMakerCommiss() != null){
                brokerUserCommisssion.setMinFuturesMakerCommiss(request.getMinFuturesMakerCommiss());
            }
            if(request.getMaxFuturesTakerCommiss() != null){
                brokerUserCommisssion.setMaxFuturesTakerCommiss(request.getMaxFuturesTakerCommiss());
            }
            if(request.getMinFuturesTakerCommiss() != null){
                brokerUserCommisssion.setMinFuturesTakerCommiss(request.getMinFuturesTakerCommiss());
            }


            if(request.getMaxDeliveryMakerCommiss() != null){
                brokerUserCommisssion.setMaxDeliveryMakerCommiss(request.getMaxDeliveryMakerCommiss());
            }
            if(request.getMinDeliveryMakerCommiss() != null){
                brokerUserCommisssion.setMinDeliveryMakerCommiss(request.getMinDeliveryMakerCommiss());
            }
            if(request.getMaxDeliveryTakerCommiss() != null){
                brokerUserCommisssion.setMaxDeliveryTakerCommiss(request.getMaxDeliveryTakerCommiss());
            }
            if(request.getMinDeliveryTakerCommiss() != null){
                brokerUserCommisssion.setMinDeliveryTakerCommiss(request.getMinDeliveryTakerCommiss());
            }

            //数据库与request比较
            if (brokerUserCommisssion.getMaxMakerCommiss() != null && brokerUserCommisssion.getMinMakerCommiss() != null && brokerUserCommisssion.getMaxMakerCommiss().compareTo(brokerUserCommisssion.getMinMakerCommiss()) < 0){
                throw new BusinessException(AccountErrorCode.BROKER_MAKER_COMMISSION_ERROR);
            }
            if (brokerUserCommisssion.getMaxTakerCommiss() != null && brokerUserCommisssion.getMinTakerCommiss() != null && brokerUserCommisssion.getMaxTakerCommiss().compareTo(brokerUserCommisssion.getMinTakerCommiss()) < 0){
                throw new BusinessException(AccountErrorCode.BROKER_TAKER_COMMISSION_ERROR);
            }
            if (brokerUserCommisssion.getMaxFuturesMakerCommiss() != null && brokerUserCommisssion.getMinFuturesMakerCommiss() != null && brokerUserCommisssion.getMaxFuturesMakerCommiss().compareTo(brokerUserCommisssion.getMinFuturesMakerCommiss()) < 0){
                throw new BusinessException(AccountErrorCode.BROKER_FUTURES_MAKER_COMMISSION_ERROR);
            }
            if (brokerUserCommisssion.getMaxFuturesTakerCommiss() != null && brokerUserCommisssion.getMinFuturesTakerCommiss() != null && brokerUserCommisssion.getMaxFuturesTakerCommiss().compareTo(brokerUserCommisssion.getMinFuturesTakerCommiss()) < 0){
                throw new BusinessException(AccountErrorCode.BROKER_FUTURES_TAKER_COMMISSION_ERROR);
            }

            if (brokerUserCommisssion.getMaxDeliveryMakerCommiss() != null && brokerUserCommisssion.getMinDeliveryMakerCommiss() != null && brokerUserCommisssion.getMaxDeliveryMakerCommiss().compareTo(brokerUserCommisssion.getMinDeliveryMakerCommiss()) < 0){
                throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_MAKER_COMMISSION_ERROR);
            }
            if (brokerUserCommisssion.getMaxDeliveryTakerCommiss() != null && brokerUserCommisssion.getMinDeliveryTakerCommiss() != null && brokerUserCommisssion.getMaxDeliveryTakerCommiss().compareTo(brokerUserCommisssion.getMinDeliveryTakerCommiss()) < 0){
                throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_TAKER_COMMISSION_ERROR);
            }
        }
        //在白名单中不检测
        BrokerCommissionWhite brokerCommissionWhite = brokerCommissionWhiteMapper.selectByUserId(request.getUserId());
        if (brokerCommissionWhite != null){
            return;
        }
        //spot taker
        if (request.getMinTakerCommiss() == null && insertOrupdate){
            request.setMinTakerCommiss(BROKER_COMMISSION_MIN);
        }else if (request.getMinTakerCommiss() != null && (request.getMinTakerCommiss().compareTo(BROKER_COMMISSION_MIN) < 0 || request.getMinTakerCommiss().compareTo(BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_TAKER_COMMISSION_ERROR);
        }

        if (request.getMaxTakerCommiss() == null && insertOrupdate){
            request.setMaxTakerCommiss(BROKER_COMMISSION_MAX);
        }else if (request.getMaxTakerCommiss() != null && (request.getMaxTakerCommiss().compareTo(BROKER_COMMISSION_MIN) < 0 || request.getMaxTakerCommiss().compareTo(BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_TAKER_COMMISSION_ERROR);
        }
        //spot maker
        if (request.getMinMakerCommiss() == null && insertOrupdate){
            request.setMinMakerCommiss(BROKER_COMMISSION_MIN);
        }else if (request.getMinMakerCommiss() != null && (request.getMinMakerCommiss().compareTo(BROKER_COMMISSION_MIN) < 0 || request.getMinMakerCommiss().compareTo(BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_MAKER_COMMISSION_ERROR);
        }

        if (request.getMaxMakerCommiss() == null && insertOrupdate){
            request.setMaxMakerCommiss(BROKER_COMMISSION_MAX);
        }else if (request.getMaxMakerCommiss() != null && (request.getMaxMakerCommiss().compareTo(BROKER_COMMISSION_MIN) < 0 || request.getMaxMakerCommiss().compareTo(BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_MAKER_COMMISSION_ERROR);
        }

        //future taker
        if (request.getMinFuturesTakerCommiss() == null && insertOrupdate){
            request.setMinFuturesTakerCommiss(BROKER_FUTURE_COMMISSION_MIN);
        }else if (request.getMinFuturesTakerCommiss() != null && (request.getMinFuturesTakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MIN) < 0 || request.getMinFuturesTakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MAX_TAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_FUTURES_TAKER_COMMISSION_ERROR);
        }
        if (request.getMaxFuturesTakerCommiss() == null && insertOrupdate){
            request.setMaxFuturesTakerCommiss(BROKER_FUTURE_COMMISSION_MAX_TAKER);
        }else if (request.getMaxFuturesTakerCommiss() != null && (request.getMaxFuturesTakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MIN) < 0 || request.getMaxFuturesTakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MAX_TAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_FUTURES_TAKER_COMMISSION_ERROR);
        }

        //future maker
        if (request.getMinFuturesMakerCommiss() == null && insertOrupdate){
            request.setMinFuturesMakerCommiss(BROKER_FUTURE_COMMISSION_MIN);
        }else if (request.getMinFuturesMakerCommiss() != null && (request.getMinFuturesMakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MIN) < 0 || request.getMinFuturesMakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MAX_MAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_FUTURES_MAKER_COMMISSION_ERROR);
        }
        if (request.getMaxFuturesMakerCommiss() == null && insertOrupdate){
            request.setMaxFuturesMakerCommiss(BROKER_FUTURE_COMMISSION_MAX_MAKER);
        }else if (request.getMaxFuturesMakerCommiss() != null && (request.getMaxFuturesMakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MIN) < 0 || request.getMaxFuturesMakerCommiss().compareTo(BROKER_FUTURE_COMMISSION_MAX_MAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_FUTURES_MAKER_COMMISSION_ERROR);
        }

        //delivery taker
        if (request.getMinDeliveryTakerCommiss() == null && insertOrupdate){
            request.setMinDeliveryTakerCommiss(BROKER_DELIVERY_COMMISSION_MIN);
        }else if (request.getMinDeliveryTakerCommiss() != null && (request.getMinDeliveryTakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MIN) < 0 || request.getMinDeliveryTakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MAX_TAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_TAKER_COMMISSION_ERROR);
        }
        if (request.getMaxDeliveryTakerCommiss() == null && insertOrupdate){
            request.setMaxDeliveryTakerCommiss(BROKER_DELIVERY_COMMISSION_MAX_TAKER);
        }else if (request.getMaxDeliveryTakerCommiss() != null && (request.getMaxDeliveryTakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MIN) < 0 || request.getMaxDeliveryTakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MAX_TAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_TAKER_COMMISSION_ERROR);
        }

        //delivery maker
        if (request.getMinDeliveryMakerCommiss() == null && insertOrupdate){
            request.setMinDeliveryMakerCommiss(BROKER_DELIVERY_COMMISSION_MIN);
        }else if (request.getMinDeliveryMakerCommiss() != null && (request.getMinDeliveryMakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MIN) < 0 || request.getMinDeliveryMakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MAX_MAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_MAKER_COMMISSION_ERROR);
        }
        if (request.getMaxDeliveryMakerCommiss() == null && insertOrupdate){
            request.setMaxDeliveryMakerCommiss(BROKER_DELIVERY_COMMISSION_MAX_MAKER);
        }else if (request.getMaxDeliveryMakerCommiss() != null && (request.getMaxDeliveryMakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MIN) < 0 || request.getMaxDeliveryMakerCommiss().compareTo(BROKER_DELIVERY_COMMISSION_MAX_MAKER) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_MAKER_COMMISSION_ERROR);
        }

    }

}
