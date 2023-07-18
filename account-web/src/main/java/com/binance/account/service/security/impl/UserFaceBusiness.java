package com.binance.account.service.security.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.TransactionFaceQuery;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserFaceReferenceMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.face.FaceHandlerContext;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.TransactionFaceLogVo;
import com.binance.account.vo.face.request.FaceEmailRequest;
import com.binance.account.vo.face.request.FaceInitRequest;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.FaceReferenceRequest;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FacePcResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.FaceReferenceResponse;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.certification.api.UserFaceApi;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 人脸识别业务接口处理
 * @author liliang1
 * @date 2018-12-07 19:05
 */
@Log4j2
@Service
public class UserFaceBusiness implements IUserFace {

    @Resource
    private IFace iFace;
    @Resource
    private TransactionFaceLogMapper transactionFaceLogMapper;
    @Resource
    private UserFaceReferenceMapper userFaceReferenceMapper;
    @Resource
    private FaceHandlerContext faceHandlerContext;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserFaceApi userFaceApi;

    @Override
    public FaceInitResponse facePcInit(FaceInitRequest request) {
        String transId = request.getTransId();
        String type = request.getType();
        FaceTransType transType = FaceTransType.getByCode(type);
        if (StringUtils.isBlank(transId) || transType == null) {
            log.info("PC 端人脸识别初始化参数错误. transId:{} type:{}", transId, type);
            throw new BusinessException(AccountErrorCode.FACE_EMAIL_EXPIRED);
        }
//        FaceInitResponse response = faceHandlerContext.getFaceHandler(transType).facePcInit(transId, transType);
        FaceInitResponse response = faceHandlerContext.getFaceHandler(transType).facePrivateInit(transId, transType);
        log.info("PC 端人脸识别初始化结果: transId:{} initResult:{}", transId, JSON.toJSONString(response));
        return response;
    }

    @Override
    public String facePcVerify(String sign, String data) {
        log.info("PC 端人脸识别结果验证信息: sign:{} data:{}", sign, data);
        FaceWebResultResponse response = iFace.webFaceResultHandler(sign, data);
        log.info("PC 端人脸识别验证结果: sign:{} result:{}", sign, JSON.toJSONString(response));
        FaceTransType transType = response.getTransType();
        String transId = response.getTransId();
        FacePcResponse facePcResponse = faceHandlerContext.getFaceHandler(transType).facePcResultHandler(transId, transType, response);
        return facePcResponse.getRedirectPath();
    }

    @Override
	public void facePcVerifyPrivate(FacePcPrivateResult request) {
		log.info("PC 端私有云人脸识别结果信息 transId:{},request:{}",request.getTransId(),request);
		FaceTransType transType = FaceTransType.getByCode(request.getFaceTransType());
		faceHandlerContext.getFaceHandler(transType).facePcPrivateResultHandler(transType, request);
	}

    @Override
    public FaceInitResponse faceSdkInit(FaceInitRequest request) {
        String transId = request.getTransId();
        String type = request.getType();
        FaceTransType transType = FaceTransType.getByCode(type);
        if (transType == null) {
            log.info("SDK 端人脸识别初始化类型错误. transId:{} type:{}", transId, type);
            throw new BusinessException(AccountErrorCode.FACE_EMAIL_EXPIRED);
        }
        String prefix = AccountConstants.KYC_FL_PREFIX+":"+type+":";
        if(transId.startsWith(prefix)) {
        	transId  = transId.substring(transId.indexOf(prefix)+1);
        }
        //根据不同的业务类型，进行不同的业务判断是否能做人脸识别
        FaceInitResponse response = faceHandlerContext.getFaceHandler(transType).faceSdkInit(transId, transType);
        log.info("SDK 端人脸识别初始化结果: transId:{} initResult:{}", transId, JSON.toJSONString(response));
        return response;
    }

