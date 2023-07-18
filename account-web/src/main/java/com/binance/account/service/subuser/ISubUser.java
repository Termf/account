package com.binance.account.service.subuser;

import java.math.BigDecimal;
import java.util.List;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.SubAccountTransferResponse;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToMasterResponse;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToSubResponse;
import com.binance.account.vo.subuser.CreateNoEmailSubUserReq;
import com.binance.account.vo.subuser.SubUserBindingVo;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.subuser.request.BindingParentSubUserEmailReq;
import com.binance.account.vo.subuser.SubAccountTransferHistoryInfoVo;
import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.CreateSubUserReq;
import com.binance.account.vo.subuser.request.ModifySubAccountRequest;
import com.binance.account.vo.subuser.request.OpenOrCloseSubUserReq;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.QuerySubUserRequest;
import com.binance.account.vo.subuser.request.ResendSubUserRegisterMailReq;
import com.binance.account.vo.subuser.request.ResetSecondValidationRequest;
import com.binance.account.vo.subuser.request.SubAccountTransHisReq;
import com.binance.account.vo.subuser.request.SubAccountTransHistoryInfoReq;
import com.binance.account.vo.subuser.request.SubAccountTransferRequest;
import com.binance.account.vo.subuser.request.SubUserCurrencyBalanceReq;
import com.binance.account.vo.subuser.request.SubUserSearchReq;
import com.binance.account.vo.subuser.request.SubUserSecurityLogReq;
import com.binance.account.vo.subuser.request.SubUserAssetBtcRequest;
import com.binance.account.vo.subuser.request.SubUserTransferByTranIdReq;
import com.binance.account.vo.subuser.request.UpdatePassWordRequest;
import com.binance.account.vo.subuser.request.UpdateSubUserRemarkReq;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.BindingParentSubUserEmailResp;
import com.binance.account.vo.subuser.response.CreateSubUserResp;
import com.binance.account.vo.subuser.response.SubAccountResp;
import com.binance.account.vo.subuser.response.SubAccountTransferHistoryInfoResp;
import com.binance.account.vo.subuser.response.SubAccountTransferResp;
import com.binance.account.vo.subuser.response.SubUserAssetBtcResponse;
import com.binance.account.vo.subuser.response.SubUserCurrencyBalanceResp;
import com.binance.account.vo.subuser.response.SubUserEmailVoResp;
import com.binance.account.vo.subuser.response.SubUserInfoResp;
import com.binance.account.vo.subuser.response.SubUserTypeResponse;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.response.ResendSendActiveCodeResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
public interface ISubUser {

    /**
     * 是否开启母子账号功能
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> isSubUserFunctionEnabled(APIRequest<ParentUserIdReq> request) throws Exception;

    /**
     * 母账号注册子账号
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<CreateSubUserResp> createSubUser(APIRequest<CreateSubUserReq> request) throws Exception;


    boolean createAssetManagerSubUser(CreateAssetManagerSubUserReq body)throws Exception;


    /**
     * 母账号注册无邮箱子账号
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<CreateNoEmailSubUserResp> createNoEmailSubUser(APIRequest<CreateNoEmailSubUserReq> request) throws Exception;

    /**
     * 根据用户ID返回账号类型(普通|母|子)
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<SubUserTypeResponse> checkRelationByUserId(APIRequest<UserIdReq> request) throws Exception;

    /**
     * 根据母账户ID和子账户ID判断是否母子关系
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> checkRelationByParentSubUserIds(APIRequest<BindingParentSubUserReq> request) throws Exception;


    /**
     * 根据母账户ID和子账户email判断是否母子关系并且返回subUserId
     *
     * @param request
     * @return
     * @throws Exception
     */
    BindingParentSubUserEmailResp checkRelationByParentSubUserEmail(BindingParentSubUserEmailReq request) throws Exception;

    /**
     * 非子账户，或已被母账户启用的子账户(用户下单前检查，接口性能要求高)
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> notSubUserOrIsEnabledSubUser(APIRequest<UserIdReq> request) throws Exception;

    /**
     * 重置2fa
     *
     * @param request
     * @return
     */
    APIResponse<Integer> resetSecondValidation(APIRequest<ResetSecondValidationRequest> request) throws Exception;

    /**
     * 修改子账户密码
     *
     * @param request
     * @return
     */
    APIResponse<Integer> updateSubUserPwd(APIRequest<UpdatePassWordRequest> request) throws Exception;


