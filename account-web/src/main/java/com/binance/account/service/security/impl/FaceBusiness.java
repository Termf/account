package com.binance.account.service.security.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserFaceReferenceMapper;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.file.IFileStorage;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.security.IFace;
import com.binance.account.utils.MessageUtils;
import com.binance.account.vo.kyc.request.FaceOcrSubmitRequest;
import com.binance.account.vo.security.FaceTransTypeContentVo;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.inspector.api.FaceIdApi;
import com.binance.inspector.common.enums.FaceErrorCode;
import com.binance.inspector.common.enums.FaceOcrUploadType;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.common.query.FaceVerificationCountRequest;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.faceid.FaceLogVo;
import com.binance.inspector.vo.faceid.face.FaceWebGetTokenRequest;
import com.binance.inspector.vo.faceid.face.FaceWebGetTokenResponse;
import com.binance.inspector.vo.faceid.request.FaceIdSignRequest;
import com.binance.inspector.vo.faceid.request.FaceImageCaptureRequest;
import com.binance.inspector.vo.faceid.request.FaceSDKVerifyRequest;
import com.binance.inspector.vo.faceid.request.FaceWebInitRequest;
import com.binance.inspector.vo.faceid.request.FaceWebSyncImageRequest;
import com.binance.inspector.vo.faceid.request.GetFaceLogRequest;
import com.binance.inspector.vo.faceid.request.IdCardOcrRequest;
import com.binance.inspector.vo.faceid.request.ImageCanDoFaceRequest;
import com.binance.inspector.vo.faceid.response.FaceCaptureResponse;
import com.binance.inspector.vo.faceid.response.FaceSDKVerifyResponse;
import com.binance.inspector.vo.faceid.response.FaceWebInitResponse;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.inspector.vo.faceid.response.FaceWebSyncImageResponse;
import com.binance.inspector.vo.faceid.response.IdCardOcrResponse;
import com.binance.inspector.vo.faceid.response.ImageCanDoFaceResponse;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.commons.ToString;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.WebUtils;
import io.shardingsphere.api.HintManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author liliang1
 * @date 2018-10-19 16:57
 */
@Log4j2
@Service
public class FaceBusiness implements IFace {

    private static final String LOCAL_PATH = ":local";
    public static final String FACE_SDK_QACODE_VALID_CACHE = "FACE_SDK_QACODE_VALID_%s";
    private static final String FACE_SDK_QACODE_CACHE = "FACE_SDK_QACODE_%s";
    private static final String FACE_IMAGE_PATH = "/FACE_IMG";

    private static final String TRANSTYPE_SUCCESS_TITLE_DEFAULT = "face.transType.successTitle.default";
    private static final String TRANSTYPE_SUCCESS_CONTENT_DEFAULT = "face.transType.successContent.default";
    private static final String TRANSTYPE_FAIL_TITLT_DEFAULT = "face.transType.failTitle.default";
    private static final String TRANSTYPE_FAIL_CONTENT_DEFAULT = "face.transType.failContent.default";
    private static final String TRANSTYPE_SUCCESS_CONTENT_WITHDRAWFACE = "face.transType.successContent.withdrawFace";
    private static final String TRANSTYPE_SUCCESS_CONTENT_KYC_USER = "face.transType.successContent.kycUser";
    private static final String TRANSTYPE_SUCCESS_CONTENT_KYC_COMPANY = "face.transType.successContent.kycCompany";
    private static final String TRANSTYPE_FAIL_CONTENT_KYC = "face.transType.failContent.kyc";

    @Resource
    private FaceIdApi faceIdApi;
    @Resource
    protected UserFaceReferenceMapper userFaceReferenceMapper;
    @Resource
    private IFileStorage fileStorage;
    @Resource
    private MessageUtils messageUtils;
    @Resource
    private ApolloCommonConfig commonConfig;
    @Resource
    private UserKycMapper userKycMapper;
    @Resource
    private CompanyCertificateMapper companyCertificateMapper;
    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private TransactionFaceLogMapper transactionFaceLogMapper;

    @Resource
    private KycCertificateMapper kycCertificateMapper;


    /**
     * 新流程的数据，直接获取最后一次jumio信息的图片, 如果获取不到，就获取id_card_ocr
     * @param userId
     * @return
     */
    public String getCertificateImage(@NotNull Long userId, KycCertificateResult certificateResult) {
        if (certificateResult == null || certificateResult.getCertificateStatus() == null || KycCertificateResult.STATUS_PASS != certificateResult.getCertificateStatus()) {
            return null;
        }
        String imagePath = null;
        if (certificateResult.isNewVersion()) {
            // 先获取最后一次jumio的记录信息
            JumioInfoVo jumioInfoVo = jumioBusiness.getLastByUserId(userId);
            if (jumioInfoVo != null && JumioStatus.PASSED.equals(jumioInfoVo.getStatus()) && StringUtils.isNotBlank(jumioInfoVo.getFace())) {
                imagePath = jumioInfoVo.getFace();
            }
            if (imagePath == null) {
                // 获取id_card_ocr
                FaceIdCardOcrVo cardOcrVo = getFaceIdCardOcr(userId);
                if (cardOcrVo != null && IdCardOcrStatus.PASS.equals(cardOcrVo.getStatus()) && StringUtils.isNotBlank(cardOcrVo.getFaceCheck())) {
                    imagePath = cardOcrVo.getFaceCheck();
                }
            }
        }else {
            imagePath = getOldCertificateImage(userId, certificateResult.getCertificateType(), certificateResult.getCertificateId());
        }
        return imagePath;
    }