    @Override
    public FaceSdkResponse appFaceSdkVerify(FaceSdkVerifyRequest request) {
        String qrCode = request.getTransId();
        FaceBusiness.FaceSdkCache faceSdkCache = iFace.getFaceSdkCacheByQrCode(qrCode);
        FaceTransType faceTransType = faceSdkCache == null ? null : faceSdkCache.getTransType();
        Long userId = faceSdkCache == null ? null : faceSdkCache.getUserId();
        String transId = faceSdkCache == null ? null : faceSdkCache.getTransId();
        FaceSdkResponse response = faceHandlerContext.getFaceHandler(faceTransType).faceSdkResultHandler(transId, userId, faceTransType, request);
        log.info("SDK 端人脸识别验证的结果: userId:{} transId:{} result:{}", userId, transId, JSON.toJSONString(response));
        return response;
    }

    @Override
    public APIResponse<Void> appFaceSdkQrValid(String qrCode) {
        FaceBusiness.FaceSdkCache faceSdkCache = iFace.validFaceSdkCacheByQrCode(qrCode);
        if (faceSdkCache == null || StringUtils.isBlank(faceSdkCache.getTransId()) || faceSdkCache.getTransType() == null) {
            log.info("SDK 人脸识别QR验证获取业务信息值失败. qrCode:{}", qrCode);
            throw new BusinessException(AccountErrorCode.AC_RESET_FACE_SDK_QR_TIMEOUT);
        }
        // 获取操作类型和业务ID，(当前只有reset的，就不按业务类型分类处理了.)
        String transId = faceSdkCache.getTransId();
        FaceTransType transType = faceSdkCache.getTransType();
        Long userId = faceSdkCache.getUserId();
        if (transType == FaceTransType.RESET_APPLY_2FA || transType == FaceTransType.RESET_APPLY_UNLOCK) {
            APIResponse<Void> response = userFaceApi.faceSdkQrValid(APIRequest.instance(qrCode));
            checkFaceResponse(response);
        }else {
            // 验证当前业务是否能做人脸识别
            faceHandlerContext.getFaceHandler(transType).validateCanDoFace(userId, transId, transType);
        }
        log.info("SDK 人脸识别二维码验证通过. qrCode:{}", qrCode);
        return APIResponse.getOKJsonResult();
    }

    private <T> T checkFaceResponse(APIResponse<T> apiResponse) {
        if (apiResponse == null || apiResponse.getStatus() != APIResponse.Status.OK) {
            if (apiResponse == null) {
                throw new BusinessException(GeneralCode.COMMON_ERROR);
            }else {
                throw new BusinessException(apiResponse.getCode(), apiResponse.getErrorData().toString(), apiResponse.getParams());
            }
        }
        return apiResponse.getData();
    }

