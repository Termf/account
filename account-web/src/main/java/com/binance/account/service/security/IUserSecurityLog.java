package com.binance.account.service.security;

import java.util.List;
import java.util.Map;

import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.security.request.*;
import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.UserSecurityLogListResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public interface IUserSecurityLog {

    /**
     * 获取用户安全日志
     *
     * @param request
     * @return
     * @throws Exception
     */
    public APIResponse<GetUserSecurityLogResponse> getUserSecurityLogList(APIRequest<GetUserSecurityLogRequest> request)
            throws Exception;

    /**
     * 获取最后登录日志
     *
     * @param request
     * @return
     * @throws Exception
     */
    public APIResponse<UserSecurityLogVo> getLastLoginLog(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 获取用户安全日志列表
     * @param request
     * @return
     */
    public APIResponse<GetUserSecurityLogResponse> getLogPage(APIRequest<UserSecurityRequest> request);

    /**
     * 新增日志（异步）
     */
    void addSecurityLogAsync(UserSecurityLog record);

    /**
     * 根据ip查询用户列表
     * @param request
     * @return
     */
	public APIResponse<UserSecurityLogListResponse> getLogByIp(APIRequest<IpPageRequest> request);

	/**
	 * 获取ip地址关联的用户数
	 * @param request
	 * @return
	 */
	public APIResponse<List<Map<String, Object>>> getLogByIpCount(APIRequest<IpRequest> request);



    public Boolean isBackendDisadbled(Long userId);
}
