package com.binance.account.service.subuser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.integration.message.MsgApiClient;
import com.binance.account.service.datamigration.impl.MsgNotificationToC2CHelper;
import com.binance.account.vo.subuser.request.AssetSubUserToCommonRequest;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import org.apache.commons.collections.CollectionUtils;
import org.javasimon.aop.Monitored;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.service.subuser.ISubUserAdmin;
import com.binance.account.vo.subuser.ParentUserBaseDetailsVo;
import com.binance.account.vo.subuser.SubUserBaseDetailsVo;
import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.SubUserIdReq;
import com.binance.account.vo.subuser.response.ParentUserSubUsersResp;
import com.binance.account.vo.subuser.response.SubUserParentUserResp;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Created by Fei.Huang on 2018/10/11.
 */
@Log4j2
@Service
public class SubUserAdminBusiness extends CheckSubUserBusiness implements ISubUserAdmin {

    @Autowired
    private ExecutorService executor;
    @Autowired
    private MsgApiClient msgApiClient;
    @Autowired
    private MsgNotificationToC2CHelper notificationToC2CHelper;

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
//    @UserPermissionValidate(userId = "#request.body.parentUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_PARENT_ACCOUNT)
    public APIResponse<Boolean> enableSubUserFunction(APIRequest<ParentUserIdReq> request) throws Exception {

        ParentUserIdReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();

        final User parentUser = checkAndGetUserById(parentUserId);

        // 确保至少一项2FA开打
        assertUser2FaAtLeastOneEnabled(parentUser.getStatus());
        // 确保未开通母子账号功能
        assertSubUserFunctionDisabled(parentUser.getStatus());
        // 确保不是子用户
        assertIsNotSubUser(parentUser);
        //不能是特殊账户
        assertIsNotMarginOrFutureOrFiatUser(parentUser);
        //不能是broker母账户
        assertBrokerSubUserFunctionDisabled(parentUser.getStatus());

        // 若存在子账户，设置 isSubUserEnabled = true
        List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(parentUserId);
        for (SubUserBinding subUserBinding : subUserBindings) {
            final Long subUserId = subUserBinding.getSubUserId();
            enableSubUser(subUserId);
        }

        // 更新用户状态
        Long status = parentUser.getStatus();
        status = BitUtils.enable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        status = BitUtils.disable(status, Constant.USER_IS_SUBUSER);
        User updateParentUser = new User();
        updateParentUser.setEmail(parentUser.getEmail());
        updateParentUser.setStatus(status);
        int result = userMapper.updateUserStatusByEmail(updateParentUser);
        log.info("enableSubUserFunction result:{}, parentUserId:{}", result, parentUser.getUserId());
        if (result != 1) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        try {
            msgApiClient.sendEnableSubAccountEmail(parentUserId,LanguageEnum.EN_US.getCode(),TerminalEnum.WEB.getCode());
            log.info("sendEnableSubAccountEmail parentUserId:{}", parentUserId);
        } catch (Exception e) {
            log.error(String.format("sendEnableSubAccountEmail error :parentUserId:%s",parentUserId), e);
        }
        List<Long> subUserIds = subUserBindings.stream().map(SubUserBinding::getSubUserId).collect(Collectors.toList());
        notificationToC2CHelper.sendUserTypeChangesMsgAsync(parentUserId, subUserIds, true);
        return APIResponse.getOKJsonResult(true);
    }

