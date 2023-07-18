package com.binance.account.controller.certificate;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.binance.account.aop.MarginValidate;
import com.binance.account.api.UserKycApi;
import com.binance.account.common.query.JumioBizStatusQuery;
import com.binance.account.common.query.JumioQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserKycModularQuery;
import com.binance.account.mq.JumioInfoMsgListener;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.certificate.impl.UserChainAddressBusiness;
import com.binance.account.service.kyc.KycApiTransferAdapter;
import com.binance.account.vo.certificate.KycDetailResponse;
import com.binance.account.vo.certificate.request.KycForceToExpiredRequest;
import com.binance.account.vo.certificate.response.KycFormAddrResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.certificate.response.UserSimpleBaseInfoResponse;
import com.binance.account.vo.security.request.ChainAddressAnalyzeRequest;
import com.binance.account.vo.security.request.ChainAddressAuditRequest;
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
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;
import com.binance.platform.monitor.logging.aop.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Monitor
@RestController
public class UserKycController implements UserKycApi {
    @Resource
    private IUserKyc kyc;
    @Resource
    private JumioBusiness jumioBusiness;
    @Autowired
    private UserChainAddressBusiness userChainAddressBusiness;
    @Autowired
    private JumioInfoMsgListener jumioInfoMsgListener;
    @Resource
    private KycApiTransferAdapter kycApiTransferAdapter;

    @Override
    public APIResponse<UserKycCountryResponse> getKycCountry(@RequestBody() @Validated APIRequest<UserIdRequest> request) throws Exception {
    	return APIResponse.getOKJsonResult(kycApiTransferAdapter.getKycCountry(request.getBody().getUserId()));
    }

    @Override
    public APIResponse<UserKycApproveVo> getApproveUser(@RequestBody() @Validated APIRequest<UserIdRequest> request) throws Exception {
        return kyc.getApproveUser(request);
    }

    @Override
    public APIResponse<Boolean> checkUserWhetherPassKyc(@RequestBody() @Validated APIRequest<UserIdRequest> request) throws Exception {
        return kyc.checkUserWhetherPassKyc(request);
    }

    @Override
    public APIResponse<?> updateKycApprove(@RequestBody() @Validated APIRequest<UpdateKycApproveRequest> request) throws Exception {
        return kyc.updateKycApprove(request);
    }

