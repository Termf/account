package com.binance.account.controller.certificate;

import javax.annotation.Resource;

import com.binance.account.common.query.CompanyCertificateQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserCertificateListRequest;
import com.binance.account.vo.certificate.CompanyCertificateVo;
import com.binance.account.vo.certificate.request.JumioIdNumberUseRequest;
import com.binance.account.vo.user.request.CompanyCertificateAuditRequest;
import com.binance.account.vo.user.response.JumioTokenResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserCertificateApi;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.kyc.KycApiTransferAdapter;
import com.binance.account.vo.certificate.UserCertificateVo;
import com.binance.account.vo.certificate.request.SaveCompanyCertificateRequest;
import com.binance.account.vo.certificate.request.SaveUserCertificateRequest;
import com.binance.account.vo.certificate.request.UserAuditCertificateResponse;
import com.binance.account.vo.certificate.request.UserDetectCertificateRequest;
import com.binance.account.vo.certificate.response.SaveUserCertificateResponse;
import com.binance.account.vo.certificate.response.UserDetectCertificateResponse;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;


@RestController
public class UserCertificateController implements UserCertificateApi {

    @Resource
    private IUserCertificate iUserCertificate;
    
    @Resource
    private KycApiTransferAdapter kycApiTransferAdapter;

    @Override
    public APIResponse<UserCertificateVo> getUserCertificateByUserId(
            @Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return this.iUserCertificate.getUserCertificateByUserId(request);
    }

    @Override
    public APIResponse<SaveUserCertificateResponse> saveUserCertificate(
            @Validated() @RequestBody() APIRequest<SaveUserCertificateRequest> request) throws Exception {
        return this.iUserCertificate.saveUserCertificate(request);
    }

    @Override
    public APIResponse<SaveUserCertificateResponse> uploadUserCertificate(
            @Validated() @RequestBody() APIRequest<SaveUserCertificateRequest> request) throws Exception {
        return this.iUserCertificate.uploadUserCertificate(request);
    }

    @Override
    public APIResponse<UserDetectCertificateResponse> userDetectCertificate(
            @Validated() @RequestBody() APIRequest<UserDetectCertificateRequest> request) throws Exception {
        return this.iUserCertificate.userDetectCertificate(request);
    }

    @Override
    public APIResponse<UserAuditCertificateResponse> userAuditCertificate(
            @Validated() @RequestBody() APIRequest<SaveUserCertificateRequest> request) throws Exception {
    	throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
//        return this.iUserCertificate.userAuditCertificate(request);
    }

    @Override
    public APIResponse<JumioTokenResponse> uploadCompanyCertificate(
            @Validated() @RequestBody() APIRequest<SaveCompanyCertificateRequest> request) throws Exception {
        return this.kycApiTransferAdapter.submitCompanyBaseInfo(request);
    }

    @Override
    public APIResponse<?> companyAuditCertificate(
            @Validated() @RequestBody() APIRequest<CompanyCertificateAuditRequest> request) throws Exception {
        return this.iUserCertificate.companyAuditCertificate(request);
    }


    @Override
    public APIResponse<CompanyCertificateVo> getCompanyCertificate(
            @Validated() @RequestBody() APIRequest<UserIdRequest> request) throws Exception {
        return this.iUserCertificate.getCompanyCertificate(request);
    }

    @Override
    public APIResponse<SearchResult<CompanyCertificateVo>> getCompanyCertificateList(
            @Validated() @RequestBody() APIRequest<CompanyCertificateQuery> request) throws Exception {
        return this.iUserCertificate.getCompanyCertificateList(request);
    }

    @Override
    public APIResponse<?> modifyCompanyCertificate(
            @Validated() @RequestBody() APIRequest<CompanyCertificateVo> request) throws Exception {
        return this.iUserCertificate.modifyCompanyCertificate(request);
    }

    @Override
    public APIResponse<Boolean> isJumioIdNumberUseByOtherUser(
            @Validated() @RequestBody() APIRequest<JumioIdNumberUseRequest> request) {
        JumioIdNumberUseRequest param = request.getBody();
        boolean result = this.iUserCertificate.isJumioIdNumberUseByOtherUser(param.getUserId(), param.getIdNumber(), param.getCountryCode(), param.getIdType());
        return APIResponse.getOKJsonResult(result);
    }

	@Override
	public APIResponse<SearchResult<UserCertificateVo>> listUserCertificate(@Validated @RequestBody APIRequest<UserCertificateListRequest> request)
			throws Exception {
		UserCertificateListRequest body = request.getBody();
		SearchResult<UserCertificateVo> result = this.iUserCertificate.listUserCertificate(body);
		if(result != null ) {
			return APIResponse.getOKJsonResult(result);
		}
		return APIResponse.getOKJsonResult();
		
	}
	
	@Override
    public APIResponse<?> refuseCompanyCertificate(
            @Validated() @RequestBody() APIRequest<CompanyCertificateAuditRequest> request) throws Exception {
        return this.iUserCertificate.refuseCompanyCertificate(request);
    }
}
