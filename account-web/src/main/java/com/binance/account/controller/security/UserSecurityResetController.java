package com.binance.account.controller.security;

import com.binance.account.api.UserSecurityResetApi;
import com.binance.account.common.query.ResetModularQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.IUserSecurityReset;
import com.binance.account.vo.face.request.FaceEmailRequest;
import com.binance.account.vo.reset.ResetAnswerLogVo;
import com.binance.account.vo.reset.UserSecurityResetVo;
import com.binance.account.vo.reset.request.ResetAnswerArg;
import com.binance.account.vo.reset.request.ResetApplyTimesArg;
import com.binance.account.vo.reset.request.ResetAuditArg;
import com.binance.account.vo.reset.request.ResetIdArg;
import com.binance.account.vo.reset.request.ResetLastArg;
import com.binance.account.vo.reset.request.ResetPendingArg;
import com.binance.account.vo.reset.request.ResetUserIdArg;
import com.binance.account.vo.reset.response.ResetAnswerRet;
import com.binance.account.vo.reset.response.ResetApplyTimesRet;
import com.binance.account.vo.reset.response.ResetIdRet;
import com.binance.account.vo.reset.response.ResetLastRet;
import com.binance.account.vo.security.request.SecurityResetFaceTokenRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.platform.monitor.logging.aop.Monitor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author liliang1
 * @date 2018-08-27 14:04
 */
@Monitor
@Log4j2
@RestController
public class UserSecurityResetController implements UserSecurityResetApi {

    @Autowired
    private IUserSecurityReset iUserSecurityReset;
    @Autowired
    private IUserFace iUserFace;

    @Override
    public APIResponse<Boolean> securityResetIsPending(@Validated @RequestBody APIRequest<ResetPendingArg> request) {
        ResetPendingArg body = request.getBody();
        Long userId = body.getUserId();
        String type = body.getType() == null ? null : body.getType().name().toUpperCase();
        return APIResponse.getOKJsonResult(iUserSecurityReset.securityResetIsPending(userId, type));
    }

    @Override
    public APIResponse sendResetEndStatusNotifyEmail(@Validated @RequestBody APIRequest<SecurityResetFaceTokenRequest> request) {
        SecurityResetFaceTokenRequest body = request.getBody();
        return iUserSecurityReset.sendResetEndStatusNotifyEmail(body.getId(), Long.valueOf(body.getUserId()));
    }

    @Override
    public APIResponse<ResetLastRet> getLastSecurityReset(@Validated @RequestBody APIRequest<ResetLastArg> request) {
        return iUserSecurityReset.getLastSecurityReset(request);
    }

    @Override
    public APIResponse<ResetIdRet> getResetById(@Validated @RequestBody APIRequest<ResetIdArg> request) {
        return iUserSecurityReset.getResetById(request);
    }

    @Override
    public APIResponse<?> sendInitResetEmail(@Validated @RequestBody APIRequest<ResetLastArg> request) {
        return iUserSecurityReset.sendInitResetEmail(request);
    }

    @Override
    public APIResponse<ResetAnswerRet> answerQuestion(@Validated @RequestBody APIRequest<ResetAnswerArg> request) {
        return iUserSecurityReset.answerQuestion(request);
    }

    @Override
    public APIResponse<?> cancelSecurityReset(@Validated @RequestBody APIRequest<ResetLastArg> request) {
        return iUserSecurityReset.cancelSecurityReset(request);
    }

    @Override
    public APIResponse<?> sendResetFaceEmail(@Validated @RequestBody APIRequest<ResetLastArg> request) {
        ResetLastArg resetLastArg = request.getBody();
        String email = resetLastArg.getEmail();
        FaceEmailRequest faceEmailRequest = new FaceEmailRequest();
        faceEmailRequest.setEmail(email);
        faceEmailRequest.setType(resetLastArg.getType().name());
        iUserFace.resendFaceEmailByEmail(faceEmailRequest);
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<List<ResetAnswerLogVo>> getResetAnswerLogs(@Validated @RequestBody APIRequest<ResetIdArg> request) {
        return APIResponse.getOKJsonResult(iUserSecurityReset.getResetAnswerLogs(request.getBody().getId()));
    }

    @Override
    public APIResponse<ResetApplyTimesRet> getResetApplyTimes(@Validated @RequestBody APIRequest<ResetApplyTimesArg> request) {
        return APIResponse.getOKJsonResult(iUserSecurityReset.getResetApplyTimes(request.getBody()));
    }

    @Override
    public APIResponse<UserSecurityResetVo> getVoById(@Validated @RequestBody APIRequest<ResetIdArg> request) {
        return APIResponse.getOKJsonResult(iUserSecurityReset.getVoById(request.getBody().getId()));
    }

    @Override
    public APIResponse<?> resetAudit(@Validated @RequestBody APIRequest<ResetAuditArg> request) {
        return iUserSecurityReset.resetAudit(request.getBody());
    }

    @Override
    public APIResponse<SearchResult<UserSecurityResetVo>> getResetList(@Validated @RequestBody APIRequest<ResetModularQuery> request) {
        return APIResponse.getOKJsonResult(iUserSecurityReset.getResetList(request.getBody()));
    }

    @Override
    public APIResponse<List<UserSecurityResetVo>> getUserAllReset(@Validated @RequestBody APIRequest<ResetUserIdArg> request) {
        return APIResponse.getOKJsonResult(iUserSecurityReset.getUserAllReset(request.getBody().getUserId()));
    }
}
