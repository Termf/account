package com.binance.account.controller.reset2fa;

import com.binance.platform.monitor.logging.aop.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.Reset2FaApi;
import com.binance.account.service.reset2fa.IReset2Fa;
import com.binance.account.vo.reset.request.Reset2faNextStepRequest;
import com.binance.account.vo.reset.request.Reset2faStartValidatedRequest;
import com.binance.account.vo.reset.request.ResetResendEmailRequest;
import com.binance.account.vo.reset.request.ResetUploadInitRequest;
import com.binance.account.vo.reset.response.Reset2faNextStepResponse;
import com.binance.account.vo.reset.response.Reset2faStartValidatedResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

@Monitor
@RestController
public class Reset2FaController implements Reset2FaApi {

    @Autowired
    private IReset2Fa iReset2Fa;

    @Override
    public APIResponse<Reset2faStartValidatedResponse> reset2faStartValidated(@Validated @RequestBody APIRequest<Reset2faStartValidatedRequest> request) {
        return APIResponse.getOKJsonResult(iReset2Fa.reset2faStartValidated(request.getBody()));
    }

    @Override
    public APIResponse<Reset2faNextStepResponse> reset2faNextStepFlow(@Validated @RequestBody APIRequest<Reset2faNextStepRequest> request) {
        return APIResponse.getOKJsonResult(iReset2Fa.reset2faNextStepFlow(request.getBody()));
    }

    @Override
    public APIResponse<Reset2faNextStepResponse> reset2faUploadEmailOpen(@Validated @RequestBody APIRequest<ResetUploadInitRequest> request) {
        return APIResponse.getOKJsonResult(iReset2Fa.reset2faUploadEmailOpen(request.getBody()));
    }

	@Override
	public APIResponse<Reset2faNextStepResponse> sendEmailAgain(@Validated @RequestBody APIRequest<ResetResendEmailRequest> request) {
		return APIResponse.getOKJsonResult(iReset2Fa.sendEmailAgain(request.getBody()));
	}
}
