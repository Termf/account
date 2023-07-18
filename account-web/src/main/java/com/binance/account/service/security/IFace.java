package com.binance.account.service.security;

import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.service.security.impl.FaceBusiness;
import com.binance.account.vo.kyc.request.FaceOcrSubmitRequest;
import com.binance.account.vo.security.FaceTransTypeContentVo;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.inspector.common.enums.FaceErrorCode;
import com.binance.inspector.common.enums.FaceOcrUploadType;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.faceid.FaceLogVo;
import com.binance.inspector.vo.faceid.face.FaceWebGetTokenResponse;
import com.binance.inspector.vo.faceid.response.FaceCaptureResponse;
import com.binance.inspector.vo.faceid.response.FaceSDKVerifyResponse;
import com.binance.inspector.vo.faceid.response.FaceWebInitResponse;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.inspector.vo.faceid.response.FaceWebSyncImageResponse;
import com.binance.inspector.vo.faceid.response.IdCardOcrResponse;
import com.binance.inspector.vo.faceid.response.ImageCanDoFaceResponse;
import com.binance.master.enums.LanguageEnum;

import java.util.Date;
import java.util.List;

/**
 * @author liliang1
 * @date 2018-10-19 16:56
 */
public interface IFace {

    /**
     * 获取kyc通过的用于做人脸识别的图片地址
     * 这个针对的是新kyc流程的，并且已经确保kyc通过
     * @param userId
     * @param kycCertificateResult
     * @return
     */
    String getCertificateImage(Long userId, KycCertificateResult kycCertificateResult);

    /**
     * 初始化PC端人脸识别获取Token
     * @param userId 用户ID
     * @param transId 业务编号
     * @param transType 业务类型
     * @param imageName 对比照片路径
     * @param isImageScale 图片是否放缩(如果图片放缩，并且返回值有有压缩图片，会直接保存更新到对照表中)
     * @param saveFaceLog 是否保存人脸操作日志
     * @param language 语言，主要获取对应的场景
     * @return
     */
    FaceWebInitResponse faceWebInitHandler(Long userId, String transId, FaceTransType transType, String imageName,
                                           boolean isImageScale, boolean saveFaceLog, LanguageEnum language, String baseUrl);
    
    /**
     * 初始化PC端人脸识别获取Token
     * @param userId 用户ID
     * @param transId 业务编号
     * @param transType 业务类型
     * @param imageName 对比照片路径
     * @param isImageScale 图片是否放缩(如果图片放缩，并且返回值有有压缩图片，会直接保存更新到对照表中)
     * @param saveFaceLog 是否保存人脸操作日志
     * @param language 语言，主要获取对应的场景
     * @return
     */
    FaceWebGetTokenResponse faceWebInitPrivateHandler(Long userId, String transId, FaceTransType transType, String imageName,
                                           boolean isImageScale, boolean saveFaceLog, LanguageEnum language, String baseUrl);


    /**
     * 保存更新人脸识别的对比源照片信息
     * @param faceReference
     * @param refImage
     * @return
     */
    boolean saveFaceReferenceCheckImage(UserFaceReference faceReference, byte[] refImage);

    /**
     * 直接保存一个check_image
     * @param userId
     * @param sourcePath
     * @param checkPath
     * @param sourceType
     */
    void saveFaceReferenceCheckImage(Long userId, String sourcePath, String checkPath, String sourceType);

    /**
     * 强制从主库中获取人脸识别对比源信息
     *
     * @param userId
     * @return
     */
    UserFaceReference getUserFaceByMasterBD(Long userId);

    /**
     * 把人脸对比照片中的检查对比源照片更新到业务完成后的对比照片信息
     * @param userId
     * @return
     */
    boolean saveFaceReferenceRefImage(Long userId);

    /**
     * 把用户的正式人脸识别对比源图片清除
     * @param userId
     * @return
     */
    int removeFaceReferenceRefImage(Long userId);

    /**
     * 保存KYC已通过的图片到人脸识别检查图片中
     * @param userId
     * @param transId
     * @param imageValidate
     * @return
     */
    boolean saveKycPassUserFaceReference(Long userId, String transId, ImageCanDoFaceResponse imageValidate);

    /**
     * 对web pc 端的face 验证结果进行解析
     * @param sign
     * @param data
     * @return
     */
    FaceWebResultResponse webFaceResultHandler(String sign, String data);