    @Monitored
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Boolean> disableSubUserFunction(APIRequest<ParentUserIdReq> request) throws Exception {

        ParentUserIdReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();

        final User parentUser = checkAndGetUserById(parentUserId);
        // 确保不是子用户
        assertIsNotSubUser(parentUser);
        //不能是特殊账户
        assertIsNotMarginOrFutureOrFiatUser(parentUser);
        //不能是broker母账户
        assertBrokerSubUserFunctionDisabled(parentUser.getStatus());

        // 确保已开通母子账号功能
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 设置 isSubUserEnabled = false
        List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(parentUserId);
        for (SubUserBinding subUserBinding : subUserBindings) {
            final Long subUserId = subUserBinding.getSubUserId();
            //1重置标记位
            disableSubUser(subUserId);
            //2 存入delete表
            createParentSubUserBindingDelete(parentUserId, subUserId, subUserBinding.getRemark(),subUserBinding.getBrokerSubAccountId());
            //3 删除老表
            deleteParentSubUserBinding(parentUserId,subUserId);
            //4 清空user_info中的parent
            int deleteParent = userInfoMapper.deleteParentByUserId(subUserId);
            log.info("deleteParent:parentUserId={},subUserId={},subAccountId={},result={}",parentUserId,subUserBinding,subUserBinding.getBrokerSubAccountId(),deleteParent);
        }

        // 更新用户状态
        User updateParentUser = new User();
        updateParentUser.setEmail(parentUser.getEmail());
        updateParentUser.setStatus(BitUtils.disable(parentUser.getStatus(), Constant.USER_IS_SUBUSER_FUNCTION_ENABLED));
        int result = userMapper.updateUserStatusByEmail(updateParentUser);
        log.info("disableSubUserFunction result:{}, parentUserId:{}", result, parentUser.getUserId());
        if (result != 1) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        
        List<Long> subUserIds = subUserBindings.stream().map(SubUserBinding::getSubUserId).collect(Collectors.toList());
        notificationToC2CHelper.sendUserTypeChangesMsgAsync(parentUserId, subUserIds, false);
        return APIResponse.getOKJsonResult(true);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Boolean> bindParentSubUser(APIRequest<BindingParentSubUserReq> request) throws Exception {

        BindingParentSubUserReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();
        final Long subUserId = requestBody.getSubUserId();

        if (parentUserId.equals(subUserId)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        User parentUser = checkAndGetUserById(parentUserId);
        if (BitUtils.isEnable(parentUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED)) {
            throw new BusinessException("broker账号不允许该操作");    
        }

        // 确保账号状态且未绑定
        Map<Long, User> userMap = assertParentSubUserUnbound(parentUserId, subUserId);
        User subUser = userMap.get(subUserId);

        // 更新子账户状态（三个关键状态）
        Long status = subUser.getStatus();
        // 子账号不能拥有字母账号功能
        status = BitUtils.disable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        // 标记为子账号
        status = BitUtils.enable(status, Constant.USER_IS_SUBUSER);
        // 标记为子账号且被母账号启用
        status = BitUtils.enable(status, Constant.USER_IS_SUB_USER_ENABLED);

        User updatedSubUser = new User();
        updatedSubUser.setEmail(subUser.getEmail());
        updatedSubUser.setStatus(status);
        int result = userMapper.updateUserStatusByEmail(updatedSubUser);
        log.info("updateUserStatusByEmail parentUserId:{}, subUserId:{}, result:{}", parentUserId, subUserId, result);

        // 更新子账号UserInfo
        int result2 = ((SubUserAdminBusiness) AopContext.currentProxy()).updateSubUserInfo(parentUserId, subUserId);
        log.info("updateSubUserInfo parentUserId:{}, subUserId:{}, result:{}", parentUserId, subUserId, result2);

        // 创建母子账号绑定关系
        int result3 = ((SubUserAdminBusiness) AopContext.currentProxy()).createParentSubUserBinding(parentUserId, subUserId, requestBody.getRemark());
        log.info("createParentSubUserBinding parentUserId:{}, subUserId:{}, result:{}", parentUserId, subUserId,
                result3);

        if (result * result2 * result3 != 1) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return APIResponse.getOKJsonResult(true);
    }

    @Monitored
    @Override
    public APIResponse<SubUserParentUserResp> queryBySubUserId(@RequestBody() APIRequest<SubUserIdReq> request)
            throws Exception {

        SubUserIdReq requestBody = request.getBody();
        final Long subUserId = requestBody.getSubUserId();

        User subUser = checkAndGetUserById(subUserId);
        // 确保是子账号
        SubUserBinding userBinding = assertIsSubUser(subUser);

        User parentUser = checkAndGetUserById(userBinding.getParentUserId());
        // 确保母子账号功能开通 后台查询，校验降级
        // assertSubUserFunctionEnabled(parentUser.getStatus());

        SubUserParentUserResp response = SubUserParentUserResp.builder()
                .subUserBaseDetails(getSubUserBaseDetails(subUserId, userBinding.getRemark(),
                        userBinding.getInsertTime(), subUser.getInsertTime(), userBinding.getBrokerSubAccountId()))
                .parentUserBaseDetails(
                        getParentUserBaseDetails(userBinding.getParentUserId(), parentUser.getInsertTime()))
                .build();

        return APIResponse.getOKJsonResult(response);
    }

    @Monitored
    @Override
    public APIResponse<ParentUserSubUsersResp> queryByParentUserId(@RequestBody() APIRequest<ParentUserIdReq> request)
            throws Exception {

        ParentUserIdReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();

        final User parentUser = checkAndGetUserById(parentUserId);
        // 确保母子账号功能开通 后台查询，校验降级
        // assertSubUserFunctionEnabled(parentUser.getStatus());

        ParentUserBaseDetailsVo parentUserBaseDetailsVo =
                getParentUserBaseDetails(parentUserId, parentUser.getInsertTime());

        List<SubUserBaseDetailsVo> subUserBaseDetailsVos = new ArrayList<>();
        List<CompletableFuture<SubUserBaseDetailsVo>> futures = new ArrayList<>();
        List<SubUserBinding> subUserBindings =
                subUserBindingMapper.getSubUserBindingsByParentUserId(parentUser.getUserId());
        for (SubUserBinding subUserBinding : subUserBindings) {
            try {
                User subUser = checkAndGetUserById(subUserBinding.getSubUserId());

                futures.add(CompletableFuture.supplyAsync(() -> getSubUserBaseDetails(subUserBinding.getSubUserId(),
                        subUserBinding.getRemark(), subUserBinding.getInsertTime(), subUser.getInsertTime(),subUserBinding.getBrokerSubAccountId()),
                        executor));

            } catch (Exception e) {
                log.error(String.format("queryByParentUserId parentUserId:%s, subUserId:%s error", parentUserId,
                        subUserBinding.getSubUserId()), e);
            }
        }
        CompletableFuture<List> combinedFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                        .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<SubUserBaseDetailsVo> voList = combinedFuture.get();
        for (SubUserBaseDetailsVo subUserBaseDetailsVo : voList) {
            subUserBaseDetailsVos.add(subUserBaseDetailsVo);
        }

        ParentUserSubUsersResp response = ParentUserSubUsersResp.builder().parentUserId(parentUserId)
                .subUserCount(CollectionUtils.isNotEmpty(subUserBaseDetailsVos) ? subUserBaseDetailsVos.size() : 0)
                .parentUserBaseDetails(parentUserBaseDetailsVo).subUserBaseDetailsList(subUserBaseDetailsVos).build();

        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Boolean> updateSubUserRemark(APIRequest<SubUserIdReq> request) throws Exception {
        SubUserIdReq requestBody = request.getBody();
        final Long subUserId = requestBody.getSubUserId();

        User subUser = checkAndGetUserById(subUserId);
        assertIsSubUser(subUser);

        SubUserBinding updateSubUserBinding = new SubUserBinding();
        updateSubUserBinding.setSubUserId(subUserId);
        updateSubUserBinding.setRemark(requestBody.getRemark());
        subUserBindingMapper.updateBySubUserIdSelective(updateSubUserBinding);

        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<Long> countSubUsersByParentUserId(@RequestBody() APIRequest<ParentUserIdReq> request)
            throws Exception {
        ParentUserIdReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();

        checkAndGetUserById(parentUserId);

        Long count = subUserBindingMapper.countSubUsersByParentUserId(parentUserId);

        return APIResponse.getOKJsonResult(count);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Void> assetSubUserToCommon(APIRequest<AssetSubUserToCommonRequest> request) {
        final AssetSubUserToCommonRequest requestBody = request.getBody();
        Long subUserId = requestBody.getSubUserId();
        log.info("assetSubUserToCommon start, subUserId={}", subUserId);
        // 校验subUser
        User subUser = checkAndGetUserById(subUserId);
        if(!isSubUser(subUser)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        
        // 是否是资管子账号
        if(!checkAssetSubUser(subUser.getStatus())) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserInfo userInfo=userInfoMapper.selectByPrimaryKey(subUserId);
        if(null==userInfo.getParent()){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        // 回退状态
        User updateDO = new User();
        updateDO.setEmail(subUser.getEmail());
        updateDO.setStatus(BitUtils.disable(BitUtils.disable(BitUtils.disable(BitUtils.disable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER),Constant.USER_IS_ASSET_SUB_USER_ENABLED),Constant.USER_IS_SUBUSER),Constant.USER_IS_SUB_USER_ENABLED));
        int updateStatus = userMapper.updateUserStatusByEmail(updateDO);
        
        // 删除母子账号关系
        int deleteBinding = deleteParentSubUserBinding(userInfo.getParent(),subUserId);
        
        // 清空user_info中的parent
        int deleteParent = userInfoMapper.deleteParentByUserId(subUserId);
        log.info("assetSubUserToCommon success subUserId={} updateStatus={} deleteBinding={} deleteParent={}", subUserId, updateStatus, deleteBinding, deleteParent);
        return APIResponse.getOKJsonResult();
    }

}
