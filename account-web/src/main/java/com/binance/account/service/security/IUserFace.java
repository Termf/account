package com.binance.account.service.security;

import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.TransactionFaceQuery;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.TransactionFaceLogVo;
import com.binance.account.vo.face.request.FaceEmailRequest;
import com.binance.account.vo.face.request.FaceInitRequest;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.FaceReferenceRequest;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.FaceReferenceResponse;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.util.List;

/**
 * @author liliang1
 * @date 2018-12-07 17:59
 */
public interface IUserFace {

    /**
     *  PC 端人脸识别初始化
     * @param request
     * @return
     */
    FaceInitResponse facePcInit(FaceInitRequest request);



    /**
     *  SDK 端人脸识别认证初始化
     * @param request
     * @return
     */
    FaceInitResponse faceSdkInit(FaceInitRequest request);

    /**
     * PC 端人脸识别结果验证
     * @param sign
     * @param data
     * @return 返回成功或者失败的重定向页面地址
     */
    String facePcVerify(String sign, String data);
    
    /**
     * PC 端人脸识别私有云结果验证
     * @param request
     */
    void facePcVerifyPrivate(FacePcPrivateResult request);

    /**
     * SDK 端人脸识别认证结果验证
     * @param request
     * @return
     */
    FaceSdkResponse appFaceSdkVerify(FaceSdkVerifyRequest request);

    /**
     * 验证SDK端做人脸识别的二维码是否还能进行验证
     * @param qrCode
     * @return
     */
    APIResponse<Void> appFaceSdkQrValid(String qrCode);

    /**
     * 判断业务的人脸识别是否已经通过
     * @param body
     * @return
     */
    boolean isFacePassed(FaceInitRequest body);


    /**
     * 保存或者更新用户的人脸对比照片信息
     * @param faceReferenceRequest
     * @return
     */
    boolean saveUserFaceReferenceCheckImage(FaceReferenceRequest faceReferenceRequest);

    /**
     * 查询对应业务的人脸识别业务列表
     * @param body
     * @return
     */
    SearchResult<TransactionFaceLogVo> getTransactionFaceLogs(TransactionFaceQuery body);

    /**
     * 重发人脸识别邮件
     * @param body
     */
    void resendFaceEmail(FaceInitRequest body);

    /**
     * 通过邮箱和类型，重发人脸识别邮件
     * @param request
     */
    void resendFaceEmailByEmail(FaceEmailRequest request);

    /**
     * 获取用户当前人脸对比照信息
     *
     * @param request
     * @return
     */
    APIResponse<FaceReferenceResponse> getUserFaceReference(APIRequest<UserIdRequest> request);

    /**
     * 根据USER ID 列表获取用户的对比照信息列表
     *
     * @param request
     * @return
     */
    List<FaceReferenceResponse> getUserFaceReferenceByUserIds(GetUserListRequest request);

    /**
     * 初始化人脸识别流程
     * @param transId
     * @param userId
     * @param faceTransType
     * @param needEmail
     * @param isKycLockOne 是否为KYC单一锁定数据
     */
    FaceFlowInitResult initFaceFlowByTransId(String transId, Long userId, FaceTransType faceTransType, boolean needEmail, boolean isKycLockOne);

    /**
     * 当业务通过时， 如果人脸识别流程状态不是最终态，则变更为终态
     * @param userId
     * @param transId
     * @param faceTransType
     * @param status
     * @param failReason
     */
    void endTransFaceLogStatus(Long userId, String transId, FaceTransType faceTransType, TransFaceLogStatus status, String failReason);

    /**
     * 由于人脸上传照片有问题需要重新上传照片的逻辑
     * @param transId
     * @param userId
     * @param faceTransType
     * @param isLockOne 是否为新版本的锁定用户KYC
     */
    void faceImageErrorRedoUpload(String transId, Long userId, FaceTransType faceTransType, boolean isLockOne);

    /**
     * 人脸识别流程审核
     *
     * @param auditRequest
     */
    void transFaceAudit(TransFaceAuditRequest auditRequest);
}
