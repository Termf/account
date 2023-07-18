package com.binance.account.service.certificate;

import com.binance.account.common.query.JumioQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserKycModularQuery;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.service.certificate.impl.UserKycAuditContext;
import com.binance.account.vo.certificate.KycDetailResponse;
import com.binance.account.vo.certificate.request.KycForceToExpiredRequest;
import com.binance.account.vo.certificate.response.KycFormAddrResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.certificate.response.UserSimpleBaseInfoResponse;
import com.binance.account.vo.security.request.UserIdAndIdRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.UserKycApproveVo;
import com.binance.account.vo.user.UserKycVo;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.account.vo.user.request.KycAuditRequest;
import com.binance.account.vo.user.request.KycBaseInfoRequest;
import com.binance.account.vo.user.request.KycSimpleBaseInfoRequest;
import com.binance.account.vo.user.request.SaveJumioSdkScanRefRequest;
import com.binance.account.vo.user.request.UpdateKycApproveRequest;
import com.binance.account.vo.user.response.InitSdkUserKycResponse;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface IUserKyc {

    /**
     * @deprecated  用新接口 {@link com.binance.account.service.kyc.KycApiTransferAdapter#getKycCountry(Long)}
     * @param userId
     * @return
     * @throws Exception
     */
    @Deprecated
    UserKycCountryResponse getKycCountry(Long userId) throws Exception;

    APIResponse<UserKycApproveVo> getApproveUser(@RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    APIResponse<Boolean> checkUserWhetherPassKyc(@RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    APIResponse<?> updateKycApprove(@RequestBody() APIRequest<UpdateKycApproveRequest> request) throws Exception;

    /**
     * kyc审核通过，保存结果，并更新关联数据（用户状态、安全等级）
     */
    void saveApproveResult(UserKycAuditContext auditContext);

    APIResponse<SearchResult<UserKycApproveVo>> getApproveList(@RequestBody() APIRequest<JumioQuery> request) throws Exception;

    APIResponse<JumioTokenResponse> submitBaseInfo(@RequestBody() APIRequest<KycBaseInfoRequest> request) throws Exception;

    APIResponse<InitSdkUserKycResponse> initSdkUserKyc(APIRequest<KycBaseInfoRequest> request);

    void saveJumioSdkScanRef(SaveJumioSdkScanRefRequest body);

    APIResponse<SearchResult<UserKycVo>> getList(@RequestBody() APIRequest<JumioQuery> request) throws Exception;

    APIResponse<?> audit(@RequestBody() APIRequest<KycAuditRequest> request) throws Exception;

    APIResponse<?> syncPhoto(@RequestBody() APIRequest<JumioQuery> request) throws Exception;

    APIResponse<UserKycVo> getKycByUserId(APIRequest<UserIdRequest> request);

    /**
     * 验证KYC是否能自动通过，如果可以则进行通过逻辑，异步执行
     * @param kycId
     * @param userId
     */
    void syncUserKycCanAutoPass(final Long kycId, final Long userId);

    /**
     * JUMIO 验证结果出来后，个人验证的后续处理逻辑
     * @param jumio
     * @param jumioPass
     * @param msg
     */
    void handleUserKyc(Jumio jumio, boolean jumioPass, String msg);

    /**
     * 个人认证模块化查询列表
     * @param query
     * @return
     */
    SearchResult<UserKycVo> getModularUserKycList(UserKycModularQuery query);

    /**
     * 通过主键id和用户id获取单条用户kyc信息
     * @param request
     * @return
     */
    APIResponse<UserKycVo> getUserKycById(APIRequest<UserIdAndIdRequest> request);

    /**
     * 获取用户当前KYC认证状态
     * @param userId
     * @return
     */
    KycDetailResponse getCurrentKycStatus(Long userId);

    /**
     * 强制把已通过的KYC状态变更到过期状态
     * (2019-05-25 目前使用在SGP交易所)
     * @param request
     */
    void forceKycPassedToExpired(KycForceToExpiredRequest request);

    /**
     * 注意：该方法目前只针对 新加坡交易所，直接补充KYC数据
     * @param userKycVo
     * @return
     */
    Boolean saveXfersUserKyc(UserKycVo userKycVo);

    /**
     * 注意：该方法目前只针对 新加坡交易所，更新用户证件图片url
     * @param userKycVo
     * @return
     */
    Boolean updateXfersUserKyc(UserKycVo userKycVo);

    APIResponse<?> refuseApprove(APIRequest<KycAuditRequest> request);

    /**
     * 存储极简单的kyc信息--UG专用
     * @param request
     * @return
     * @throws Exception
     */
    Boolean submitSimpleBaseInfo(KycSimpleBaseInfoRequest request) throws Exception;

    /**
     * ug获取提交简易kyc base信息
     * @return
     * @throws Exception
     */
    APIResponse<UserSimpleBaseInfoResponse> getSimpleBaseInfo(APIRequest<UserIdRequest> request) throws Exception;

    APIResponse<KycFormAddrResponse> getKycFormAddrByUserIds(GetUserListRequest request);

}