    @Override
    public APIResponse<SearchResult<UserKycApproveVo>> getApproveList(@RequestBody() @Validated APIRequest<JumioQuery> request) throws Exception {
        return kyc.getApproveList(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<JumioTokenResponse> submitBaseInfo(@RequestBody() @Validated APIRequest<KycBaseInfoRequest> request) throws Exception {
        return kycApiTransferAdapter.submitUserBaseInfo(request);
    }

    @Override
    public APIResponse<InitSdkUserKycResponse> initSdkUserKyc(@RequestBody() @Validated APIRequest<KycBaseInfoRequest> request) throws Exception {
    	return kycApiTransferAdapter.initSdkUserKyc(request);
    }

    @Override
    public APIResponse<Void> saveJumioSdkScanRef(@RequestBody() @Validated APIRequest<SaveJumioSdkScanRefRequest> request) throws Exception {
        return kycApiTransferAdapter.saveJumioSdkScanRef(request);
    }

    @Override
    public APIResponse<SearchResult<UserKycVo>> getList(@RequestBody() @Validated APIRequest<JumioQuery> request) throws Exception {
        return kyc.getList(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<?> audit(@RequestBody() @Validated APIRequest<KycAuditRequest> request) throws Exception {
        return kyc.audit(request);
    }

    @Override
    public APIResponse<?> syncPhoto(@RequestBody() @Validated APIRequest<JumioQuery> request) throws Exception {
        return kyc.syncPhoto(request);
    }
    @Deprecated
    @Override
    public APIResponse<UserKycVo> getKycByUserId(@RequestBody() @Validated APIRequest<UserIdRequest> request) {
        return kycApiTransferAdapter.getKycByUserId(request);
    }

    @Override
    public APIResponse<?> getChainAddressAuditPage(@RequestBody() APIRequest<ChainAddressAuditRequest> request) {
        return userChainAddressBusiness.getChainAddressAuditPage(request);
    }

    @Override
    public APIResponse<?> submitChainAddressAudit(@RequestBody() @Validated APIRequest<ChainAddressAnalyzeRequest> request) {
        return userChainAddressBusiness.submitChainAddressAudit(request);
    }

    @Override
    public APIResponse<Boolean> isAddressInWhitelist(@RequestParam("address") String address) {
        return userChainAddressBusiness.isAddressInWhitelist(address);
    }


    @Override
    public APIResponse<?> auditChainAddress(@RequestBody() @Validated APIRequest<ChainAddressAuditRequest> request) {
        return userChainAddressBusiness.auditChainAddress(request);
    }

    @Override
    public APIResponse<SearchResult<UserKycVo>> getModularUserKycList(@RequestBody() @Validated APIRequest<UserKycModularQuery> request) {
        return APIResponse.getOKJsonResult(kyc.getModularUserKycList(request.getBody()));
    }

    @Override
    public APIResponse<String> queryJumioBizStatus(@RequestBody() @Validated APIRequest<JumioBizStatusQuery> request) {
        JumioBizStatus bizStatus = jumioBusiness.queryJumioBizStatus(request.getBody());
        String result = bizStatus == null ? "PENDING" : bizStatus.name();
        return APIResponse.getOKJsonResult(result);
    }

	@Override
	public APIResponse<UserKycVo> getUserKycById(@RequestBody() @Validated APIRequest<UserIdAndIdRequest> request) throws Exception {
		return kycApiTransferAdapter.getUserKycById(request);
	}

    @Override
    public APIResponse<?> syncJumioAuditResult(@Validated @RequestBody() APIRequest<String> request) {
        String message = request.getBody();
        if (StringUtils.isBlank(message)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        JumioInfoVo jumioInfoVo = JSON.parseObject(message, JumioInfoVo.class);
        if (jumioInfoVo == null) {
            //JUMIO 信息获取失败
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String result = jumioInfoMsgListener.execute(jumioInfoVo);
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    @SentinelResource(value = "/user/kyc/currentKycStatus")
    public APIResponse<KycDetailResponse> getCurrentKycStatus(@Validated @RequestBody() APIRequest<UserIdRequest> request) {
        return kycApiTransferAdapter.getCurrentKycStatus(request);
    }

    @Override
    public APIResponse<Void> forceKycPassedToExpired(@Validated() @RequestBody() APIRequest<KycForceToExpiredRequest> request) {
        return kycApiTransferAdapter.forceKycPassedToExpired(request);
    }

    @Override
    public APIResponse<Boolean> saveXfersKycData(@Validated @RequestBody APIRequest<UserKycVo> request) {
        return APIResponse.getOKJsonResult(kyc.saveXfersUserKyc(request.getBody()));
    }

    @Override
    public APIResponse<Boolean> updateXfersKycData(@Validated @RequestBody APIRequest<UserKycVo> request) {
        // TODO Auto-generated method stub
        return APIResponse.getOKJsonResult(kyc.updateXfersUserKyc(request.getBody()));
    }

    @Override
	public APIResponse<?> refuseApprove(@RequestBody() @Validated APIRequest<KycAuditRequest> request) throws Exception {
		return kyc.refuseApprove(request);
	}

    @Override
    public APIResponse<Boolean> submitSimpleBaseInfo(@Validated() @RequestBody() APIRequest<KycSimpleBaseInfoRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(kyc.submitSimpleBaseInfo(request.getBody()));
    }

    @Override
    public APIResponse<UserSimpleBaseInfoResponse> getSimpleBaseInfo(@Validated() @RequestBody() APIRequest<UserIdRequest> request) throws Exception {
        return kyc.getSimpleBaseInfo(request);
    }


    @Override
    public APIResponse<KycFormAddrResponse> getKycFormAddrByUserIds(@RequestBody() @Validated APIRequest<GetUserListRequest> request) throws Exception {
        return kyc.getKycFormAddrByUserIds(request.getBody());
    }
}