    @Override
    public boolean isFacePassed(FaceInitRequest body) {
        if (body == null || StringUtils.isAnyBlank(body.getTransId(), body.getType())) {
            log.info("请求参数错误. ");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String transId = body.getTransId();
        FaceTransType faceTransType = FaceTransType.getByCode(body.getType());
        if (faceTransType == null) {
            log.info("请求参数的人脸识别业务类型错误. transId:{} type:{}", transId, body.getType());
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (faceTransType == FaceTransType.RESET_APPLY_2FA || faceTransType == FaceTransType.RESET_APPLY_UNLOCK) {
            com.binance.certification.request.face.FaceInitRequest request = new com.binance.certification.request.face.FaceInitRequest();
            request.setTransId(body.getTransId());
            request.setType(body.getType());
            APIResponse<Boolean> response = userFaceApi.isFacePassed(APIRequest.instance(request));
            checkFaceResponse(response);
            return response.getData();
        }else {
            boolean result = faceHandlerContext.getFaceHandler(faceTransType).isFacePassed(transId, faceTransType);
            log.info("业务当前人脸识别状态是否通过的结果: transId:{} type:{} result:{}", transId, faceTransType, result);
            return result;
        }
    }

    @Override
    public boolean saveUserFaceReferenceCheckImage(FaceReferenceRequest faceReferenceRequest) {
        if (faceReferenceRequest == null || faceReferenceRequest.getUserId() == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (faceReferenceRequest.getCheckImageData() == null || faceReferenceRequest.getCheckImageData().length <= 0) {
            log.info("检查照片的数据为空值. ");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserFaceReference userFaceReference = new UserFaceReference();
        userFaceReference.setUserId(faceReferenceRequest.getUserId());
        userFaceReference.setSourceType(faceReferenceRequest.getSourceType());
        userFaceReference.setSourceImage(faceReferenceRequest.getSourceImage());
        userFaceReference.setSourceScale(faceReferenceRequest.getSourceScale());
        userFaceReference.setNeedScale(false);
        userFaceReference.setRefQuality(faceReferenceRequest.getRefQuality());
        userFaceReference.setQualityThreshold(faceReferenceRequest.getQualityThreshold());
        userFaceReference.setOrientation(faceReferenceRequest.getOrientation());
        boolean faceSave = iFace.saveFaceReferenceCheckImage(userFaceReference, faceReferenceRequest.getCheckImageData());
        log.info("保存用户的对比照片检查字段信息结果: userId:{} result:{}", faceReferenceRequest.getUserId(), faceSave);
        return faceSave;
    }

    @Override
    public SearchResult<TransactionFaceLogVo> getTransactionFaceLogs(TransactionFaceQuery query) {
        SearchResult<TransactionFaceLogVo> result = new SearchResult<>(Collections.emptyList(), 0L);
        if (query == null) {
            return result;
        }
        // email 不为null时转换到userId
        if (StringUtils.isNotBlank(query.getEmail())) {
            User user = userMapper.queryByEmail(query.getEmail());
            if (user == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            query.setUserId(user.getUserId());
        }

        long rows = transactionFaceLogMapper.getTransactionFaceLogsCount(query);
        if (rows <= 0) {
            return result;
        }
        List<TransactionFaceLog> faceLogs = transactionFaceLogMapper.getTransactionFaceLogs(query);
        if (faceLogs == null || faceLogs.isEmpty()) {
            return result;
        }
        List<TransactionFaceLogVo> voList = new ArrayList<>();
        Set<Long> userIdSet = new HashSet<>();
        faceLogs.stream().forEach(item -> {
            TransactionFaceLogVo vo = new TransactionFaceLogVo();
            BeanUtils.copyProperties(item, vo);
            voList.add(vo);
            userIdSet.add(item.getUserId());
        });
        List<Long> userIds = new ArrayList<>(userIdSet);
        List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
        List<UserInfo> userInfos = userInfoMapper.selectUserInfoList(userIds);
        Map<Long, UserIndex> userIndexMap = userIndices.stream().collect(Collectors.toMap(UserIndex::getUserId, item -> item));
        Map<Long, UserInfo> userInfoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUserId, item -> item));
        voList.stream().forEach(item -> {
            UserIndex userIndex = userIndexMap.get(item.getUserId());
            if (userIndex != null) {
                item.setEmail(userIndex.getEmail());
            }
            UserInfo userInfo = userInfoMap.get(item.getUserId());
            if (userInfo != null) {
                item.setUserRemark(userInfo.getRemark());
            }
        });
        result.setRows(voList);
        result.setTotal(rows);
        return result;
    }

    @Override
    public void resendFaceEmail(FaceInitRequest body) {
        if (body == null || StringUtils.isAnyBlank(body.getTransId(), body.getType())) {
            log.info("请求参数错误. ");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String transId = body.getTransId();
        FaceTransType faceTransType = FaceTransType.getByCode(body.getType());
        if (faceTransType == null) {
            log.info("请求参数的人脸识别业务类型错误. transId:{} type:{}", transId, body.getType());
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        log.info("请求发送人脸识别通知邮件: transId:{} type:{}", transId, faceTransType);
        faceHandlerContext.getFaceHandler(faceTransType).resendFaceEmailByTransId(transId, faceTransType);
    }

    @Override
    public void resendFaceEmailByEmail(FaceEmailRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getEmail(), request.getType())) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String email = request.getEmail();
        FaceTransType faceTransType = FaceTransType.getByCode(request.getType());
        if (faceTransType == null) {
            log.info("请求参数的人脸识别业务类型错误. email:{} type:{}", email, request.getType());
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        log.info("请求发送人脸识别通知邮件: email:{} type:{}", email, faceTransType);
        faceHandlerContext.getFaceHandler(faceTransType).resendFaceEmailByEmail(email, faceTransType);
    }

    @Override
    public APIResponse<FaceReferenceResponse> getUserFaceReference(APIRequest<UserIdRequest> request) {
        if (request == null || request.getBody() == null) {
            return APIResponse.getErrorJsonResult("请求参数缺失");
        }
        Long userId = request.getBody().getUserId();
        UserFaceReference faceReference = userFaceReferenceMapper.selectByPrimaryKey(userId);
        if (faceReference == null) {
            return APIResponse.getErrorJsonResult("获取不到用户的人脸对比照信息");
        }
        FaceReferenceResponse response = new FaceReferenceResponse();
        BeanUtils.copyProperties(faceReference, response);
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public List<FaceReferenceResponse> getUserFaceReferenceByUserIds(GetUserListRequest request) {
        List<Long> userList = request.getUserIds();
        if (userList == null || userList.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserFaceReference> faceReferences = userFaceReferenceMapper.getListByUserIds(userList);
        if (faceReferences == null || faceReferences.isEmpty()) {
            return Collections.emptyList();
        }
        return faceReferences.stream()
                .map(item -> {
                    FaceReferenceResponse response = new FaceReferenceResponse();
                    BeanUtils.copyProperties(item, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 初始化人脸识别流程
     * @param transId
     * @param userId
     * @param faceTransType
     * @param isKycLockOne 是否为KYC单一锁定数据
     */
    @Override
    public FaceFlowInitResult initFaceFlowByTransId(String transId, Long userId, FaceTransType faceTransType, boolean needEmail, boolean isKycLockOne) {
        if (faceTransType == null || userId == null || StringUtils.isBlank(transId)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        return faceHandlerContext.getFaceHandler(faceTransType).initTransFace(transId, userId, faceTransType, needEmail, isKycLockOne);
    }

    /**
     * 用于业务通过时如果人脸识别未通过的状态下直接强制通过
     */
    @Override
    public void endTransFaceLogStatus(Long userId, String transId, FaceTransType faceTransType, TransFaceLogStatus status, String failReason) {
        if (StringUtils.isBlank(transId) || faceTransType == null || status == null) {
            return;
        }
        TransactionFaceLog faceLog = faceHandlerContext.getFaceHandler(faceTransType).getByMasterdb(transId, faceTransType);
        if (faceLog != null && !TransFaceLogStatus.isEndStatus(faceLog.getStatus())) {
            log.info("强制修改人脸识别业务流程的状态：userId:{} transId:{} type:{} status:{}",
                    userId, transId, faceTransType, status);
            TransactionFaceLog transactionFaceLog = new TransactionFaceLog();
            transactionFaceLog.setId(faceLog.getId());
            transactionFaceLog.setUserId(faceLog.getUserId());
            transactionFaceLog.setStatus(status);
            transactionFaceLog.setFailReason(failReason);
            transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
            transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
        }
    }

    @Override
    public void faceImageErrorRedoUpload(String transId, Long userId, FaceTransType faceTransType, boolean isLockOne) {
        if (faceTransType == null || userId == null || StringUtils.isBlank(transId)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        faceHandlerContext.getFaceHandler(faceTransType).faceImageErrorRedoUpload(transId, userId, faceTransType, isLockOne);
    }

    @Override
    public void transFaceAudit(TransFaceAuditRequest auditRequest) {
        if (auditRequest == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        FaceTransType transType = FaceTransType.getByName(auditRequest.getTransType());
        if (transType == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String transId = auditRequest.getTransId();
        Long userId = auditRequest.getUserId();
        TransFaceLogStatus auditStatus = auditRequest.getStatus();
        log.info("人脸识别流程人工审核: userId:{} transId:{} auditStatus:{}", userId, transId, auditStatus);
        faceHandlerContext.getFaceHandler(transType).transFaceAudit(auditRequest, transType);
    }

}
