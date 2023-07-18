package com.binance.account.service.user.impl;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserEmailChange;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.UserEmailChangeMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.impl.UserSecurityBusiness;
import com.binance.account.service.security.impl.UserSecurityResetHelper;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.IUserEmailChange;
import com.binance.account.utils.EncryptUtil;
import com.binance.account.utils.MapUtil;
import com.binance.account.vo.face.ResetEmailFaceFlowInitResult;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.user.BaseUserEmailChangeVo;
import com.binance.account.vo.user.UserChangeEmailEnum;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.OldEmailCaptchaResponse;
import com.binance.account.vo.user.response.UserEmailChangeInitResponse;
import com.binance.account.vo.user.response.UserEmailChangeResponse;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.Md5Tools;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class UserEmailChangeBusiness implements IUserEmailChange {


    @Resource
    private UserEmailChangeMapper userEmailChangeMapper;

    @Autowired
    private IUserFace iUserFace;

    @Autowired
    private IUser iUser;

    @Autowired
    private ISysConfig iSysConfig;

    @Autowired
    private UserCommonBusiness userCommonBusiness;

    @Resource
    private ApolloCommonConfig apolloCommonConfig;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserSecurityResetHelper userSecurityResetHelper;

    @Resource
    protected UserMapper userMapper;

    @Resource
    private UserSecurityBusiness userSecurityBusiness;


    private final String CHANGE_TOTAL_COUNT = "user_email_change_total_count";

    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    private static final String signWorld = "sharingordlw";


    @Override
    public APIResponse<UserEmailChangeInitResponse> initFlow(Long userId, String oldEmail, Integer availableType) {

        if (apolloCommonConfig.isEmailSuccessCountSwitch()) {
            // 判断申请更换邮箱是否超过3次
            int totalCount = userEmailChangeMapper.countByUserIdAndStatus(userId,
                    Byte.parseByte(UserChangeEmailEnum.PASS.getStatus() + ""));
            if (totalCount >= getIntFromSysConfig(CHANGE_TOTAL_COUNT, 3)) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_SUCCESS_COUNT.getCode());
            }
        }


        APIResponse<UserEmailChangeInitResponse> response = null;

        UserEmailChangeInitResponse data = new UserEmailChangeInitResponse();
        try {
            List<UserEmailChange> emailChangeList = userEmailChangeMapper.findUndoneByUserId(userId);

            String flowId = null;

            if (emailChangeList != null && !emailChangeList.isEmpty()) {
                UserEmailChange userEmailChange = getEmailChange(emailChangeList, availableType);
                if (userEmailChange != null) {
                    if (userEmailChange.getStatus() == Byte.parseByte(UserChangeEmailEnum.INIT.getStatus() + "")) {
                        flowId = userEmailChange.getFlowId();
                    } else {

                        log.info("UserEmailChangeBusiness initFlow not support status is {}, userId is {}", userEmailChange.getStatus(), userId);
                        // 判断流程是否结束
                        String idValue = RedisCacheUtils.get(userEmailChange.getFlowId());

                        if (StringUtils.isNotBlank(idValue)) {
                            UserEmailChangeInitResponse userEmailChangeInitResponse = new UserEmailChangeInitResponse();
                            userEmailChangeInitResponse.setFlowStatus(userEmailChange.getStatus());
                            userEmailChangeInitResponse.setFlowId(EncryptUtil.encryptHex(userEmailChange.getFlowId(), signWorld));
                            if (userEmailChange.getStatus()==UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus()){
                                String sign = Md5Tools.MD5(Md5Tools.MD5(userEmailChange.getId() + signWorld) + userEmailChange.getFlowId());
                                userEmailChangeInitResponse.setSign(sign);
                            }
                            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode(), userEmailChangeInitResponse);
                        } else {
                            cancelStatus(userEmailChange.getFlowId(), userEmailChange);
                        }
                    }
                }
            }

            boolean isOldEmailUsed = availableType == null || availableType == 0;

            boolean is2Fa = availableType == null || availableType == 2;

            Long id = null;
            if (StringUtils.isBlank(flowId)) {
                flowId = userId + "_" + DateUtils.formatter(new Date(), "yyMMddHHmmssSSS");
                // 初始化流程
                UserEmailChange record = new UserEmailChange();
                record.setUserId(userId);
                record.setFlowId(flowId);
                record.setOldEmail(oldEmail);
                record.setAvailableType(availableType == null ? Byte.parseByte("0") : Byte.parseByte(availableType + ""));


                if (isOldEmailUsed || is2Fa) {
                    record.setStatus(Byte.parseByte(UserChangeEmailEnum.FACE_VALID.getStatus() + ""));
                }

                userEmailChangeMapper.insertSelective(record);
                id = record.getId();
            }

            //返回给前端的 flowId 进行加密处理
            data.setFlowId(EncryptUtil.encryptHex(flowId, signWorld));

            // 调用face接口
            try {

                if (isOldEmailUsed || is2Fa) {
                    // 老邮箱可用
                    response = new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode(), data);
                    // 发送邮件
                    // 记录老邮箱link的时间
                    RedisCacheUtils.set(flowId, userId + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);
                    //发验证码开关
                    if (isOldEmailUsed){
                        //发送邮件
                        Long recordId = id == null ? userEmailChangeMapper.findByFlowId(flowId).getId() : id;
                        String sign = Md5Tools.MD5(Md5Tools.MD5(recordId + signWorld) + flowId);
                        userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_old_link", userId, oldEmail, "/v1/public/account/user/email/old/link?requestId=" + flowId + "&sign=" + sign, null);
                    }
                    return response;
                }

                ResetEmailFaceFlowInitResult flowInitResult =
                        (ResetEmailFaceFlowInitResult) iUserFace.initFaceFlowByTransId(EncryptUtil.encryptHex(flowId, signWorld), userId, FaceTransType.RESET_EMAIL, false, false);
                data.setType(flowInitResult.getType());
                switch (flowInitResult.getNextStep()) {
                    case KYC:
                        response = new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_CHANGE_NEED_KYC.getCode(), data);
                        break;
                    case FACE:
                        response = new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_CHANGE_NEED_FACE.getCode(), data);
                        break;
                }

            } catch (Exception e) {
                log.error("UserEmailChangeBusiness initFaceFlowByTransId error is {}", e);
                response = new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_CHANGE_FACE_TIMEOUT.getCode(), data);
            }

        } catch (Exception e) {
            log.error("UserEmailChangeBusiness initFlow error is {}", e);
            response = new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode(), data);
        }

        return response;
    }

    private UserEmailChange getEmailChange(List<UserEmailChange> emailChangeList, Integer availableType) {
        if (availableType == null) {
            availableType = 0;
        }

        UserEmailChange emailChange = null;
        for (UserEmailChange change : emailChangeList) {
            // 对之前兼容的数据进行
            if (change.getAvailableType() != Byte.parseByte(availableType + "")) {
                //
                cancelStatus(change.getFlowId(), change);
            } else {
                emailChange = change;
            }
        }

        return emailChange;
    }

    @Override
    public void updateStatus(String flowId) throws Exception {
        // 根据flowId 查询
        UserEmailChange emailChange = userEmailChangeMapper.findByFlowId(flowId);
        // 更新状态
        UserEmailChange userEmailChange = new UserEmailChange();
        userEmailChange.setFlowId(flowId);
        userEmailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.FACE_VALID.getStatus() + ""));
        userEmailChange.setUpdatedAt(new Date());
        userEmailChangeMapper.updateUserEmailChangeByFlowId(userEmailChange);

        if (emailChange != null && emailChange.getUserId() != null && StringUtils.isNotBlank(emailChange.getOldEmail())) {
            // 记录老邮箱link的时间
            RedisCacheUtils.set(flowId, emailChange.getUserId() + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);
            // 发送邮件
            userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_old_link", emailChange.getUserId(), emailChange.getOldEmail(),
                    "/v1/public/account/user/email/old/link?requestId=" + flowId, null);
        }
    }

    @Override
    public APIResponse<Void> linkOldEmail(String flowId, Long userId) {
        try {
            // 先判断链接是否生效
            String userIdValue = RedisCacheUtils.get(flowId);
            log.info("UserEmailChangeBusiness linkOldEmail flowId is {},userId is {},redis values is {}", flowId, userId, userIdValue);

            // 判断流程是否正确流转到该状态
            UserEmailChange userEmailChange = userEmailChangeMapper.findByFlowId(flowId);
            if (StringUtils.isBlank(userIdValue)) {
                cancelStatus(flowId, userEmailChange);
                rejectEmail(flowId, userEmailChange.getOldEmail(), userEmailChange.getUserId(),
                        "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }

            // 流程的userID与登陆的userID不相符,跳转到登陆页
            if (!userIdValue.trim().equals(String.valueOf(userId))) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
            }


            if (userEmailChange.getStatus() != 1) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }

            // 判断申请更换邮箱是否超过3次
            int totalCount = userEmailChangeMapper.countByUserIdAndStatus(userEmailChange.getUserId(),
                    Byte.parseByte(UserChangeEmailEnum.PASS.getStatus() + ""));
            if (totalCount >= getIntFromSysConfig(CHANGE_TOTAL_COUNT, 3)) {
                cancelStatus(flowId, userEmailChange);
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_VALID.getCode());
            }

            // 更新状态
            UserEmailChange change = new UserEmailChange();
            change.setFlowId(flowId);
            change.setStatus(Byte.parseByte(UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus() + ""));
            change.setUpdatedAt(new Date());
            userEmailChangeMapper.updateUserEmailChangeByFlowId(change);
        } catch (Exception e) {
            log.error("link old error is {},flowId is {}", e, flowId);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
        }

        return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode());
    }

    @Override
    public APIResponse<Void> linkOldEmailV2(String flowId, Long userId, String sign) {
        try {
            // 先判断链接是否生效
            String userIdValue = RedisCacheUtils.get(flowId);
            log.info("UserEmailChangeBusiness linkOldEmail flowId is {},userId is {},redis values is {}", flowId, userId, userIdValue);


            // 判断流程是否正确流转到该状态
            UserEmailChange userEmailChange = userEmailChangeMapper.findByFlowId(flowId);
            if (StringUtils.isBlank(userIdValue)) {
                cancelStatus(flowId, userEmailChange);
                rejectEmail(flowId, userEmailChange.getOldEmail(), userEmailChange.getUserId(),
                        "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }

            // 流程的userID与登陆的userID不相符,跳转到登陆页
            if (!userIdValue.trim().equals(String.valueOf(userId))) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
            }


            if (userEmailChange.getStatus() != 1) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }

            //判断是否修改了参数的签名
            String signParam = Md5Tools.MD5(Md5Tools.MD5(userEmailChange.getId() + signWorld) + flowId);

            if (!signParam.equals(sign)) {
                //如果不等于
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }

            // 判断申请更换邮箱是否超过3次
            int totalCount = userEmailChangeMapper.countByUserIdAndStatus(userEmailChange.getUserId(),
                    Byte.parseByte(UserChangeEmailEnum.PASS.getStatus() + ""));
            if (totalCount >= getIntFromSysConfig(CHANGE_TOTAL_COUNT, 3)) {
                cancelStatus(flowId, userEmailChange);
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_VALID.getCode());
            }

            // 更新状态
            UserEmailChange change = new UserEmailChange();
            change.setFlowId(flowId);
            change.setStatus(Byte.parseByte(UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus() + ""));
            change.setUpdatedAt(new Date());
            userEmailChangeMapper.updateUserEmailChangeByFlowId(change);
        } catch (Exception e) {
            log.error("link old error is {},flowId is {}", e, flowId);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
        }

        return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode());
    }

    @Override
    public APIResponse<OldEmailCaptchaResponse> validOldEmailCaptcha(APIRequest<OldEmailCaptchaRequest> request) {
        OldEmailCaptchaRequest oldEmailCaptchaRequest = request.getBody();

        String emailCaptcha = oldEmailCaptchaRequest.getEmailVerifyCode();
        String flowId = oldEmailCaptchaRequest.getFlowId();
        Long userId = oldEmailCaptchaRequest.getUserId();

        try {
            if (EncryptUtil.isHexNumber(flowId)) {
                flowId = EncryptUtil.decryptHex(flowId, signWorld);
            }
        }catch (Exception e){
            log.error("findByFlowIdAndUid decryptHex error ,flowId is {},error ",flowId,e);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
        }

        //验证邮箱验证码是否正确
        try {
            userSecurityBusiness.isEmailAuthenticator(userId, emailCaptcha, true);
        } catch (BusinessException e) {
            log.warn("validOldEmailCaptcha business error ,flowId is {} ,error is ", flowId, e);
            return new APIResponse<>(APIResponse.Status.OK, e.getErrorCode().getCode());
        }

        try {
            // 先判断链接是否生效
            String userIdValue = RedisCacheUtils.get(flowId);
            log.info("validOldEmailCaptcha  flowId is {},userId is {},redis values is {}", flowId, userId, userIdValue);


            // 判断流程是否正确流转到该状态
            UserEmailChange userEmailChange = userEmailChangeMapper.findByFlowId(flowId);
            if (StringUtils.isBlank(userIdValue)) {
                cancelStatus(flowId, userEmailChange);
                rejectEmail(flowId, userEmailChange.getOldEmail(), userEmailChange.getUserId(),
                        "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }

            // 流程的userID与登陆的userID不相符,跳转到登陆页
            if (!userIdValue.trim().equals(String.valueOf(userId))) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
            }


            if (userEmailChange.getStatus() != 1) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }
            // 更新状态
            UserEmailChange change = new UserEmailChange();
            change.setFlowId(flowId);
            change.setStatus(Byte.parseByte(UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus() + ""));
            change.setUpdatedAt(new Date());
            userEmailChangeMapper.updateUserEmailChangeByFlowId(change);

            Long recordId = userEmailChange.getId();
            String sign = Md5Tools.MD5(Md5Tools.MD5(recordId + signWorld) + flowId);

            OldEmailCaptchaResponse oldEmailCaptchaResponse = new OldEmailCaptchaResponse();
            oldEmailCaptchaResponse.setSign(sign);
            return APIResponse.getOKJsonResult(oldEmailCaptchaResponse);
        } catch (Exception e) {
            log.error("link old error is {},flowId is {}", e, flowId);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
        }
    }

    @Override
    public APIResponse<String> confirmNewEmail(String flowId, String newEmail, String pwd) {
        try {

            String newFlowId = flowId;
            if (EncryptUtil.isHexNumber(flowId)) {
                newFlowId = EncryptUtil.decryptHex(flowId, signWorld);
            }

            UserEmailChange change = userEmailChangeMapper.findByFlowId(newFlowId);
            if (change == null || StringUtils.isNotBlank(change.getNewEmail())) {
                // 流程不对，不支持该操作
                cancelStatus(flowId, change);
                rejectEmail(flowId, change.getOldEmail(), change.getUserId(),
                        "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode(), flowId);
            }

            boolean flag = (change.getAvailableType() == 0 && change.getStatus() == UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus())
                    || (change.getAvailableType() == 1 && change.getStatus() == UserChangeEmailEnum.FACE_VALID.getStatus());
            if (flag) {
                // 检验新邮箱是否注册过
                APIRequest<GetUserRequest> request = new APIRequest<>();
                GetUserRequest getUserRequest = new GetUserRequest();
                getUserRequest.setEmail(newEmail);
                request.setBody(getUserRequest);

                final User user = this.userMapper.queryByEmail(newEmail);
                // 邮箱已经注册过
                if (user != null) {
                    return new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_CHANGE_ALREADY_REG.getCode(), flowId);
                }

                // 更新email
                UserEmailChange userEmailChange = new UserEmailChange();
                userEmailChange.setFlowId(flowId);
                userEmailChange.setNewEmail(newEmail);
                userEmailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.NEW_EMAIL_VALID.getStatus() + ""));
                userEmailChange.setUpdatedAt(new Date());
                userEmailChange.setPwd(pwd);
                userEmailChangeMapper.updateUserEmailChangeByFlowId(userEmailChange);

                // 记录邮箱link的时间
                RedisCacheUtils.set(newFlowId, change.getUserId() + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);

                // 发送新邮箱link的链接
                // 发送邮件
                String sign = Md5Tools.MD5(Md5Tools.MD5(signWorld) + newFlowId);

                userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_new_link", change.getUserId(), newEmail,
                        "/v1/public/account/user/email/new/link?requestId=" + newFlowId + "&sign=" + sign, null);

                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode());
            } else {
                // 流程不对，不支持该操作
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode(), "");
            }
        } catch (Exception e) {
            log.error("UserEmailChangeBusiness confirmNewEmail error is {},flowId is {}", e, flowId);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode(), "");
        }

    }

    @Override
    public APIResponse<String> confirmNewEmailV2(String flowId, Long userId, String newEmail, String pwd) {
        String newFlowId = null;
        try {
            newFlowId = flowId;
            if (EncryptUtil.isHexNumber(flowId)) {
                newFlowId = EncryptUtil.decryptHex(flowId, signWorld);
            }

            UserEmailChange change = userEmailChangeMapper.findByFlowId(newFlowId);
            if (change == null || StringUtils.isNotBlank(change.getNewEmail())) {
                // 流程不对，不支持该操作
                cancelStatus(newFlowId, change);
                rejectEmail(newFlowId, change.getOldEmail(), change.getUserId(),
                        "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode());
            }

            // 判断链接是否生效
            String userIdValue = RedisCacheUtils.get(newFlowId);


            if (StringUtils.isBlank(userIdValue)) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode());
            }


            // 流程的userID与登陆的userID不相符,跳转到登陆页
            if (!userIdValue.trim().equals(String.valueOf(userId))) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
            }

            boolean flag = (change.getAvailableType() == 0 && change.getStatus() == UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus())
                    || (change.getAvailableType() == 1 && change.getStatus() == UserChangeEmailEnum.FACE_VALID.getStatus());
            if (flag) {
                // 检验新邮箱是否注册过
                APIRequest<GetUserRequest> request = new APIRequest<>();
                GetUserRequest getUserRequest = new GetUserRequest();
                getUserRequest.setEmail(newEmail);
                request.setBody(getUserRequest);

                final User user = this.userMapper.queryByEmail(newEmail);
                // 邮箱已经注册过
                if (user != null) {
                    return new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_CHANGE_ALREADY_REG.getCode(), newFlowId);
                }

                // 更新email
                UserEmailChange userEmailChange = new UserEmailChange();
                userEmailChange.setFlowId(newFlowId);
                userEmailChange.setNewEmail(newEmail);
                userEmailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.NEW_EMAIL_VALID.getStatus() + ""));
                userEmailChange.setUpdatedAt(new Date());
                userEmailChange.setPwd(pwd);
                userEmailChangeMapper.updateUserEmailChangeByFlowId(userEmailChange);

                // 记录邮箱link的时间
                RedisCacheUtils.set(newFlowId, change.getUserId() + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);

                // 发送新邮箱link的链接
                // 发送邮件
                String sign = Md5Tools.MD5(Md5Tools.MD5(signWorld) + newFlowId);

                //发送验证码
                    userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_new_link", change.getUserId(), newEmail,
                            "/v1/public/account/user/email/new/link?requestId=" + newFlowId + "&sign=" + sign, null);

                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode());
            } else {
                // 流程不对，不支持该操作
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode(), "");
            }
        } catch (Exception e) {
            log.error("UserEmailChangeBusiness confirmNewEmail error is {},flowId is {}", e, newFlowId);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode(), "");
        }

    }

    @Override
    public APIResponse<String> confirmNewEmailV2(APIRequest<UserEmailChangeConfirmNewEmailRequest> newEmailConfirmRequestAPIRequest) {

        UserEmailChangeConfirmNewEmailRequest confirmRequest = newEmailConfirmRequestAPIRequest.getBody();

        String flowId = confirmRequest.getFlowId();
        Long userId = confirmRequest.getUserId();
        String newEmail = confirmRequest.getEmail();
        String pwd = confirmRequest.getPwd();
        String newSafePwd = confirmRequest.getNewSafePwd();


        String newFlowId = null;
        try {
            newFlowId = flowId;
            if (EncryptUtil.isHexNumber(flowId)) {
                newFlowId = EncryptUtil.decryptHex(flowId, signWorld);
            }

            UserEmailChange change = userEmailChangeMapper.findByFlowId(newFlowId);
            if (change == null || StringUtils.isNotBlank(change.getNewEmail())) {
                // 流程不对，不支持该操作
                cancelStatus(newFlowId, change);
                rejectEmail(newFlowId, change.getOldEmail(), change.getUserId(),
                        "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode());
            }

            // 判断链接是否生效
            String userIdValue = RedisCacheUtils.get(newFlowId);


            if (StringUtils.isBlank(userIdValue)) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode());
            }


            // 流程的userID与登陆的userID不相符,跳转到登陆页
            if (!userIdValue.trim().equals(String.valueOf(userId))) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
            }

            boolean flag = (change.getAvailableType() == 0 && change.getStatus() == UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus())
                    || (change.getAvailableType() == 1 && change.getStatus() == UserChangeEmailEnum.FACE_VALID.getStatus());
            if (flag) {
                // 检验新邮箱是否注册过
                APIRequest<GetUserRequest> request = new APIRequest<>();
                GetUserRequest getUserRequest = new GetUserRequest();
                getUserRequest.setEmail(newEmail);
                request.setBody(getUserRequest);

                final User user = this.userMapper.queryByEmail(newEmail);
                // 邮箱已经注册过
                if (user != null) {
                    return new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_CHANGE_ALREADY_REG.getCode(), newFlowId);
                }

                // 更新email
                UserEmailChange userEmailChange = new UserEmailChange();
                userEmailChange.setFlowId(newFlowId);
                userEmailChange.setNewEmail(newEmail);
                userEmailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.NEW_EMAIL_VALID.getStatus() + ""));
                userEmailChange.setUpdatedAt(new Date());
                userEmailChange.setPwd(pwd);
                userEmailChange.setNewSafePwd(newSafePwd);
                userEmailChangeMapper.updateUserEmailChangeByFlowId(userEmailChange);

                // 记录邮箱link的时间
                RedisCacheUtils.set(newFlowId, change.getUserId() + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);

                // 发送新邮箱link的链接
                // 发送邮件
                String sign = Md5Tools.MD5(Md5Tools.MD5(signWorld) + newFlowId);

                //发送验证码
                userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_new_link", change.getUserId(), newEmail,
                        "/v1/public/account/user/email/new/link?requestId=" + newFlowId + "&sign=" + sign, null);

                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode());
            } else {
                // 流程不对，不支持该操作
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode(), "");
            }
        } catch (Exception e) {
            log.error("UserEmailChangeBusiness confirmNewEmail error is {},flowId is {}", e, newFlowId);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode(), "");
        }

    }

    @Override
    public APIResponse<String> confirmNewEmailV3(APIRequest<NewEmailConfirmRequest> newEmailConfirmRequestAPIRequest) {
        NewEmailConfirmRequest newEmailConfirmRequest = newEmailConfirmRequestAPIRequest.getBody();
        String flowId = newEmailConfirmRequest.getFlowId();
        Long userId = newEmailConfirmRequest.getUserId();
        String newEmail = newEmailConfirmRequest.getEmail();
        String pwd = newEmailConfirmRequest.getPwd();
        String sign = newEmailConfirmRequest.getSign();
        String newSafePwd = newEmailConfirmRequest.getNewSafePwd();
        try {
                if (EncryptUtil.isHexNumber(flowId)) {
                    flowId = EncryptUtil.decryptHex(flowId, signWorld);
                }
            UserEmailChange change = userEmailChangeMapper.findByFlowId(flowId);
            if (change == null || StringUtils.isNotBlank(change.getNewEmail())) {
                // 流程不对，不支持该操作
                cancelStatus(flowId, change);
                rejectEmail(flowId, change.getOldEmail(), change.getUserId(),
                        "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode());
            }

            //判断是否修改了参数的签名
            String signParam = Md5Tools.MD5(Md5Tools.MD5(change.getId() + signWorld) + flowId);

            if (!signParam.equals(sign)) {
                //如果不等于
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
            }

            // 判断链接是否生效
            String userIdValue = RedisCacheUtils.get(flowId);


            if (StringUtils.isBlank(userIdValue)) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode());
            }


            // 流程的userID与登陆的userID不相符,跳转到登陆页
            if (!userIdValue.trim().equals(String.valueOf(userId))) {
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
            }

            boolean flag = ( (change.getAvailableType() == 0 || change.getAvailableType() == 2) && change.getStatus() == UserChangeEmailEnum.OLD_EMAIL_VALID.getStatus())
                    || (change.getAvailableType() == 1 && change.getStatus() == UserChangeEmailEnum.FACE_VALID.getStatus());
            if (flag) {
                // 检验新邮箱是否注册过
                APIRequest<GetUserRequest> request = new APIRequest<>();
                GetUserRequest getUserRequest = new GetUserRequest();
                getUserRequest.setEmail(newEmail);
                request.setBody(getUserRequest);

                final User user = this.userMapper.queryByEmail(newEmail);
                // 邮箱已经注册过
                if (user != null) {
                    long emailUsedCount =
                            Long.valueOf(String.valueOf(RedisCacheUtils.get(userId.toString(), Long.class, AccountConstants.CONFIRMNEWEMAIL_EMAIL_USED_COUNT, 0L)));
                    log.info("emailUsedCount={}",emailUsedCount);
                    if(emailUsedCount>2){
                        try {
                            RedisCacheUtils.increment(userId.toString(), AccountConstants.CONFIRMNEWEMAIL_EMAIL_USED_COUNT, 1L, 24L, TimeUnit.HOURS);// 有效期
                        } catch (Exception e) {
                            log.error("emailUsedCount userid限制", e);
                        }
                        log.info("emailUsedCount  overlimit");
                        throw new BusinessException(GeneralCode.SYS_ERROR);
                    }else{
                        try {
                            RedisCacheUtils.increment(userId.toString(), AccountConstants.CONFIRMNEWEMAIL_EMAIL_USED_COUNT, 1L, 24L, TimeUnit.HOURS);// 有效期
                        } catch (Exception e) {
                            log.error("emailUsedCount userid限制", e);
                        }
                        return new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_EMAIL_CHANGE_ALREADY_REG.getCode(), flowId);
                    }
                }

                // 更新email
                UserEmailChange userEmailChange = new UserEmailChange();
                userEmailChange.setFlowId(flowId);
                userEmailChange.setNewEmail(newEmail);
                userEmailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.NEW_EMAIL_VALID.getStatus() + ""));
                userEmailChange.setUpdatedAt(new Date());
                userEmailChange.setPwd(pwd);
                userEmailChange.setNewSafePwd(newSafePwd);
                userEmailChangeMapper.updateUserEmailChangeByFlowId(userEmailChange);

                // 记录邮箱link的时间
                RedisCacheUtils.set(flowId, change.getUserId() + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode(),EncryptUtil.encryptHex(flowId, signWorld));
            } else {
                // 流程不对，不支持该操作
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_SUPPORT.getCode(), "");
            }
        } catch (Exception e) {
            log.error("UserEmailChangeBusiness confirmNewEmail error is {},flowId is {}", e, flowId);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode(), "");
        }
    }

    private void rejectEmail(String flowId, String email, Long userId, String enRejectInfo, String zhRejectInfo) {
        try {
            Map<String, String> rejectMap = new HashMap<>();

            rejectMap.put(LanguageEnum.EN_US.getLang(), enRejectInfo);
            rejectMap.put(LanguageEnum.ZH_CN.getLang(), zhRejectInfo);

            userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_reject", userId, email,
                    "/v1/public/account/user/email/new/link?requestId=" + flowId, rejectMap);
        } catch (Exception e) {
            log.error("reject email error is {},flowId is {}", e, flowId);
        }
    }

    private APIResponse<UserEmailChangeInitResponse> review(String flowId, Long userId) {
        // 审核中
        UserEmailChange emailChange = new UserEmailChange();
        emailChange.setFlowId(flowId);
        emailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.REVIEW.getStatus() + ""));
        emailChange.setUpdatedAt(new Date());
        userEmailChangeMapper.updateUserEmailChangeByFlowId(emailChange);

        // iUserFace.endTransFaceLogStatus(userId, flowId, FaceTransType.RESET_EMAIL, TransFaceLogStatus.REVIEW, "review");
        return new APIResponse<>(APIResponse.Status.OK, GeneralCode.USER_CERTIFICATE_AUDIT.getCode());
    }

    @Override
    public APIResponse<UserEmailChangeInitResponse> linkNewEmail(String flowId, Long userId) {

        UserEmailChangeInitResponse userEmailChangeInitResponse = new UserEmailChangeInitResponse();
        // 判断链接是否生效
        String userIdValue = RedisCacheUtils.get(flowId);
        // 判断审核状态
        UserEmailChange userEmailChange = userEmailChangeMapper.findByFlowId(flowId);
        log.info("UserEmailChangeBusiness linkNewEmail flowId is {},userId is {},redis values is {}", flowId, userId, userIdValue);

        if (userEmailChange == null || StringUtils.isBlank(userIdValue)) {

            cancelStatus(flowId, userEmailChange);
            rejectEmail(flowId, userEmailChange.getOldEmail(), userEmailChange.getUserId(),
                    "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
        }

        // 流程的userID与登陆的userID不相符,跳转到登陆页
        if (!userIdValue.trim().equals(String.valueOf(userId))) {
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
        }


        if (apolloCommonConfig.getChangeEmailReviewSwitch()) {
            // 审核中
            return review(flowId, userId);
        } else {
            // 用户的备注信息
            List<Long> userIds = new ArrayList<>();
            userIds.add(userId);
            List<UserInfo> userInfos = userInfoMapper.selectUserInfoList(userIds);
            if (userInfos != null && !userInfos.isEmpty() && userInfos.get(0) != null && StringUtils.isNotBlank(userInfos.get(0).getRemark())) {
                // 审核中
                return review(flowId, userId);
            }
            // 提现备注信息
            final Map<Long, String> modifyCauseMap = userSecurityResetHelper.oldWithdrawDailyLimitModifyCause(userIds);

            if (modifyCauseMap != null && StringUtils.isNotBlank(modifyCauseMap.get(userId))) {
                // 审核中
                return review(flowId, userId);
            }
        }

        // 如果是拒绝状态，给老邮箱发送拒绝状态
        if (userEmailChange.getStatus() == UserChangeEmailEnum.REFUSE.getStatus()) {
            // 发送拒绝邮件
            iUserFace.endTransFaceLogStatus(userId, flowId, FaceTransType.RESET_EMAIL, TransFaceLogStatus.FAIL, "reject");
            rejectEmail(flowId, userEmailChange.getOldEmail(), userId, "Your account does not meet the requirements for changing  email address.",
                    "您的账户不符合更换邮箱的要求");
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
        }

        if (userEmailChange.getStatus() == UserChangeEmailEnum.NEW_EMAIL_VALID.getStatus()) {
            // 更新状态为通过
            iUserFace.endTransFaceLogStatus(userId, flowId, FaceTransType.RESET_EMAIL, TransFaceLogStatus.PASSED, "passed");

            UserEmailChange updateUserEmailChange = new UserEmailChange();
            updateUserEmailChange.setFlowId(flowId);
            updateUserEmailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.PASS.getStatus() + ""));
            updateUserEmailChange.setUpdatedAt(new Date());
            updateUserEmailChange.setPwd("");
            updateUserEmailChange.setNewSafePwd("");
            userEmailChangeMapper.updateUserEmailChangeByFlowId(updateUserEmailChange);

            try {
                // 调用接口更新email
                APIRequest<ModifyUserEmailRequest> apiRequest = getModifyUserEmailRequestAPIRequest(userEmailChange);

                APIResponse<Integer> response = iUser.modifyUserEmail(apiRequest);
                if (response != null && response.getData() == 1) {
                    try {
                        // 给新邮箱和老邮箱发送更换成功邮件
                        userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_old_pass", userId, userEmailChange.getOldEmail(), null,
                                null);
                        userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_new_pass", userId, userEmailChange.getNewEmail(), null,
                                null);
                    } catch (Exception e) {
                        log.warn("UserEmailChangeBusiness linkNewEmail send success error is {},userId is {}", e, userId);
                    }

                    userEmailChangeInitResponse.setFlowStatus(UserChangeEmailEnum.PASS.getStatus());
                    return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode(), userEmailChangeInitResponse);
                }
            } catch (Exception e) {
                log.info("UserEmailChangeBusiness linkNewEmail modifyUserEmail email error is {},flowId is {},userId is {}", e, flowId, userId);

                iUserFace.endTransFaceLogStatus(userId, flowId, FaceTransType.RESET_EMAIL, TransFaceLogStatus.FAIL, "reject");


                UserEmailChange emailChange = new UserEmailChange();
                emailChange.setFlowId(flowId);
                emailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.REFUSE.getStatus() + ""));
                emailChange.setUpdatedAt(new Date());
                userEmailChangeMapper.updateUserEmailChangeByFlowId(emailChange);

                rejectEmail(flowId, userEmailChange.getOldEmail(), userId, "Your account does not meet the requirements for changing  email address.",
                        "您的账户不符合更换邮箱的要求");
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());

            }

        }

        return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
    }

    @Override
    public APIResponse<UserEmailChangeInitResponse> linkNewEmailV2(String flowId, Long userId, String sign) {
        //判断是否修改了参数的签名
        String signParam = Md5Tools.MD5(Md5Tools.MD5(signWorld) + flowId);
        if (!signParam.equals(sign)) {
            //如果不等于
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.AC_RESET_EMAIL_EXPIRED.getCode());
        }

        return linkNewEmail(flowId, userId);
    }

    @Override
    public APIResponse<UserEmailChangeInitResponse> validNewEmailCaptcha(APIRequest<NewEmailCaptchaRequest> request) {
        NewEmailCaptchaRequest newEmailCaptchaRequest = request.getBody();
        String emailCaptcha = newEmailCaptchaRequest.getEmailVerifyCode();
        String googleCaptcha = newEmailCaptchaRequest.getGoogleVerifyCode();
        String phoneCaptcha = newEmailCaptchaRequest.getSmsVerifyCode();
        String flowId = newEmailCaptchaRequest.getFlowId();
        Long userId = newEmailCaptchaRequest.getUserId();

        try {
            if (EncryptUtil.isHexNumber(flowId)) {
                flowId = EncryptUtil.decryptHex(flowId, signWorld);
            }
        }catch (Exception e){
            log.error("findByFlowIdAndUid decryptHex error ,flowId is {},error ",flowId,e);
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
        }
            //如果验证码错误 API_ENABLEAPIWITHDRAW_NOT_EXIST
            try {
                MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                        .userId(userId)
                        .bizScene(BizSceneEnum.NEW_EMAIL_VERIFY)
                        .emailVerifyCode(emailCaptcha)
                        .googleVerifyCode(googleCaptcha)
                        .mobileVerifyCode(phoneCaptcha)
                        .build();
                userSecurityBusiness.verifyMultiFactors(verify);
            } catch (BusinessException e) {
                log.warn("validNewEmailCaptcha business error , flowId is {},error is ", flowId, e);
                return new APIResponse<>(APIResponse.Status.OK, e.getErrorCode().getCode());

            } catch (Exception e) {
                log.error("validNewEmailCaptcha error , flowId is {},error is ", flowId, e);
                return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_ERROR.getCode());
            }
        return linkNewEmail(flowId, userId);
    }

    private void cancelStatus(String flowId, UserEmailChange userEmailChange) {
        if (userEmailChange.getStatus() < 5) {
            UserEmailChange record = new UserEmailChange();
            record.setFlowId(flowId);
            record.setStatus(Byte.parseByte(UserChangeEmailEnum.CANCEL.getStatus() + ""));
            record.setUpdatedAt(new Date());
            userEmailChangeMapper.updateUserEmailChangeByFlowId(record);
        }
    }

    @Override
    public APIResponse<Void> resendEmail(Long userId, String flowId, String email, Integer type) {
        switch (type) {
            case 1:
                // old email
                userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_old_link", userId, email,
                        "/v1/public/account/user/email/old/link?requestId=" + flowId, null);
                RedisCacheUtils.set(flowId, userId + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);
                break;
            case 2:
                // new email
                userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_new_link", userId, email,
                        "/v1/public/account/user/email/new/link?requestId=" + flowId, null);
                RedisCacheUtils.set(flowId, userId + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);
                break;
        }
        return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SUCCESS.getCode());
    }

    @Override
    public APIResponse<UserEmailChangeResponse> getEmailChangeList(APIRequest<UserEmailChangeRequest> request) {
        UserEmailChangeResponse response = new UserEmailChangeResponse();

        try {
            Map<String, Object> map = new HashMap<>();

            UserEmailChangeRequest emailChangeRequest = request.getBody();
            if (emailChangeRequest != null) {
                if (emailChangeRequest.getLimit() == 0) {
                    emailChangeRequest.setLimit(10);
                }

                map = MapUtil.beanToMap(emailChangeRequest);
                if (StringUtils.isNotBlank(emailChangeRequest.getStart())) {
                    map.put("start", DateUtils.formatter(emailChangeRequest.getStart(), FORMATTER));
                }

                if (StringUtils.isNotBlank(emailChangeRequest.getEnd())) {
                    map.put("end", DateUtils.addDays(DateUtils.formatter(emailChangeRequest.getEnd(), FORMATTER), 1));
                }
            }
            Integer count = userEmailChangeMapper.totalCount(map);
            List<UserEmailChange> userEmailChanges = userEmailChangeMapper.findList(map);

            if (userEmailChanges != null && !userEmailChanges.isEmpty()) {
                List<Long> userIds = getUserIds(userEmailChanges);
                // 用户的备注信息
                List<UserInfo> userInfos = userInfoMapper.selectUserInfoList(userIds);
                final Map<Long, String> userRemarkMap = Maps.newHashMap();
                userInfos.stream().forEach(item -> userRemarkMap.put(item.getUserId(), item.getRemark()));
                // 提现备注信息
                final Map<Long, String> modifyCauseMap = userSecurityResetHelper.oldWithdrawDailyLimitModifyCause(userIds);

                userEmailChanges.stream().forEach(item -> {
                    item.setUserRemark(userRemarkMap.get(item.getUserId()));
                    item.setWithdrawalRemark(modifyCauseMap.get(item.getUserId()));
                });
            }

            response.setDatas(getBaseEmailVos(userEmailChanges));
            response.setTotalCount(count == null ? 0 : count);
        } catch (Exception e) {
            log.error("UserEmailChangeBusiness getEmailChangeList error is {} , request is {}", e, JSONObject.toJSONString(request));
            return APIResponse.getErrorJsonResult(response);
        }
        return APIResponse.getOKJsonResult(response);
    }

    private List<Long> getUserIds(List<UserEmailChange> userEmailChanges) {
        List<Long> userIds = new ArrayList<>();
        for (UserEmailChange userEmailChange : userEmailChanges) {
            userIds.add(userEmailChange.getUserId());
        }
        return userIds;
    }

    private List<BaseUserEmailChangeVo> getBaseEmailVos(List<UserEmailChange> userEmailChanges) {
        if (userEmailChanges == null || userEmailChanges.isEmpty()) {
            return new ArrayList<>();
        }

        List<BaseUserEmailChangeVo> vos = new ArrayList<>();
        for (UserEmailChange userEmailChange : userEmailChanges) {
            if (userEmailChange == null) {
                continue;
            }
            BaseUserEmailChangeVo vo = new BaseUserEmailChangeVo();
            BeanUtils.copyProperties(userEmailChange, vo);
            vos.add(vo);
        }

        return vos;
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateUserEmailChangeByFlowId(UserEmailChangeRequest request) throws Exception {

        UserEmailChange record = new UserEmailChange();
        BeanUtils.copyProperties(request, record);
        record.setUpdatedAt(new Date());
        userEmailChangeMapper.updateUserEmailChangeByFlowId(record);


        if (record.getStatus() == UserChangeEmailEnum.PASS.getStatus()) {
            UserEmailChange userEmailChange = userEmailChangeMapper.findByFlowId(record.getFlowId());
            // 调用更换邮箱成功接口
            APIResponse<Integer> response = iUser.modifyUserEmail(getModifyUserEmailRequestAPIRequest(userEmailChange));
            if (response != null && response.getData() == 1) {
                // 给新邮箱和老邮箱发送更换成功邮件
                try {
                    userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_old_pass", userEmailChange.getUserId(),
                            userEmailChange.getOldEmail(), null, null);
                    userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_new_pass", userEmailChange.getUserId(),
                            userEmailChange.getNewEmail(), null, null);
                } catch (Exception e) {
                    log.warn("updateUserEmailChangeByFlowId send success email error is {},flowId is {}", e, record.getFlowId());
                }
            } else {
                throw new Exception();
            }
        }

        try {
            if (record.getStatus() == UserChangeEmailEnum.REFUSE.getStatus()) {
                UserEmailChange userEmailChange = userEmailChangeMapper.findByFlowId(record.getFlowId());
                try {
                    rejectEmail(userEmailChange.getFlowId(), userEmailChange.getOldEmail(), userEmailChange.getUserId(),
                            "Your account does not meet the requirements for changing  email address.", "您的账户不符合更换邮箱的要求");
                } catch (Exception e) {
                    log.error("updateUserEmailChangeByFlowId send fail email error is {},flowId is {}", e, record.getFlowId());
                }
            }
        } catch (Exception e) {
            log.error("UserEmailChangeBusiness updateUserEmailChangeByFlowId send email error is {},request is {}", e,
                    JSONObject.toJSONString(request));
        }
    }

    @Override
    public APIResponse<UserEmailChange> findByFlowIdAndUid(String flowId, Long userId) {

        String newFlowId = flowId;
        try {
            if (EncryptUtil.isHexNumber(flowId)) {
                newFlowId = EncryptUtil.decryptHex(flowId, signWorld);
            }
        }catch (Exception e){
            log.error("findByFlowIdAndUid decryptHex error ,flowId is {},error ",flowId,e);
        }

        UserEmailChange userEmailChange = userEmailChangeMapper.findByFlowId(newFlowId);

        if (userEmailChange==null){
            return APIResponse.getOKJsonResult();
        }
        // 流程的userID与登陆的userID不相符
        if (userEmailChange.getUserId() != userId.longValue()) {
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.SYS_NOT_LOGIN.getCode());
        }

        return APIResponse.getOKJsonResult(userEmailChange);
    }

    private APIRequest<ModifyUserEmailRequest> getModifyUserEmailRequestAPIRequest(UserEmailChange userEmailChange) {
        ModifyUserEmailRequest userEmailRequest = new ModifyUserEmailRequest();
        userEmailRequest.setEmail(userEmailChange.getNewEmail());
        userEmailRequest.setUserId(userEmailChange.getUserId());
        userEmailRequest.setNewPassword(userEmailChange.getPwd());
        userEmailRequest.setNewSafePassword(userEmailChange.getNewSafePwd());
        APIRequest<ModifyUserEmailRequest> apiRequest = new APIRequest<>();
        apiRequest.setBody(userEmailRequest);
        return apiRequest;
    }


    private int getIntFromSysConfig(String key, Integer defaultValue) {
        Integer returnValue = null;
        try {
            SysConfig sysConfig = iSysConfig.selectByDisplayName(key);
            if (sysConfig != null && StringUtils.isNotBlank(sysConfig.getCode())) {
                returnValue = Integer.parseInt(sysConfig.getCode().trim());
            }
            return returnValue == null ? defaultValue : returnValue;

        } catch (Exception e) {
            log.error("UserEmailChangeBusiness updateStatus get config error is {}", e);
            return defaultValue;
        }
    }

}