    /**
     * 获取KYC认证通过时使用做人脸识别的JUMIO图片(注意获取到的图片需要先经过检查)
     * (这个前提是已经检查过user_face_reference表)
     * @param userId
     * @param certificateType
     * @param certificateId 允许为空，当空时获取最后一笔的记录
     * @return
     */
    private String getOldCertificateImage(@NotNull Long userId, @NotNull Integer certificateType, Long certificateId) {
        String scanRef = "";
        JumioHandlerType handlerType = null;
        if (certificateType == null) {
            log.info("获取用户的认证类型错误. userId:{}", userId);
            return null;
        }
        if (KycCertificateResult.TYPE_USER== certificateType) {
            //用户是通过 个人认证通过的KYC，获取最后一条通过记录的scanRef获取对应的照片地址
            UserKyc userKyc = null;
            if (certificateId == null) {
                userKyc = userKycMapper.getLast(userId);
            }else {
                userKyc = userKycMapper.getById(userId, certificateId);
            }
            if (userKyc != null && !userKyc.isOcrFlow() && StringUtils.isNotBlank(userKyc.getScanReference())
                    && StringUtils.equalsAnyIgnoreCase(userKyc.getStatus().name(), KycStatus.passed.name(), KycStatus.forbidPassed.name())) {
                scanRef = userKyc.getScanReference();
                handlerType = JumioHandlerType.USER_KYC;
            }else if (userKyc != null && userKyc.isOcrFlow()) {
                // 如果是face ocr 的认证流程，需要从face ocr结果中获取
                FaceIdCardOcrVo idCardOcrVo = getFaceIdCardOcr(userId);
                if (idCardOcrVo == null || StringUtils.isBlank(idCardOcrVo.getFace()) || !IdCardOcrStatus.PASS.equals(idCardOcrVo.getStatus())) {
                    return null;
                }else {
                    return idCardOcrVo.getFace();
                }
            }
        }else if (KycCertificateResult.TYPE_COMPANY == certificateType) {
            // 用户是通过 企业认证通过的KYC，同样获取对应的scanRef获取到对应的照片地址
            CompanyCertificate companyCertificate = null;
            if (certificateId == null) {
                companyCertificate = companyCertificateMapper.getLast(userId);
            }else {
                companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId, certificateId);
            }
            if (companyCertificate != null && StringUtils.isNotBlank(companyCertificate.getScanReference())
                    && StringUtils.equalsAnyIgnoreCase(companyCertificate.getStatus().name(), CompanyCertificateStatus.passed.name(), CompanyCertificateStatus.forbidPassed.name())) {
                scanRef = companyCertificate.getScanReference();
                handlerType = JumioHandlerType.COMPANY_KYC;
            }
        }
        if (StringUtils.isBlank(scanRef) || handlerType == null) {
            log.info("获取JUMIO唯一标识码失败.userId:{} certificateType:{}", userId, certificateType);
            return null;
        }
        JumioInfoVo vo = jumioBusiness.getByUserAndScanRef(userId, scanRef, handlerType.getCode());
        if (vo == null) {
            // 再次尝试获取
            vo = jumioBusiness.getByUserAndScanRef(userId, scanRef, handlerType.getCode());
        }
        if (vo == null || StringUtils.isBlank(vo.getFace())) {
            log.info("获取JUMIO验证的第三张图片信息失败. userId:{} scanRef:{}", userId, scanRef);
            return null;
        }else {
            log.info("获取到KYC验证的手持照片信息：userId:{} scanRef:{} image:{}", userId, scanRef, vo.getFace());
            return vo.getFace();
        }
    }

    @Override
    public FaceWebInitResponse faceWebInitHandler(Long userId, String transId, FaceTransType transType, String imageName,
                                                  boolean isImageScale, boolean saveFaceLog, LanguageEnum language, String baseUrl) {
        if (userId == null || transType == null || StringUtils.isAnyBlank(transId, imageName)) {
            log.info("初始化PC端人脸识别请求参数错误.");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        try {
            FaceWebInitRequest webInitRequest = new FaceWebInitRequest();
            webInitRequest.setUserId(userId);
            webInitRequest.setTransId(transId);
            webInitRequest.setTransType(transType);
            webInitRequest.setImageRefName(imageName);
            webInitRequest.setImageScale(isImageScale);
            APIRequestHeader header = new APIRequestHeader();
            header.setLanguage(language);
            header.setTerminal(TerminalEnum.PC);
            header.setDomain(baseUrl);
            APIResponse<FaceWebInitResponse> response = faceIdApi.webFaceInit(APIRequest.instance(header, webInitRequest));
            if (response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
                log.info("Web PC 端重置流程初始化Face Token 未知异常失败. userId:{} transId:{}", userId, transId);
                throw new BusinessException(GeneralCode.AC_RESET_FACE_TOKEN_FAIL);
            }
            FaceWebInitResponse result = response.getData();
            String logStr = LogMaskUtils.maskJsonString(JSON.toJSONString(result), "contrastImage");
            int contrastImageLength = result.getContrastImage() == null ? 0 : result.getContrastImage().length;
            log.info("Web PC 端重置流程初始化Face Token 结果: userId:{} transId:{} result:{} contrastImageLength:{}", userId, transId, logStr, contrastImageLength);
            return result;
        }catch(BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("WEB PC端人脸识别初始化请求异常: userId:{} transId:{}", userId, transId, e);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_TOKEN_FAIL);
        }
    }

    @Override
	public FaceWebGetTokenResponse faceWebInitPrivateHandler(Long userId, String transId, FaceTransType transType,
			String imageName, boolean isImageScale, boolean saveFaceLog, LanguageEnum language, String baseUrl) {
    	if (userId == null || transType == null || StringUtils.isAnyBlank(transId, imageName)) {
            log.info("初始化PC端私有云人脸识别请求参数错误.");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        try {
        	FaceWebGetTokenRequest webInitRequest = new FaceWebGetTokenRequest();
            webInitRequest.setUserId(userId);
            webInitRequest.setTransId(transId);
            webInitRequest.setTransType(transType);
            webInitRequest.setImageRefName(imageName);
            webInitRequest.setBaseUrl(baseUrl);
            webInitRequest.setLanguage(language);
            APIRequestHeader header = new APIRequestHeader();
            header.setLanguage(language);
            header.setTerminal(TerminalEnum.PC);
            header.setDomain(baseUrl);
            APIResponse<FaceWebGetTokenResponse> response = faceIdApi.facePrivateWebTokenInit(APIRequest.instance(header, webInitRequest));
            if (response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
                log.info("Web PC 端重置私有云流程初始化Face Token 未知异常失败. userId:{} transId:{}", userId, transId);
                throw new BusinessException(GeneralCode.AC_RESET_FACE_TOKEN_FAIL);
            }
            FaceWebGetTokenResponse result = response.getData();

            log.info("Web PC 端重置流程私有云初始化Face Token 结果: userId:{} transId:{} result:{} ", userId, transId, result);
            return result;
        }catch(BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("WEB PC端私有云人脸识别初始化请求异常: userId:{} transId:{}", userId, transId, e);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_TOKEN_FAIL);
        }
	}

    /**
     * 保存或者更新人脸检查对比照片
     * 保存成功返回true, 否则返回false
     */
    @Override
    public boolean saveFaceReferenceCheckImage(UserFaceReference faceReference, byte[] checkImageData) {
        if (faceReference == null || faceReference.getUserId() == null || checkImageData == null || checkImageData.length <= 0) {
            return false;
        }
        Long userId = faceReference.getUserId();
        try {
            log.info("保存或者更新人脸检查对比照片. userId:{}, sourceType:{}", userId, faceReference.getSourceType());
            //把照片保存
            StringBuilder sb = new StringBuilder();
            sb.append(FACE_IMAGE_PATH).append(DateUtils.formatter(new Date(), DateUtils.SIMPLE_NUMBER_PATTERN)).append("/");
            String path = sb.toString();
            //都是已JPG的格式保存
            String imageName = String.format("%s%d_faceref_%d%s", path, userId, new Random().nextInt(10000000), ".jpg");
            fileStorage.save(checkImageData, imageName);
            log.info("上传对比照图片到FTP成功. userId:{} imageName:{} imageLength:{}", userId, imageName, checkImageData.length);
            faceReference.setCheckImage(imageName);
            faceReference.setCreateTime(DateUtils.getNewUTCDate());
            faceReference.setUpdateTime(DateUtils.getNewUTCDate());
            if (userFaceReferenceMapper.updateByPrimaryKeySelective(faceReference) <= 0) {
                userFaceReferenceMapper.insert(faceReference);
            }
            return true;
        }catch (Exception e) {
            log.error("保存或者更新人脸检查对比照片失败. userId:{}", faceReference.getUserId(), e);
            return false;
        }
    }

    /**
     * 直接保存一个check_image
     * @param userId
     * @param sourcePath
     * @param checkPath
     * @param sourceType
     */
    @Override
    public void saveFaceReferenceCheckImage(Long userId, String sourcePath, String checkPath, String sourceType) {
        if (userId == null || StringUtils.isBlank(checkPath)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserFaceReference reference = new UserFaceReference();
        reference.setUserId(userId);
        reference.setSourceImage(sourcePath);
        reference.setCheckImage(checkPath);
        reference.setSourceType(sourceType);
        reference.setCreateTime(DateUtils.getNewUTCDate());
        reference.setUpdateTime(DateUtils.getNewUTCDate());
        if (userFaceReferenceMapper.updateByPrimaryKeySelective(reference) <= 0) {
            userFaceReferenceMapper.insert(reference);
        }
    }

    /**
     * 强制从主库读取人脸识别的对比照信息
     *
     * @param userId
     * @return
     */
    @Override
    public UserFaceReference getUserFaceByMasterBD(Long userId) {
        if (userId == null) {
            return null;
        }
        HintManager hintManager = null;
        UserFaceReference userFaceReference = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            userFaceReference = userFaceReferenceMapper.selectByPrimaryKey(userId);
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
        return userFaceReference;
    }

    @Override
    public boolean saveFaceReferenceRefImage(Long userId) {
        try {
            // 防止主从同步问题，先暂停下
            Thread.sleep(600);
        }catch (InterruptedException e) {
            log.error("save face ref image interrupted. userId:{}", userId);
            return false;
        }
        UserFaceReference userFaceReference = getUserFaceByMasterBD(userId);
        if (userFaceReference == null) {
            log.info("查询不到用户的对比照片信息记录. userId:{}", userId);
            return false;
        }
        if (StringUtils.isBlank(userFaceReference.getCheckImage())) {
            log.info("用户人脸识别对比照表中的检查照片信息缺失. userId:{}", userId);
            return false;
        }
        UserFaceReference newReference = new UserFaceReference();
        newReference.setUserId(userFaceReference.getUserId());
        newReference.setRefImage(userFaceReference.getCheckImage());
        newReference.setRefImageName(userFaceReference.getCheckImage());
        newReference.setUpdateTime(DateUtils.getNewUTCDate());
        int row = userFaceReferenceMapper.updateRefImage(newReference);
        log.info("更新用户的对比源正式照片结果: userId:{} row:{}", userId, row);
        return row > 0;
    }


    @Override
    public int removeFaceReferenceRefImage(Long userId) {
        UserFaceReference faceReference = new UserFaceReference();
        faceReference.setUserId(userId);
        faceReference.setRefImage(null);
        faceReference.setRefImageName(null);
        faceReference.setUpdateTime(DateUtils.getNewUTCDate());
        int row = userFaceReferenceMapper.updateRefImage(faceReference);
        log.info("清除用户人脸识别正式对比源图片. userId:{} row:{}", userId, row);
        return row;
    }

    /**
     * 保存KYC通过的检查图片用于人脸识别使用的验证图片
     * @param userId
     * @param transId
     * @param imageValidate
     * @return
     */
    @Override
    public boolean saveKycPassUserFaceReference(Long userId, String transId, ImageCanDoFaceResponse imageValidate) {
        final UserFaceReference userFaceReference = new UserFaceReference();
        userFaceReference.setUserId(userId);
        userFaceReference.setSourceType(FaceTransType.KYC_USER.name());
        userFaceReference.setSourceImage(imageValidate.getSourceImage());
        userFaceReference.setSourceScale(imageValidate.getImageScale());
        userFaceReference.setNeedScale(false);
        userFaceReference.setRefQuality(imageValidate.getQuality());
        userFaceReference.setQualityThreshold(imageValidate.getQualityThreshold());
        userFaceReference.setOrientation(imageValidate.getOrientation());
        boolean faceSave = this.saveFaceReferenceCheckImage(userFaceReference, imageValidate.getImageRef());
        log.info("保存更新人脸识别对比照片结果后发送邮件通知用户做人脸识别：userId:{} transId:{}, faceSave:{}", userId, transId, faceSave);
        boolean saveResult = false;
        if (faceSave && StringUtils.isNotBlank(userFaceReference.getCheckImage())) {
            log.info("由于KYC已经通过了，如果保存检查照片成功的话，直接保存更新到业务通过的照片信息中");
            // 上面会保存checkImage
            userFaceReference.setRefImage(userFaceReference.getCheckImage());
            userFaceReference.setRefImageName(userFaceReference.getCheckImage());
            userFaceReference.setUpdateTime(DateUtils.getNewUTCDate());
            int row = userFaceReferenceMapper.updateRefImage(userFaceReference);
            saveResult = row > 0;
        }
        return faceSave && saveResult;
    }

    @Override
    public FaceWebResultResponse webFaceResultHandler(String sign, String data) {
        APIResponse<FaceWebResultResponse> response = faceIdApi.webFaceResultHandler(APIRequest.instance(new FaceIdSignRequest(data, sign)));
        log.info("解析Web PC 端人脸识别结果信息:{}", JSON.toJSONString(response));
        if (response == null || response.getStatus() != APIResponse.Status.OK) {
            log.info("解析Web PC 端人脸验证结果异常. sign:{} data:{}", sign, data);
            FaceWebResultResponse result = new FaceWebResultResponse();
            result.setSuccess(false);
            return result;
        }
        return response.getData();
    }

    /**
     * 根据域名和API 组织队以ing的URL路径
     * @param baseUrl 允许空，当空时取默认配置值
     * @param api
     * @param language
     * @return
     */
    @Override
    public String getFaceApiPath(String baseUrl, String api, LanguageEnum language) {
        String faceDomain = baseUrl;
        if (StringUtils.isNotBlank(baseUrl)) {
            if (StringUtils.endsWith(baseUrl, "/")) {
                faceDomain = baseUrl.substring(0, baseUrl.length() -1);
            }
        }
        if (StringUtils.isBlank(faceDomain)) {
            if (LanguageEnum.ZH_CN == language) {
                faceDomain = commonConfig.getFaceDomainCn();
            }else {
                faceDomain = commonConfig.getFaceDomainEn();
            }
        }
        String url = faceDomain + api;
        if (StringUtils.containsIgnoreCase(url, LOCAL_PATH)) {
            // 如果存在有语言到路径信息，需要替换语言信息
            url = StringUtils.replace(url, LOCAL_PATH, language.getLang());
        }
        return url;
    }

    @Override
    public FaceWebSyncImageResponse webFaceSyncImageHandler(Long userId, String bizNo) {
        FaceWebSyncImageRequest imageRequest = new FaceWebSyncImageRequest(userId, bizNo);
        APIResponse<FaceWebSyncImageResponse> imageResponse = faceIdApi.webFaceSyncImageHandler(APIRequest.instance(imageRequest));
        log.info("获取Web PC Face 照片结果: userId:{} bizNo:{}, result:{}", userId, bizNo, JSON.toJSONString(imageResponse));
        if (imageResponse.getStatus() == APIResponse.Status.OK) {
            return imageResponse.getData();
        }
        return null;
    }

    @Override
    public FaceCaptureResponse faceImageCapture(Long userId, String imagePath, boolean imageScale, FaceTransType faceTransType) {
        if (userId == null || StringUtils.isBlank(imagePath)) {
            log.info("face image capture request but params miss. userId:{} imagePath:{}", userId, imagePath);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        // 对图片进行截取，需要进行放缩后进行截取
        FaceImageCaptureRequest request = new FaceImageCaptureRequest(userId, imagePath, imageScale);
        APIResponse<FaceCaptureResponse> response = faceIdApi.faceImageCaptureByPath(APIRequest.instance(request));
        if (response.getStatus() != APIResponse.Status.OK) {
            log.info("截取照片中的人脸信息失败. userId:{} imagePath:{} response:{}", userId, imagePath, JSON.toJSONString(response));
            return null;
        }
        return response.getData();
    }


    @Override
    public String initFaceSdkQrCode(Long userId, String transId, FaceTransType transType) {
        if (userId == null || StringUtils.isBlank(transId) || transType == null) {
            //初始化SDK的人脸识别二维码信息参数信息错误
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        //根据用户ID和业务编号业务类型，MD5生成一个串，当作qaCode也当作缓存中的key值进行
//        String temp = String.format("%d.%s.%s", userId, transId, transType.name());
//        String qrCode = DigestUtils.md5Hex(temp);
        String qrCode = UUID.randomUUID().toString().replaceAll("-", "");

        String cacheKey = String.format(FACE_SDK_QACODE_VALID_CACHE, qrCode);
        //先删除历史的内容然后再放入新内容
        RedisCacheUtils.del(cacheKey);
        //缓存一个小时，一个小时后失效
        long timeOut = commonConfig.getQrCodeValidSecond();

        String cacheValue = JSON.toJSONString(new FaceSdkCache(userId, transId, transType));
        RedisCacheUtils.set(cacheKey, cacheValue, timeOut);
        // 返回的格式串: nav/face/qrcode
        return String.format("nav/face/%s", qrCode);
    }

    @Override
    public FaceSdkCache validFaceSdkCacheByQrCode(String qrCode){
    	if (StringUtils.isBlank(qrCode)) {
            return null;
        }
        if(qrCode.startsWith(AccountConstants.KYC_FL_PREFIX)) {
        	String[] splits = qrCode.split(":",-1);
        	if(splits == null || splits.length != 3) {
        		return null;
        	}
        	//TODO
        	String transId = splits[2];
        	FaceTransType transType = FaceTransType.getByCode(splits[1]);
        	if(transType == null) {
        		return null;
        	}
        	TransactionFaceLog log = transactionFaceLogMapper.findByTransId(transId,transType.name());
        	if(log == null) {
        		return null;
        	}
            FaceSdkCache faceSdkCache = new FaceSdkCache(log.getUserId(), transId, transType);
        	return faceSdkCache;
        }

        String cacheKey = String.format(FACE_SDK_QACODE_VALID_CACHE, qrCode);
        try {
            String cacheValue = RedisCacheUtils.get(cacheKey, String.class);
            if (StringUtils.isBlank(cacheValue)) {
                return null;
            }
            cacheKey = String.format(FACE_SDK_QACODE_CACHE, qrCode);

            //先删除历史的内容然后再放入新内容
            RedisCacheUtils.del(cacheKey);
            //缓存一个小时，一个小时后失效
            long timeOut = 60 * 60L;
            RedisCacheUtils.set(cacheKey, cacheValue, timeOut);

            return JSON.parseObject(cacheValue, FaceSdkCache.class);
        }catch (Exception e) {
            log.error("根据qrCode获取缓存值失败. qrCode:{} ", qrCode, e);
            return null;
        }
    }

    @Override
    public FaceSdkCache getFaceSdkCacheByQrCode(String qrCode) {
        if (StringUtils.isBlank(qrCode)) {
            return null;
        }
        if(qrCode.startsWith(AccountConstants.KYC_FL_PREFIX)) {
        	String[] splits = qrCode.split(":",-1);
        	if(splits == null || splits.length != 3) {
        		return null;
        	}
        	//TODO
        	String transId = splits[2];
        	FaceTransType transType = FaceTransType.getByCode(splits[1]);
        	if(transType == null) {
        		return null;
        	}
        	TransactionFaceLog log = transactionFaceLogMapper.findByTransId(transId,transType.name());
        	if(log == null) {
        		return null;
        	}
            FaceSdkCache faceSdkCache = new FaceSdkCache(log.getUserId(), transId, transType);
        	return faceSdkCache;
        }

        String cacheKey = String.format(FACE_SDK_QACODE_CACHE, qrCode);
        try {
            String cacheValue = RedisCacheUtils.get(cacheKey, String.class);
            if (StringUtils.isBlank(cacheValue)) {
                return null;
            }
            return JSON.parseObject(cacheValue, FaceSdkCache.class);
        }catch (Exception e) {
            log.error("根据qrCode获取缓存值失败. qrCode:{} ", qrCode, e);
            return null;
        }
    }

    @Override
    public FaceSDKVerifyResponse faceSdkVerify(Long userId, String transId, FaceTransType transType, String imageRef, boolean imageRefScale, FaceSdkVerifyRequest verifyRequest) {
        try {
            FaceSDKVerifyRequest param = new FaceSDKVerifyRequest();
            param.setUserId(userId);
            param.setTransId(transId);
            param.setTransType(transType);
            param.setImageRef(imageRef);
            param.setImageRefScale(imageRefScale);
            param.setDelta(verifyRequest.getDelta());
            param.setImageBest(verifyRequest.getImageBest());
            param.setImageEnv(verifyRequest.getImageEnv());
            param.setImageAction1(verifyRequest.getImageAction1());
            param.setImageAction2(verifyRequest.getImageAction2());
            param.setImageAction3(verifyRequest.getImageAction3());
            APIRequestHeader header = WebUtils.getAPIRequestHeader();
            APIResponse<FaceSDKVerifyResponse> result = faceIdApi.sdkVerify(APIRequest.instance(header, param));
            log.info("FACE SDK userId:{} transId:{} result:{}", userId, transId, JSON.toJSONString(result));
            if (result.getStatus() != APIResponse.Status.OK) {
                //出现系统异常信息
                String errorMessage = result.getErrorData() == null ? "系统未知异常" : result.getErrorData().toString();
                log.warn("SDK 端人脸识别出现系统错误. userId:{} transId:{} {}", userId, transId, errorMessage);
                throw new BusinessException(GeneralCode.SYS_ERROR, errorMessage);
            }else {
                return result.getData();
            }
        }catch (BusinessException e) {
            throw e;
        }catch (Exception e) {
            log.error("SDK 端人脸识别验证失败. userId:{} transId:{}", userId, transId, e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    @Override
    public ImageCanDoFaceResponse validateFaceImageCanUsed(Long userId, String transId, String image, boolean imageScale, boolean saveLog) {
        if (userId == null || StringUtils.isAnyBlank(transId, image)) {
            log.info("请求参数错误. userId:{} transId:{} image:{}", userId, transId, image);
            return null;
        }
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            APIRequestHeader header = WebUtils.getAPIRequestHeader();
            ImageCanDoFaceRequest request = new ImageCanDoFaceRequest();
            request.setUserId(userId);
            request.setTransId(transId);
            request.setImage(image);
            request.setImageScale(imageScale);
            request.setSaveFaceLog(saveLog);
            APIResponse<ImageCanDoFaceResponse> response = faceIdApi.validateImageCanDoFace(APIRequest.instance(header, request));
            stopWatch.stop();
            String logStr = LogMaskUtils.maskJsonString(JSON.toJSONString(response), "imageRef");
            log.info("请求验证照片是否能做人脸识别结果: userId:{} transId:{} response:{} times:{}", userId, transId, logStr, stopWatch.getTotalTimeSeconds());
            if (response.getStatus() != APIResponse.Status.OK) {
                //验证照片是否能做人脸识别出现错误.
                log.info("验证照片是否能做人脸识别失败. userId:{} transId:{}", userId, transId);
                return null;
            }else {
                return response.getData();
            }
        }catch (Exception e) {
            log.error("请求验证照片是否能做人脸识别结果异常: userId:{} transId:{}", userId, transId, e);
            return null;
        }
    }

    @AllArgsConstructor
    @Setter
    @Getter
    public static class FaceSdkCache extends ToString {
        private static final long serialVersionUID = -2742433438226179192L;
        private Long userId;
        private String transId;
        private FaceTransType transType;
    }

    /**
     * 不同人脸识别业务对应的验证成功和失败的
     * @param transType
     * @param success
     * @return
     */
    @Override
    public FaceTransTypeContentVo getTransTypeContent(FaceTransType transType, boolean success) {
        //目前全部是使用默认的
        FaceTransTypeContentVo contentVo;
        if (transType == null) {
            return getDefault(success);
        }
        switch (transType) {
            case KYC_COMPANY:
                if (success) {
                    contentVo = typeContentVoBuild(TRANSTYPE_SUCCESS_TITLE_DEFAULT, TRANSTYPE_SUCCESS_CONTENT_KYC_COMPANY);
                }else {
                    contentVo = typeContentVoBuild(TRANSTYPE_SUCCESS_TITLE_DEFAULT, TRANSTYPE_FAIL_CONTENT_KYC);
                }
                break;
            case KYC_USER:
                if (success) {
                    contentVo = typeContentVoBuild(TRANSTYPE_SUCCESS_TITLE_DEFAULT, TRANSTYPE_SUCCESS_CONTENT_KYC_USER);
                }else {
                    contentVo = typeContentVoBuild(TRANSTYPE_SUCCESS_TITLE_DEFAULT, TRANSTYPE_FAIL_CONTENT_KYC);
                }
                break;
            case WITHDRAW_FACE:
                if (success) {
                    contentVo = typeContentVoBuild(TRANSTYPE_SUCCESS_TITLE_DEFAULT, TRANSTYPE_SUCCESS_CONTENT_WITHDRAWFACE);
                }else {
                    contentVo = typeContentVoBuild(TRANSTYPE_FAIL_TITLT_DEFAULT, TRANSTYPE_FAIL_CONTENT_DEFAULT);
                }
                break;
            default:
                contentVo = getDefault(success);
                break;
        }
        return contentVo;
    }

    private FaceTransTypeContentVo getDefault(boolean success) {
        if (success) {
            return typeContentVoBuild(TRANSTYPE_SUCCESS_TITLE_DEFAULT, TRANSTYPE_SUCCESS_CONTENT_DEFAULT);
        }else {
            return typeContentVoBuild(TRANSTYPE_FAIL_TITLT_DEFAULT, TRANSTYPE_FAIL_CONTENT_DEFAULT);
        }
    }

    private FaceTransTypeContentVo typeContentVoBuild(String titleKey, String contentKey) {
        LanguageEnum language;
        try {
        	String lang = messageUtils.getLanguage();
        	language = LanguageEnum.findByLang(lang);
        }catch(Exception e) {
        	log.warn("获取语言信息异常.",e);
        	language = LanguageEnum.EN_US;
        }
        String title = MessageMapHelper.getMessage(titleKey, language);
        String content = MessageMapHelper.getMessage(contentKey, language);
        return new FaceTransTypeContentVo(title, content);
    }

    @Override
    public int getFaceLogDailyTimes(Long userId, String transId, FaceTransType transType, String source) {
        if (userId == null || StringUtils.isBlank(transId) || transType == null) {
            return 0;
        }
        Date endTime = DateUtils.getNewUTCDate();
        Date startTime = DateUtils.add(endTime, Calendar.DATE, -1);
        FaceVerificationCountRequest countRequest = new FaceVerificationCountRequest();
        countRequest.setUserId(userId);
        countRequest.setTransId(transId);
        countRequest.setFaceTransType(transType);
        countRequest.setStartDate(startTime);
        countRequest.setEndDate(endTime);
        countRequest.setSource(source);
        return faceLogTimes(countRequest);
    }

    private int faceLogTimes(FaceVerificationCountRequest countRequest) {
        Long userId = countRequest.getUserId();
        String transId = countRequest.getTransId();
        try {
            APIResponse<Integer> response = faceIdApi.transVerificationCount(APIRequest.instance(countRequest));
            if (response.getStatus() != APIResponse.Status.OK) {
                log.info("获取人脸识别验证次数失败, userId:{} transId:{}", userId, transId);
                return 0;
            }else {
                return response.getData() == null ? 0 : response.getData();
            }
        }catch (Exception e) {
            log.error("获取人脸识别验证次数异常. userId:{} transId:{}", userId, transId, e);
            return 0;
        }
    }

    @Override
    public int getFaceLogTimes(Long userId, String transId, FaceTransType transType, FaceStatus faceStatus, FaceErrorCode faceErrorCode) {
        if (userId == null) {
            return 0;
        }
        FaceVerificationCountRequest countRequest = new FaceVerificationCountRequest();
        countRequest.setUserId(userId);
        countRequest.setTransId(transId);
        countRequest.setFaceTransType(transType);
        countRequest.setFaceStatus(faceStatus);
        countRequest.setFaceErrorCode(faceErrorCode);
        return faceLogTimes(countRequest);
    }

    @Override
    public FaceLogVo getFaceLogByBizNo(Long userId, String transId, String bizNo) {
        if (userId == null || StringUtils.isAnyBlank(transId, bizNo)) {
            return null;
        }
        try {
            GetFaceLogRequest request = new GetFaceLogRequest();
            request.setUserId(userId);
            request.setTransId(transId);
            request.setBizNo(bizNo);
            APIResponse<FaceLogVo> response = faceIdApi.getLastFaceLog(APIRequest.instance(request));
            if (response == null || response.getStatus() != APIResponse.Status.OK) {
                return null;
            }
            return response.getData();
        }catch (Exception e) {
            log.error("获取人脸某一笔业务信息失败. userId:{} transId:{} bizNo:{}", userId, transId, bizNo);
            return null;
        }
    }

    @Override
    public List<FaceLogVo> getFaceLogsByUser(Long userId, String transId, FaceTransType transType, FaceStatus faceStatus) {
        if (userId == null) {
            return Collections.emptyList();
        }
        try {
            GetFaceLogRequest request = new GetFaceLogRequest();
            request.setUserId(userId);
            request.setTransId(transId);
            request.setTransType(transType);
            request.setFaceStatus(faceStatus);
            APIResponse<List<FaceLogVo>> response = faceIdApi.getFaceLogsByUser(APIRequest.instance(request));
            if (response == null || response.getStatus() != APIResponse.Status.OK) {
                log.warn("获取用户人脸识别流水信息失败. userId:{} transId:{} transType:{}", userId, transId, transType);
                return Collections.emptyList();
            }else {
                return response.getData();
            }
        }catch (Exception e) {
            log.error("获取用户人脸识别流水信息异常. userId:{} transId:{} transType:{}", userId, transId, transType, e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getFaceOcrTimes(Long userId, FaceOcrUploadType type, Date startTime, Date endTime) {
        try {
            // 查询inspect face ocr 提交次数
            IdCardOcrRequest request = new IdCardOcrRequest();
            request.setUserId(userId);
            request.setType(type);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
            APIResponse<IdCardOcrResponse> response = faceIdApi.countFaceIdCard(APIRequest.instance(request));
            if (response == null || response.getStatus() != APIResponse.Status.OK) {
                String message = response == null ? null : response.getErrorData() == null ? null : response.getErrorData().toString();
                throw new BusinessException(GeneralCode.SYS_ERROR, message == null ? "System Error" : message);
            }
            return response.getData() == null ? 0 : response.getData().getCount();
        }catch (Exception e) {
            log.error("获取用户提交Face ocr 提交次数异常. userId:{}, type:{}", type, userId, e);
            return 0;
        }
    }

    @Override
    public IdCardOcrResponse faceOcrValidate(FaceOcrSubmitRequest request) {
        IdCardOcrRequest ocrRequest = new IdCardOcrRequest();
        ocrRequest.setUserId(request.getUserId());
        ocrRequest.setType(FaceOcrUploadType.valueOf(request.getType()));
        ocrRequest.setFace(request.getFace());
        ocrRequest.setFront(request.getFront());
        ocrRequest.setBack(request.getBack());
        ocrRequest.setFaceFileKey(request.getFaceFileKey());
        ocrRequest.setFrontFileKey(request.getFrontFileKey());
        ocrRequest.setBackFileKey(request.getBackFileKey());
        APIRequestHeader apiRequestHeader = WebUtils.getAPIRequestHeader();
        APIResponse<IdCardOcrResponse> response = faceIdApi.faceIdCardOcr(APIRequest.instance(apiRequestHeader, ocrRequest));
        log.info("get face ocr result userId:{} result:{}", request.getUserId(), response == null ? null : response.getStatus());
        if (response != null && response.getStatus() == APIResponse.Status.OK) {
            return response.getData();
        }else {
            if (response == null) {
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            if(response.getErrorData() != null ) {
                String message = MessageMapHelper.getMessage(response.getErrorData().toString(), WebUtils.getAPIRequestHeader().getLanguage());
            	throw new BusinessException(response.getCode(),  message);
            }
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    @Override
    public FaceIdCardOcrVo getFaceIdCardOcr(Long userId) {
        if (userId == null) {
            return null;
        }
        APIResponse<FaceIdCardOcrVo> response = faceIdApi.getFaceIdCardOcr(APIRequest.instance(userId + ""));
        if (response == null || response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
            log.warn("get user ocr result fail. userId:{}", userId);
            return null;
        }
        return response.getData();
    }

	@Override
	public void resetFaceIdOcr(Long userId) {
		faceIdApi.resetFaceIdCard(APIRequest.instance(userId+""));
	}

}
