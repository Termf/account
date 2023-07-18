package com.binance.account.controller.withdraw;

import com.binance.account.service.kyc.CertificateCenterDispatcher;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.withdraw.request.UserWithdrawLockLogRequest;
import com.binance.account.vo.withdraw.request.WithdrawFaceInHoursRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawFaceTipResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockLogResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserWithdrawPropertyApi;
import com.binance.account.common.validator.ValidateResult;
import com.binance.account.service.withdraw.IUserWithdrawPropertyBusiness;
import com.binance.account.vo.withdraw.request.UserWithdrawLockAmountRequest;
import com.binance.account.vo.withdraw.request.UserWithdrawLockRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawLockAmountResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockResponse;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class UserWithdrawPropertyController implements UserWithdrawPropertyApi {

	@Autowired
	private IUserWithdrawPropertyBusiness iUserWithdrawPropertyBusiness;
	@Autowired
	private CertificateCenterDispatcher certificateCenterDispatcher;

	@Override
	public APIResponse<UserWithdrawLockResponse> lock(@Validated @RequestBody APIRequest<UserWithdrawLockRequest> request) throws Exception{

		UserWithdrawLockRequest body = request.getBody();
		ValidateResult rs = body.validate();
        if (!rs.isOk()){
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), rs.getMessage());
        }
        UserWithdrawLockResponse response = iUserWithdrawPropertyBusiness.lock(body);
        return APIResponse.getOKJsonResult(response);
	}

	@Override
	public APIResponse<UserWithdrawLockResponse> unlock(@Validated @RequestBody APIRequest<UserWithdrawLockRequest> request) throws Exception{

		UserWithdrawLockRequest body = request.getBody();
		ValidateResult rs = body.validate();
        if (!rs.isOk()){
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), rs.getMessage());
        }
        UserWithdrawLockResponse response = iUserWithdrawPropertyBusiness.unlock(body);
        return APIResponse.getOKJsonResult(response);
	}

	@Override
	public APIResponse<UserWithdrawLockAmountResponse> getLockAmount(
			@RequestBody APIRequest<UserWithdrawLockAmountRequest> request) {

		UserWithdrawLockAmountRequest body = request.getBody();
		Long userId = body.getUserId();
		try {
			UserWithdrawLockAmountResponse response = iUserWithdrawPropertyBusiness.getLockAmount(userId);
        	return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
			log.warn("get user withdraw lock amount failed " + body.getUserId(),e);
			return APIResponse.getErrorJsonResult("get user withdraw lock amount failed," + e.getMessage());
		}
	}

	@Override
	public APIResponse<UserWithdrawFaceTipResponse> checkWithdrawFaceStatus(@Validated @RequestBody APIRequest<UserIdRequest> request) {
		Long userId = request.getBody().getUserId();
		return APIResponse.getOKJsonResult(certificateCenterDispatcher.checkWithdrawFaceStatus(userId));
	}

    @Override
    public APIResponse<Boolean> checkWithdrawFaceInHours(@Validated @RequestBody APIRequest<WithdrawFaceInHoursRequest> request) {
        return APIResponse.getOKJsonResult(certificateCenterDispatcher.checkWithdrawFaceInHours(request.getBody()));
    }

	@Override
	public APIResponse<UserWithdrawLockLogResponse> queryLockLog(@Validated @RequestBody APIRequest<UserWithdrawLockLogRequest> request) throws Exception {
		UserWithdrawLockLogResponse logResponse = iUserWithdrawPropertyBusiness.queryLockLog(request.getBody());
		return APIResponse.getOKJsonResult(logResponse);
	}
}
