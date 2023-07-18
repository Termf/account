package com.binance.account.controller.kyc;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.binance.account.api.kyc.KycCertificateApi;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.query.KycCertificateQuery;
import com.binance.account.common.query.KycRefByNumberQuery;
import com.binance.account.common.query.KycRefQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.service.kyc.KycCertificateService;
import com.binance.account.service.kyc.KycFlowProcessFactory;
import com.binance.account.service.kyc.KycFlowProcessor;
import com.binance.account.vo.certificate.response.KycRefQueryByNumberResponse;
import com.binance.account.vo.certificate.response.KycRefQueryResponse;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.KycFillInfoHistoryVo;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.*;
import com.binance.account.vo.kyc.response.*;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.platform.monitor.logging.aop.Monitor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Monitor
@RestController
public class KycCertificateController implements KycCertificateApi {

    @Resource
    private KycFlowProcessFactory kycFlowProcessFactory;
    @Resource
    private KycCertificateService kycCertificateService;

    @Override
    public APIResponse<GetKycStatusResponse> getKycStatus(@Validated @RequestBody APIRequest<Long> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.getKycStatus(request.getBody()));
    }

    @Override
    @SentinelResource(value = "/kyc/certificate/baseInfoSubmit")
    public APIResponse<BaseInfoResponse> baseInfoSubmit(@Validated @RequestBody APIRequest<BaseInfoRequest> request) {
        BaseInfoRequest baseInfoRequest = request.getBody();
        baseInfoRequest.setOldApi(false);
        BaseInfoResponse response = kycCertificateService.baseInfoSubmit(baseInfoRequest);
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<BaseInfoResponse> getKycBaseInfo(@Validated @RequestBody APIRequest<GetBaseInfoRequest> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.getKycBaseInfo(request.getBody()));
    }

    @Override
    public APIResponse<AddressInfoSubmitResponse> addressInfoSubmit(@Validated @RequestBody APIRequest<AddressInfoSubmitRequest> request) {
        AddressInfoSubmitResponse response = kycCertificateService.addressInfoSubmit(request.getBody());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Void> addressAuthResult(@Validated @RequestBody APIRequest<AddresAuthResultRequest> request){
    	kycCertificateService.addressAuthResult(request.getBody());
    	return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> kycBindMobile(@Validated @RequestBody APIRequest<KycBindMobileRequest> request) {
        kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_KYC_BIND_MOBILE).process(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> kycSendSmsCode(@Validated @RequestBody APIRequest<KycBindMobileRequest> request) {
        kycCertificateService.kycSendSmsCode(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<JumioInitResponse> kycJumioInit(@Validated @RequestBody APIRequest<KycFlowRequest> request) {
    	return APIResponse.getOKJsonResult(kycCertificateService.kycJumioInit(request.getBody()));
    }

    @Override
    public APIResponse<KycFaceInitResponse> kycFaceInit(@Validated @RequestBody APIRequest<FaceInitFlowRequest> request) {
        KycFaceInitResponse response = (KycFaceInitResponse) kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_KYC_FACE_INIT).process(request.getBody());
        return APIResponse.getOKJsonResult(response);
    }


    @Override
    @SentinelResource(value = "/kyc/certificate/faceOcrSubmit")
    public APIResponse<FaceOcrSubmitResponse> kycFaceOcrSubmit(@Validated @RequestBody APIRequest<FaceOcrSubmitRequest> request) {
        FaceOcrSubmitResponse response =  kycCertificateService.kycFaceOcrSubmit(request.getBody());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
	public APIResponse<Void> idmAuthResult(@Validated @RequestBody APIRequest<IdmAuthRequest> request) {
		kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_IDM_AUTH_RESULT).process(request.getBody());
        return APIResponse.getOKJsonResult();
	}

    @Override
    public APIResponse<SearchResult<KycCertificateVo>> getKycCertificateList(@Validated @RequestBody APIRequest<KycCertificateQuery> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.getKycCertificateList(request.getBody()));
    }

    @Override
    public APIResponse<KycCertificateVo> getKycCertificateDetail(@Validated @RequestBody APIRequest<Long> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.getKycCertificateDetail(request.getBody(),false));
    }

    @Override
    public APIResponse<KycFillInfoVo> getKycFillInfo(@Validated @RequestBody APIRequest<GetBaseInfoRequest> request) {
        KycFillType fillType = request.getBody().getFillType();
        Long userId = request.getBody().getUserId();
        return APIResponse.getOKJsonResult(kycCertificateService.getKycFillInfo(userId, fillType));
    }

    @Override
    public APIResponse<List<KycFillInfoHistoryVo>> getKycFillInfoHistories(@Validated @RequestBody APIRequest<GetBaseInfoRequest> request) {
        KycFillType fillType = request.getBody().getFillType();
        Long userId = request.getBody().getUserId();
        return APIResponse.getOKJsonResult(kycCertificateService.getKycFillInfoHistories(userId, fillType));
    }

	@Override
	public APIResponse<KycCertificateVo> getKycCertificateFullDetail(@Validated @RequestBody APIRequest<Long> request) {
		return APIResponse.getOKJsonResult(kycCertificateService.getKycCertificateDetail(request.getBody(),true));
	}

	@Override
	public APIResponse<Boolean> syncFiatPtStatus(@Validated @RequestBody APIRequest<FiatKycSyncStatusRequest> request) {
		return APIResponse.getOKJsonResult(kycCertificateService.syncFiatPtStatus(request.getBody()));
	}

    @Override
    public APIResponse<SearchResult<KycRefQueryResponse>> kycRefQuery(@Validated @RequestBody APIRequest<KycRefQuery> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.kycRefQuery(request.getBody()));
    }

    @Override
    public APIResponse<SearchResult<KycRefQueryByNumberResponse>> kycRefQueryByNumber(@Validated @RequestBody APIRequest<KycRefByNumberQuery> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.kycRefQueryByNumber(request.getBody()));
    }

    @Override
    public APIResponse<DeleteKycNumberInfoResponse> deleteKycNumberInfo(@Validated @RequestBody APIRequest<DeleteKycNumberInfoRequest> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.deleteKycNumberInfo(request.getBody()));
    }

    @Override
    public APIResponse<KycFillInfoVo> additionalInfo(@Validated @RequestBody APIRequest<AdditionalInfoRequest> request) {
        return APIResponse.getOKJsonResult(kycCertificateService.additionalInfo(request.getBody()));
    }

    @Override
    public APIResponse<Void> changeAccountStatusAndLevel(@Validated @RequestBody APIRequest<KycAccountChangeRequest> request) {
        kycCertificateService.changeAccountStatusAndLevel(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> changeWithdrawFaceByKycPass(@Validated @RequestBody APIRequest<KycPassWithdrawFaceRequest> request) {
        kycCertificateService.changeWithdrawFaceByKycPass(request.getBody());
        return APIResponse.getOKJsonResult();
    }
}
