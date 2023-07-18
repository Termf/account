package com.binance.account.controller.security;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.binance.account.vo.security.request.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserSecurityLogApi;
import com.binance.account.service.security.IUserSecurityLog;
import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.UserSecurityLogListResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

@RestController
public class UserSecurityLogController implements UserSecurityLogApi {

    @Resource
    private IUserSecurityLog iUserSecurityLog;

    @Override
    public APIResponse<GetUserSecurityLogResponse> getUserSecurityLogList(
            @Validated @RequestBody APIRequest<GetUserSecurityLogRequest> request) throws Exception {
        return this.iUserSecurityLog.getUserSecurityLogList(request);
    }

    @Override
    public APIResponse<UserSecurityLogVo> getLastLoginLog(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return this.iUserSecurityLog.getLastLoginLog(request);
    }

    @Override
    public APIResponse<GetUserSecurityLogResponse> getLogPage(@Validated @RequestBody APIRequest<UserSecurityRequest> request)  
            throws Exception {
        return this.iUserSecurityLog.getLogPage(request);
    }

	@Override
	public APIResponse<UserSecurityLogListResponse> getLogByIp(@Validated @RequestBody APIRequest<IpPageRequest> request)
			throws Exception {
		return this.iUserSecurityLog.getLogByIp(request);
	}

	@Override
	public APIResponse<List<Map<String, Object>>> getLogByIpCount(@Validated @RequestBody APIRequest<IpRequest> request) throws Exception {
		return this.iUserSecurityLog.getLogByIpCount(request);
	}

    @Override
    public APIResponse<Boolean> isBackendDisadbled(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(this.iUserSecurityLog.isBackendDisadbled(request.getBody().getUserId()));
    }

}
