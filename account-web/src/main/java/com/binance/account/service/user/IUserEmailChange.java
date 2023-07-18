package com.binance.account.service.user;


import com.binance.account.data.entity.user.UserEmailChange;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.OldEmailCaptchaResponse;
import com.binance.account.vo.user.response.UserEmailChangeInitResponse;
import com.binance.account.vo.user.response.UserEmailChangeResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public interface IUserEmailChange {

    /**
     * 初始化用户流程
     * @return
     */
    APIResponse<UserEmailChangeInitResponse> initFlow(Long userId, String oldEmail,Integer availableType);

    /**
     * 更新流程状态，发送邮件
     * @param flowId
     * @return
     * @throws Exception
     */
    void updateStatus(String flowId) throws Exception;


    /**
     * 点击老邮箱link
     * @param flowId
     * @return
     */
    @Deprecated
    APIResponse<Void> linkOldEmail(String flowId,Long userId);

    /**
     * 点击老邮箱link
     * @param flowId
     * @return
     */
    APIResponse<Void> linkOldEmailV2(String flowId,Long userId,String sign);


    /**
     * 验证老邮箱验证码
     * @param
     * @return
     */
    APIResponse<OldEmailCaptchaResponse> validOldEmailCaptcha(APIRequest<OldEmailCaptchaRequest> request);

    /**
     * 确认新邮箱
     * @param flowId
     * @param newEmail
     * @return
     */
    @Deprecated
    APIResponse<String> confirmNewEmail(String flowId,String newEmail,String pwd);

    /**
     * 确认新邮箱
     * @param flowId
     * @param newEmail
     * @return
     */
    @Deprecated
    APIResponse<String> confirmNewEmailV2(String flowId,Long userId,String newEmail,String pwd);

    /**
     * 确认新邮箱
     * @return
     */
    APIResponse<String> confirmNewEmailV2(APIRequest<UserEmailChangeConfirmNewEmailRequest> request);

    /**
     * 验证码版本 确认新邮箱
     * @param
     * @return
     */
    APIResponse<String> confirmNewEmailV3(APIRequest<NewEmailConfirmRequest> request);


    /**
     * 点击新邮箱link
     * @param flowId
     * @param userId
     * @return
     */
    @Deprecated
    APIResponse<UserEmailChangeInitResponse> linkNewEmail(String flowId,Long userId);

    /**
     * 点击新邮箱link
     * @param flowId
     * @param userId
     * @return
     */
    APIResponse<UserEmailChangeInitResponse> linkNewEmailV2(String flowId,Long userId,String sign);


    /**
     * 验证新邮箱验证码
     * @param request
     * @return
     */
    APIResponse<UserEmailChangeInitResponse> validNewEmailCaptcha(APIRequest<NewEmailCaptchaRequest> request);


    /**
     * 重新发送邮件
     * @param flowId
     * @param email
     * @param type 1: old  2: new
     * @return
     */
    APIResponse<Void> resendEmail(Long userId,String flowId,String email,Integer type);


    /**
     * 分页查询
     * @param request
     * @return
     */
    APIResponse<UserEmailChangeResponse> getEmailChangeList(APIRequest<UserEmailChangeRequest> request);


    void updateUserEmailChangeByFlowId(UserEmailChangeRequest request) throws Exception;


    /**
     * 根据flowId 和 userId获取 更换邮箱信息
     * @param flowId
     * @param userId 这个最好从 binance-mgs 通过token获取得到。
     * @return
     */
    APIResponse<UserEmailChange> findByFlowIdAndUid(String flowId, Long userId);

}