    /**
     * 条件查询子账户
     *
     * @param request
     * @return
     */
    APIResponse<SubUserInfoResp> selectSubUserInfo(APIRequest<QuerySubUserRequest> request);

    /**
     * 母账户查询子账户列表(包含上面的接口)
     *
     * @param request
     * @return
     */
    APIResponse<List<SubAccountResp>> getSubAccountList(APIRequest<SubUserSearchReq> request);

    /**
     * 查母账户下的子账户邮箱集合
     *
     * @param request
     * @return
     */
    APIResponse<SubUserEmailVoResp> selectSubUserEmailList(APIRequest<ParentUserIdReq> request);

    /**
     * 子账户登录历史集合
     *
     * @param request
     * @return
     */
    APIResponse<GetUserSecurityLogResponse> loginHistoryList(APIRequest<SubUserSecurityLogReq> request);

    /**
     * 子母账户划转
     *
     * @param request
     * @return
     */
    APIResponse<SubAccountTransferResponse> subAccountTransfer(APIRequest<SubAccountTransferRequest> request) throws Exception;

    /**
     * 母账户修改子账户邮箱接口
     *
     * @param request
     * @return
     */
    APIResponse<Integer> modifySubAccount(APIRequest<ModifySubAccountRequest> request) throws Exception;

    /**
     * 母账户查询子母账户划转历史
     *
     * @param request
     * @return
     */
    APIResponse<List<SubAccountTransferResp>> getSubAccountTransferHistory(APIRequest<SubAccountTransHisReq> request) throws Exception;


    public User checkParentAndSubUserBinding(final Long parentUserId, final String subUserEmail) throws Exception;

    public List<UserInfo> checkParentAndGetSubUserInfoList(final Long parentUserId,String email,Integer isSubUserEnabled) throws Exception;

    public UserInfo checkParentAndGetUserInfo(final Long parentUserId) throws Exception;

    public APIResponse<SubAccountTransferVersionForSubToSubResponse> subAccountTransferVersionForSubToSub(APIRequest<SubAccountTransferVersionForSubToSubRequest> request) throws Exception ;

    public APIResponse<SubAccountTransferVersionForSubToMasterResponse> subAccountTransferVersionForSubToMaster(APIRequest<SubAccountTransferVersionForSubToMasterRequest> request) throws Exception ;

    public APIResponse<List<SubAccountTranHisResForSapiVersion>> subUserHistoryVersionForSapi(APIRequest<SubUserHistoryVersionForSapiRequest> request) throws Exception;


    /**
     * 母账户查询子母账户划转历史的详细信息
     * @param request
     * @return
     */
    APIResponse<SubAccountTransferHistoryInfoResp> getSubAccountTransferHistoryInfo(APIRequest<SubAccountTransHistoryInfoReq> request) throws Exception;

    /**
     * 母账户重发子账户激活邮件
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<ResendSendActiveCodeResponse> resendSubUserRegisterMail(APIRequest<ResendSubUserRegisterMailReq> request) throws Exception;

    /**
     * 子账户列表及子账户BTC总值
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<SubUserAssetBtcResponse> subUserAssetBtcList(APIRequest<SubUserAssetBtcRequest> request) throws Exception;

    /**
     * 查询母账号资产BTC总值
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<BigDecimal> parentUserAssetBtc(APIRequest<ParentUserIdReq> request) throws Exception;

    /**
     * 母账户下所有子账户总资产折合BTC数
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<BigDecimal> allSubUserAssetBtc(APIRequest<ParentUserIdReq> request) throws Exception;

    /**
     * 查询子账户相应币种的可用余额
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<List<SubUserCurrencyBalanceResp>> subUserCurrencyBalance(APIRequest<SubUserCurrencyBalanceReq> request) throws Exception;

    /**
     * 母账户根据tranId查询子母账户划转记录
     * @param request
     * @return
     */
    APIResponse<SubAccountTransferHistoryInfoVo> getSubUserTransferByTranId(APIRequest<SubUserTransferByTranIdReq> request) throws Exception;



    APIResponse<Integer> updateSubUserRemark(APIRequest<UpdateSubUserRemarkRequest> request) throws Exception;


    APIResponse<Boolean> subAccountFutureAssetTransfer(APIRequest<SubAccountFutureTransferReq> request)throws Exception;


    APIResponse<List<SubUserBindingVo>> queryFutureSubUserBinding(APIRequest<IdRequest> request);

}
