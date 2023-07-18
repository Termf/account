package com.binance.account.service.security;

import java.util.List;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.query.ResetModularQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.vo.reset.ResetAnswerLogVo;
import com.binance.account.vo.reset.UserSecurityResetVo;
import com.binance.account.vo.reset.request.ResetAnswerArg;
import com.binance.account.vo.reset.request.ResetApplyTimesArg;
import com.binance.account.vo.reset.request.ResetAuditArg;
import com.binance.account.vo.reset.request.ResetIdArg;
import com.binance.account.vo.reset.request.ResetLastArg;
import com.binance.account.vo.reset.response.ResetAnswerRet;
import com.binance.account.vo.reset.response.ResetApplyTimesRet;
import com.binance.account.vo.reset.response.ResetIdRet;
import com.binance.account.vo.reset.response.ResetLastRet;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * @author liliang1
 * @date 2018-08-22 20:35
 */
public interface IUserSecurityReset {

    /**
     * 查询用户的重置流程是否正在处理中
     * @param userId
     * @param type
     * @return
     */
    Boolean securityResetIsPending(Long userId, String type);

    /**
     * 审核通过或者拒绝是发送的通知邮件
     *
     * @param status
     * @param reset
     * @param message
     * @param msgLocal
     * @param msgParams
     */
    void sendResetAuthEmail(UserSecurityResetStatus status, UserSecurityReset reset, String message, boolean msgLocal, String... msgParams);

    /**
     * 发送reset 通过或者拒绝的邮件接口
     *
     * @param transId
     * @param userId
     * @return
     */
    @Deprecated
    APIResponse sendResetEndStatusNotifyEmail(String transId, Long userId);

    /**
     * 处理重置是否自动通过（内起线程独立处理）
     * @param userId
     * @param transId
     * @param ip
     * @param terminal
     */
    void autoPassResetHandler(final Long userId, final String transId, final String ip, final TerminalEnum terminal);

    /**
     * 重置流程的最近一次记录前端需要的信息
     * @param request
     * @return
     */
    APIResponse<ResetLastRet> getLastSecurityReset(APIRequest<ResetLastArg> request);

    /**
     * 根据ID获取重置流程中前端需要的信息
     * @param request
     * @return
     */
    APIResponse<ResetIdRet> getResetById(APIRequest<ResetIdArg> request);

    /**
     * 初始化2FA重置流程并且发送邮件
     * @param request
     * @return
     */
    APIResponse<Long> sendInitResetEmail(APIRequest<ResetLastArg> request);

    /**
     * 重置流程回答问题
     * @param request
     * @return
     */
    APIResponse<ResetAnswerRet> answerQuestion(APIRequest<ResetAnswerArg> request);

    /**
     * 取消重置流程
     * @param request
     * @return
     */
    APIResponse<?> cancelSecurityReset(APIRequest<ResetLastArg> request);

    /**
     * 查询重置流程的回答问题的结果信息
     * @param resetId
     * @return
     */
    List<ResetAnswerLogVo> getResetAnswerLogs(String resetId);

    /**
     * 查用户某一类型的重置流程申请的次数
     * @param arg
     * @return
     */
    ResetApplyTimesRet getResetApplyTimes(ResetApplyTimesArg arg);

    /**
     * 根据ID获取对象信息
     * @param id
     * @return
     */
    UserSecurityResetVo getVoById(String id);

    /**
     * 重置流程的审核
     * @param auditArg
     * @return
     */
    APIResponse<?> resetAudit(ResetAuditArg auditArg);

    /**
     * 查询重置流程列表信息
     * @param query
     * @return
     */
    SearchResult<UserSecurityResetVo> getResetList(ResetModularQuery query);

    /**
     * 查询用户所有的重置流程信息
     * @param userId
     * @return
     */
    List<UserSecurityResetVo> getUserAllReset(Long userId);

    /**
     * 强制从主库读取数据
     * @param resetId
     * @return
     */
    UserSecurityReset getFromMasterDbById(String resetId);
}
