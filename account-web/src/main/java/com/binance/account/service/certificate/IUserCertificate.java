package com.binance.account.service.certificate;


import com.binance.account.common.query.CompanyCertificateQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserCertificateListRequest;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.vo.certificate.CompanyCertificateVo;
import com.binance.account.vo.certificate.UserCertificateVo;
import com.binance.account.vo.certificate.request.SaveCompanyCertificateRequest;
import com.binance.account.vo.certificate.request.SaveUserCertificateRequest;
import com.binance.account.vo.certificate.request.UserAuditCertificateResponse;
import com.binance.account.vo.certificate.request.UserDetectCertificateRequest;
import com.binance.account.vo.certificate.response.SaveUserCertificateResponse;
import com.binance.account.vo.certificate.response.UserDetectCertificateResponse;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.request.CompanyCertificateAuditRequest;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.util.Date;

public interface IUserCertificate {

    /**
     * 获取个人认证信息
     * (2018-12-06标识为改接口将去除, 老板版的个人认证信息)
     * @param request
     * @return
     */
    @Deprecated
    APIResponse<UserCertificateVo> getUserCertificateByUserId(APIRequest<UserIdRequest> request);

    /**
     * 保存个人认证信息
     * (2018-12-06标识为改接口将去除, 老板版的个人认证信息)
     * @param request
     * @return
     */
    @Deprecated
    APIResponse<SaveUserCertificateResponse> saveUserCertificate(APIRequest<SaveUserCertificateRequest> request);

    /**
     * 上传个人认证信息
     * (2018-12-06标识为改接口将去除, 老板版的个人认证信息)
     * @param request
     * @return
     */
    @Deprecated
    APIResponse<SaveUserCertificateResponse> uploadUserCertificate(
            APIRequest<SaveUserCertificateRequest> request);

    /**
     * 用户身份认证验证
     * (2018-12-06标识为改接口将去除, 老板版的个人认证信息)
     * @param request
     * @return
     */
    @Deprecated
    APIResponse<UserDetectCertificateResponse> userDetectCertificate(
            APIRequest<UserDetectCertificateRequest> request);

    /**
     * 用户审核
     * (2018-12-06标识为改接口将去除, 老板版的个人认证信息)
     * @param request
     * @return
     */
    @Deprecated
    APIResponse<UserAuditCertificateResponse> userAuditCertificate(
            APIRequest<SaveUserCertificateRequest> request);

    /**
     * 身份认证，更新身份认证状态
     * @param userId    user.id
     * @param isPassed  true.认证通过
     */
    int updateCertificateStatus(Long userId, boolean isPassed);

    /**
     * 更新安全等级
     * @param userId  user.id
     * @param securityLevel 1:普通,2:身份认证,3:?
     */
    int updateSecurityLevel(Long userId, Integer securityLevel);

    /**
     * 上传企业认证信息
     *
     * @param request
     * @return
     */
    APIResponse<JumioTokenResponse> uploadCompanyCertificate(
            APIRequest<SaveCompanyCertificateRequest> request);

    /**
     * 企业信息审核
     *
     * @param request
     * @return
     */
    APIResponse<?> companyAuditCertificate(
            APIRequest<CompanyCertificateAuditRequest> request);

    void removeCertificateIndex(Long userId,Long certificateId,String country,String number,String documentType);

    void deleteKycEndHandler(Long userId, Long certificateId, String jumioScanRef);

    /**
     * 获取用户企业认证信息
     * @param request
     * @return
     */
    APIResponse<CompanyCertificateVo> getCompanyCertificate(APIRequest<UserIdRequest> request);

    /**
     * 获取用户企业认证信息
     * @param request
     * @return
     */
    APIResponse<SearchResult<CompanyCertificateVo>> getCompanyCertificateList(APIRequest<CompanyCertificateQuery> request);


    /**
     * 修改信息审核
     *
     * @param request
     * @return
     */
    APIResponse<?> modifyCompanyCertificate(APIRequest<CompanyCertificateVo> request);

    /**
     * 证件号是否被其他人占用
     * @param number 证件号
     * @param countryCode 国家代码
     * @param userId 用户id
     */
    boolean isIDNumberOccupied(String number, String countryCode,String type,  Long userId);

    /**
     * 当JUMIO 信息过期时的处理逻辑
     * @param userId
     * @param jumio
     */
    void jumioExpireHandler(Long userId, Jumio jumio);

    /**
     * 个人KYC认证过期
     * @param userKyc
     */
    void userKycExpired(UserKyc userKyc);

    /**
     * 企业KYC认证过期
     * @param certificate
     */
    void companyKycExpired(CompanyCertificate certificate);

    /**
     * 当JUMIO 解析结果出来后的后续处理逻辑
     * @param jumio
     * @param jumioPass
     * @param msg
     */
    void handleCompanyCertificate(Jumio jumio, boolean jumioPass, String msg);

    /**
     * 验证JUMIO返回的证件号是否被占用，验证的保护KYC通过的用户的和RESET通过的记录
     * @param userId
     * @param idNumber
     * @param countryCode
     * @param idType
     * @return true-已经被别的用户占用, false-未被占用
     */
    boolean isJumioIdNumberUseByOtherUser(Long userId, String idNumber, String countryCode, String idType);

    /**
     * 从主库获取KYC信息
     * @param userId
     * @param jumioId
     * @return
     */
    UserKyc getUserKycFromMasterDbByJumioId(Long userId, String jumioId);

    /**
     * 从主库获取KYC信息
     * @param userId
     * @param id
     * @return
     */
    UserKyc getUserKycFromMasterDbById(Long userId, Long id);

    /**
     * 从主库获取企业KYC信息
     * @param userId
     * @param jumioId
     * @return
     */
    CompanyCertificate getCompanyKycFromMasterDbByJumioId(Long userId, String jumioId);

    /**
     * 从主库获取企业KYC信息
     * @param userId
     * @param id
     * @return
     */
    CompanyCertificate getCompanyKycFromMasterDbById(Long userId, Long id);

    /**
     * 查询用户身份信息
     * @param request
     * @return
     */
    SearchResult<UserCertificateVo> listUserCertificate(UserCertificateListRequest request);

    /**
     * 企业认证信息审核通过重置为失败
     *
     * @param request
     * @return
     */
    APIResponse<?> refuseCompanyCertificate(
            APIRequest<CompanyCertificateAuditRequest> request);

    /**
     * kyc 通过后调用securityFace检查检查
     * @param userId
     * @param transId
     * @param faceTransType
     * @param refTransId
     * @param kycStatus
     * @param kycPassTime
     */
    void kycPassCheckSecurityFaceCheck(Long userId, String transId, FaceTransType faceTransType, String refTransId, String kycStatus, Date kycPassTime);

}
