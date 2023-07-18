package com.binance.account.controller.security;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.binance.account.api.UserFaceApi;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.TransactionFaceQuery;
import com.binance.account.service.kyc.KycApiTransferAdapter;
import com.binance.account.service.kyc.KycFlowProcessFactory;
import com.binance.account.service.kyc.KycFlowProcessor;
import com.binance.account.service.security.IUserFace;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.TransactionFaceLogVo;
import com.binance.account.vo.face.request.FaceEmailRequest;
import com.binance.account.vo.face.request.FaceInitRequest;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.FacePcResultRequest;
import com.binance.account.vo.face.request.FaceReferenceRequest;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.request.TransFaceInitRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.kyc.request.FaceInitFlowRequest;
import com.binance.account.vo.kyc.response.KycFaceInitResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.FaceReferenceResponse;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.platform.monitor.logging.aop.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liliang1
 * @date 2018-12-11 16:06
 */
@Monitor
@RestController
public class UserFaceController implements UserFaceApi {

    @Autowired
    private IUserFace iUserFace;
    @Resource
    private KycFlowProcessFactory kycFlowProcessFactory;

    @Resource
    private KycApiTransferAdapter kycApiTransferAdapter;

    @Override
    public APIResponse<FaceInitResponse> facePcInit(@Validated @RequestBody APIRequest<FaceInitRequest> request) {
        return kycApiTransferAdapter.facePcInit(request);
    }

    @Override
    public APIResponse<FaceInitResponse> faceSdkInit(@Validated @RequestBody APIRequest<FaceInitRequest> request) {
        return kycApiTransferAdapter.faceSdkInit(request);
    }

    @Override
    public APIResponse<String> pcFaceVerify(@Validated @RequestBody APIRequest<FacePcResultRequest> request) {
        FacePcResultRequest resultRequest = request.getBody();
        return APIResponse.getOKJsonResult(iUserFace.facePcVerify(resultRequest.getSign(), resultRequest.getData()));
    }

	@Override
	public APIResponse<Void> pcFaceVerifyPrivate(@Validated @RequestBody APIRequest<FacePcPrivateResult> request) {
		return kycApiTransferAdapter.pcFaceVerifyPrivate(request);
	}

    @Override
    @SentinelResource(value = "/userFace/sdk/faceVerify")
    public APIResponse<FaceSdkResponse> appFaceSdkVerify(@Validated @RequestBody APIRequest<FaceSdkVerifyRequest> request) {
        return kycApiTransferAdapter.appFaceSdkVerify(request);
    }

    @Override
    public APIResponse<Void> faceSdkQrValid(@Validated @RequestBody APIRequest<String> request) {
        return iUserFace.appFaceSdkQrValid(request.getBody());
    }

    @Override
    public APIResponse<Boolean> isFacePassed(@Validated @RequestBody APIRequest<FaceInitRequest> request) {
        return APIResponse.getOKJsonResult(iUserFace.isFacePassed(request.getBody()));
    }

    @Override
    public APIResponse<Boolean> saveUserFaceReferenceCheckImage(@Validated @RequestBody APIRequest<FaceReferenceRequest> request) {
        FaceReferenceRequest faceReferenceRequest = request.getBody();
        return APIResponse.getOKJsonResult(iUserFace.saveUserFaceReferenceCheckImage(faceReferenceRequest));
    }

    @Override
    public APIResponse<SearchResult<TransactionFaceLogVo>> getTransactionFaceLogs(@Validated @RequestBody APIRequest<TransactionFaceQuery> request) {
        return APIResponse.getOKJsonResult(iUserFace.getTransactionFaceLogs(request.getBody()));
    }

    @Override
    public APIResponse<Void> resendFaceEmail(@Validated @RequestBody APIRequest<FaceInitRequest> request) {
        iUserFace.resendFaceEmail(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> resendFaceEmailByEmail(@Validated @RequestBody APIRequest<FaceEmailRequest> request) {
        iUserFace.resendFaceEmailByEmail(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<FaceReferenceResponse> getUserFaceReference(@Validated @RequestBody APIRequest<UserIdRequest> request) {
        return iUserFace.getUserFaceReference(request);
    }

    @Override
    public APIResponse<List<FaceReferenceResponse>> getUserFaceReferenceByUserIds(@Validated @RequestBody APIRequest<GetUserListRequest> request) {
        return APIResponse.getOKJsonResult(iUserFace.getUserFaceReferenceByUserIds(request.getBody()));
    }

    @Override
    public APIResponse<FaceFlowInitResult> initFaceFlowByTransId(@Validated @RequestBody APIRequest<TransFaceInitRequest> request) {
        TransFaceInitRequest transFaceInitRequest = request.getBody();
        String transId = transFaceInitRequest.getTransId();
        Long userId = transFaceInitRequest.getUserId();
        FaceTransType faceTransType = FaceTransType.getByCode(transFaceInitRequest.getType());
        // 兼容老的，在没有配置值时默认true
        boolean needEmail = transFaceInitRequest.getNeedEmail() == null ? true : transFaceInitRequest.getNeedEmail();
        // 老的版本按流程来的是false，默认使用false
        boolean isKycLockOne = transFaceInitRequest.isKycLockOne();
        if (isKycLockOne && (faceTransType == FaceTransType.KYC_COMPANY || faceTransType == FaceTransType.KYC_USER)) {
            // 如果是新版kyc锁定流程预建face流程，则需要从kyc的执行器中走
        	FaceInitFlowRequest flowRequest = new FaceInitFlowRequest();
            flowRequest.setUserId(userId);
            flowRequest.setKycType(faceTransType == FaceTransType.KYC_COMPANY ? KycCertificateKycType.COMPANY : KycCertificateKycType.USER);
            flowRequest.setTransId(transId);
            KycFlowResponse response = kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_KYC_FACE_INIT).process(flowRequest);
            KycFaceInitResponse faceInitResponse = (KycFaceInitResponse) response;
            FaceFlowInitResult result = new FaceFlowInitResult();
            result.setType(faceInitResponse.getTransType());
            result.setTransId(faceInitResponse.getTransId());
            return APIResponse.getOKJsonResult(result);
        }else {
            FaceFlowInitResult faceFlowInitResult = iUserFace.initFaceFlowByTransId(transId, userId, faceTransType, needEmail, false);
            return APIResponse.getOKJsonResult(faceFlowInitResult);
        }
    }

    @Override
    public APIResponse<Void> faceImageErrorRedoUpload(@Validated @RequestBody APIRequest<TransFaceInitRequest> request) {
        TransFaceInitRequest transFaceInitRequest = request.getBody();
        String transId = transFaceInitRequest.getTransId();
        Long userId = transFaceInitRequest.getUserId();
        FaceTransType faceTransType = FaceTransType.getByCode(transFaceInitRequest.getType());
        boolean isLockOne = transFaceInitRequest.isKycLockOne();
        iUserFace.faceImageErrorRedoUpload(transId, userId, faceTransType, isLockOne);
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> transFaceAudit(@Validated @RequestBody APIRequest<TransFaceAuditRequest> request) {
        iUserFace.transFaceAudit(request.getBody());
        return APIResponse.getOKJsonResult();
    }
}