    /**
     * 获取Web PC 端人脸识别的照片
     * @param userId
     * @param bizNo
     * @return
     */
    FaceWebSyncImageResponse webFaceSyncImageHandler(Long userId, String bizNo);

    /**
     * 获取 域名网站 和对应API的路径
     * @param baseUrl 允许空值，当为空时按默配置值处理
     * @param key
     * @param language
     * @return
     */
    String getFaceApiPath(String baseUrl, String key, LanguageEnum language);


    /**
     * 根据路径信息，截取图片中的人脸照片
     * @param userId
     * @param imagePath
     * @param imageScale 照片是否压缩
     * @param faceTransType
     * @return
     */
    FaceCaptureResponse faceImageCapture(Long userId, String imagePath, boolean imageScale, FaceTransType faceTransType);

    /**
     * 初始化生成一个二维码的值，用于SDK端操作人脸识别使用
     * @param userId
     * @param transId
     * @param transType
     * @return
     */
    String initFaceSdkQrCode(Long userId, String transId, FaceTransType transType);

    /**
     * 校验二维码是否已经过期
     * @param qrCode
     * @return
     */
    FaceBusiness.FaceSdkCache validFaceSdkCacheByQrCode(String qrCode);

    /**
     * 根据初始化生成的二维码值获取缓存中的信息
     * @param qrCode
     * @return
     */
    FaceBusiness.FaceSdkCache getFaceSdkCacheByQrCode(String qrCode);

    /**
     * 人脸识别SDK验证方式进行获取验证结果
     * @param verifyRequest SDK的验证信息(transId是用于解析业务类型的，该方法中不再处理该值)
     * @param userId 用户ID
     * @param transId 业务标识
     * @param transType 类型
     * @param imageRef 对比照片路径
     * @param imageRefScale 对比照片是否进行放缩
     * @return
     */
    FaceSDKVerifyResponse faceSdkVerify(Long userId, String transId, FaceTransType transType, String imageRef, boolean imageRefScale, FaceSdkVerifyRequest verifyRequest);


    /**
     * 验证照片是否能做人脸识别
     * @param userId 用户ID
     * @param transId 业务标识
     * @param image 验证的照片地址
     * @param imageScale 照片是否需要放缩
     * @param saveLog 是否保存验证日志
     * @return
     */
    ImageCanDoFaceResponse validateFaceImageCanUsed(Long userId, String transId, String image, boolean imageScale, boolean saveLog);
    /**
     * 不同人脸识别业务对应的验证成功和失败的
     * @param transType
     * @param success
     * @return
     */
    FaceTransTypeContentVo getTransTypeContent(FaceTransType transType, boolean success);

    /**
     * 获取24小时内某一比业务的验证次数
     * @param userId
     * @param transId
     * @param transType
     * @param source
     * @return
     */
    int getFaceLogDailyTimes(Long userId, String transId, FaceTransType transType, String source);

    /**
     * 查询某一次业务的人脸识别验证次数
     * @param userId 当userId为空值时返回0
     * @param transId
     * @param transType
     * @param faceStatus
     * @param faceErrorCode
     * @return 其他参数为null时可以查询用户的对应人脸验证次数
     */
    int getFaceLogTimes(Long userId, String transId, FaceTransType transType, FaceStatus faceStatus, FaceErrorCode faceErrorCode);

    /**
     * 获取某一笔人脸识别记录信息
     * @param userId
     * @param transId
     * @param bizNo
     * @return
     */
    FaceLogVo getFaceLogByBizNo(Long userId, String transId, String bizNo);

    /**
     * 获取用户所有的人脸识别流水
     * @param userId
     * @param transId
     * @param transType
     * @param faceStatus
     * @return
     */
    List<FaceLogVo> getFaceLogsByUser(Long userId, String transId, FaceTransType transType, FaceStatus faceStatus);


    /**
     * 获取用户在某一段时间内提交过的ocr次数
     * @param userId
     * @param type
     * @param startTime
     * @param endTime
     * @return
     */
    int getFaceOcrTimes(Long userId, FaceOcrUploadType type, Date startTime, Date endTime);

    /**
     * 提交face ocr 信息进行验证
     * @param request
     * @return
     */
    IdCardOcrResponse faceOcrValidate(FaceOcrSubmitRequest request);


    /**
     * 获取用户的ocr 信息
     * @param userId
     * @return
     */
    FaceIdCardOcrVo getFaceIdCardOcr(Long userId);

    /**
     * 重置face id ocr
     * @param userId
     */
    void resetFaceIdOcr(Long userId);
}
