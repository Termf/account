package com.binance.account.controller.subuser;

import java.util.HashMap;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.security.impl.UserSecurityBusiness;
import com.binance.account.service.subuser.ISubUserExtra;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.IUserFuture;
import com.binance.account.vo.security.request.CreateFutureAccountRequest;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToMasterResponse;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToSubResponse;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.service.user.impl.UserInfoBusiness;
import com.binance.account.util.BitUtils;
import com.binance.account.vo.security.request.*;
import com.binance.account.vo.subuser.CreateNoEmailSubUserReq;
import com.binance.account.vo.subuser.FuturePositionRiskVO;
import com.binance.account.vo.subuser.SubUserBindingVo;
import com.binance.account.vo.subuser.enums.SubAccountSummaryQueryType;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.BindingParentSubUserEmailResp;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountSummaryResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountSummaryResp;
import com.binance.account.vo.subuser.response.SubAccountFuturesEnableResp;
import com.binance.account.vo.subuser.response.SubAccountMarginEnableResp;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.user.CreateFutureUserResponse;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.response.CreateMarginUserResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.SysType;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.binance.account.vo.subuser.SubAccountTransferHistoryInfoVo;
import com.binance.account.vo.subuser.request.ResendSubUserRegisterMailReq;
import com.binance.account.vo.subuser.request.SubAccountTransHistoryInfoReq;
import com.binance.account.vo.subuser.request.SubUserAssetBtcRequest;
import com.binance.account.vo.subuser.request.SubUserCurrencyBalanceReq;
import com.binance.account.vo.subuser.request.SubUserTransferByTranIdReq;
import com.binance.account.vo.subuser.response.SubAccountTransferHistoryInfoResp;
import com.binance.account.vo.subuser.response.SubUserAssetBtcResponse;
import com.binance.account.vo.subuser.response.SubUserCurrencyBalanceResp;
import com.binance.account.vo.user.response.ResendSendActiveCodeResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.aop.MarginValidate;
import com.binance.account.api.SubUserApi;
import com.binance.account.service.subuser.ISubUser;
import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.SubAccountTransferResponse;
import com.binance.account.vo.subuser.response.CreateSubUserResp;
import com.binance.account.vo.subuser.response.SubAccountResp;
import com.binance.account.vo.subuser.response.SubAccountTransferResp;
import com.binance.account.vo.subuser.response.SubUserEmailVoResp;
import com.binance.account.vo.subuser.response.SubUserInfoResp;
import com.binance.account.vo.subuser.response.SubUserTypeResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * Created by Fei.Huang on 2018/10/19.
 */
@Log4j2
@RestController
public class SubUserController implements SubUserApi {

    @Resource
    private ISubUser iSubUser;

    @Resource
    private ISubUserExtra iSubUserExtra;

    @Resource
    private IUser iUser;

    @Autowired
    private IUserFuture userFuture;

    @Autowired
    private IMsgNotification iMsgNotification;

    @Autowired
    private UserInfoBusiness userInfoBusiness;

    @Autowired
    private UserCommonBusiness userCommonBusiness;

    @Autowired
    private UserSecurityBusiness userSecurityBusiness;

    @Override
    @SentinelResource(value = "/sub-user/relation/check")
    public APIResponse<SubUserTypeResponse> checkRelationByUserId(@RequestBody() APIRequest<UserIdReq> request)
            throws Exception {
        return iSubUser.checkRelationByUserId(request);
    }

    @Override
    public APIResponse<Boolean> checkRelationByParentSubUserIds(@RequestBody() APIRequest<BindingParentSubUserReq> request)
            throws Exception {
        return iSubUser.checkRelationByParentSubUserIds(request);
    }

    @Override
    public APIResponse<BindingParentSubUserEmailResp> checkRelationByParentSubUserEmail(@Validated @RequestBody() APIRequest<BindingParentSubUserEmailReq> request) throws Exception {
        BindingParentSubUserEmailReq requestBody=request.getBody();
        BindingParentSubUserEmailResp resp=iSubUser.checkRelationByParentSubUserEmail(requestBody);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<Boolean> notSubUserOrIsEnabledSubUser(@RequestBody() APIRequest<UserIdReq> request)
            throws Exception {
        return iSubUser.notSubUserOrIsEnabledSubUser(request);
    }


    @Override
    public APIResponse<Integer> enableSubUser(@RequestBody() APIRequest<OpenOrCloseSubUserReq> request) throws Exception {
        return iSubUserExtra.enableOrDisableSubUser(request, true);
    }

    @Override
    public APIResponse<Integer> disableSubUser(@RequestBody() APIRequest<OpenOrCloseSubUserReq> request) throws Exception {
        return iSubUserExtra.enableOrDisableSubUser(request, false);
    }

    @Override
    public APIResponse<Integer> resetSecondValidation(@RequestBody APIRequest<ResetSecondValidationRequest> request) throws Exception {
        return iSubUser.resetSecondValidation(request);
    }
    @MarginValidate(userId = "#request.body.parentUserId")
    @Override
    public APIResponse<CreateSubUserResp> createSubUser(@RequestBody() APIRequest<CreateSubUserReq> request)
            throws Exception {
        return iSubUser.createSubUser(request);
    }
    @MarginValidate(userId = "#request.body.parentUserId")
    @Override
    public APIResponse<CreateNoEmailSubUserResp> createNoEmailSubUser(@Validated @RequestBody() APIRequest<CreateNoEmailSubUserReq> request) throws Exception {
        return iSubUser.createNoEmailSubUser(request);
    }

    @Override
    public APIResponse<Boolean> createAssetManagerSubUser(@Validated @RequestBody APIRequest<CreateAssetManagerSubUserReq> request)
            throws Exception {
        return APIResponse.getOKJsonResult(iSubUser.createAssetManagerSubUser(request.getBody()));
    }

    @Override
    public APIResponse<Integer> updateSubUserPwd(@RequestBody() APIRequest<UpdatePassWordRequest> request) throws Exception {
        return iSubUser.updateSubUserPwd(request);
    }

    @Override
    public APIResponse<SubUserInfoResp> selectSubUserInfo(@RequestBody() APIRequest<QuerySubUserRequest> request) throws Exception {
        return iSubUser.selectSubUserInfo(request);
    }

    @Override
    public APIResponse<List<SubAccountResp>> getSubAccountList(@Validated @RequestBody() APIRequest<SubUserSearchReq> request) throws Exception {
        return iSubUser.getSubAccountList(request);
    }

    @Override
    @SentinelResource(value = "/sub-account/transfer/history")
    public APIResponse<List<SubAccountTransferResp>> getSubAccountTransferHistory(@Validated @RequestBody() APIRequest<SubAccountTransHisReq> request) throws Exception {
        Entry entry = null;
        APIResponse<List<SubAccountTransferResp>> resp=new APIResponse<List<SubAccountTransferResp>>();
        try{
            // 针对参数parentId+email进行限流
            entry = SphU.entry("/sub-account/transfer/history", EntryType.IN,1,getParamFlowArg4History(request));
            resp=iSubUser.getSubAccountTransferHistory(request);
        }catch(BlockException e){
            log.error("BlockException occur paramFlowLimit, path=/sub-account/transfer/history");
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }finally {
            if(entry!=null){
                entry.exit(1,getParamFlowArg4History(request));
            }
        }
        return resp;
    }

    /**
     * 根据请求参数获取应该如何限流
     * */
    public String getParamFlowArg4History(APIRequest<SubAccountTransHisReq> request){
        SubAccountTransHisReq requestbody=request.getBody();
        String parentId=requestbody.getParentUserId().toString();
        String email = StringUtils.isBlank(requestbody.getEmail()) ? "":requestbody.getEmail();
        return parentId+email;
    }

    @Override
    @SentinelResource(value = "/sub-account/transfer/historyInfo")
    public APIResponse<SubAccountTransferHistoryInfoResp> getSubAccountTransferHistoryInfo(@Validated @RequestBody() APIRequest<SubAccountTransHistoryInfoReq> request) throws Exception {
        Entry entry = null;
        APIResponse<SubAccountTransferHistoryInfoResp> resp=new APIResponse<SubAccountTransferHistoryInfoResp>();
        try{
            // 针对参数parentId+subUserId进行限流
            entry = SphU.entry("/sub-account/transfer/historyInfo", EntryType.IN,1,getParamFlowArg(request));
            resp=iSubUser.getSubAccountTransferHistoryInfo(request);
        }catch(BlockException e){
            log.error("BlockException occur paramFlowLimit, path=/sub-account/transfer/historyInfo");
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }finally {
            if(entry!=null){
                entry.exit(1,getParamFlowArg(request));
            }
        }
        return resp;
    }

    /**
     * 根据请求参数获取应该如何限流
     * */
    public String getParamFlowArg(APIRequest<SubAccountTransHistoryInfoReq> request){
        SubAccountTransHistoryInfoReq requestbody=request.getBody();
        String parentId=requestbody.getParentUserId().toString();
        String subUserId=null==requestbody.getUserId()?"0":requestbody.getUserId().toString();
        return parentId+subUserId;
    }

    @Override
	public APIResponse<SubUserEmailVoResp> selectSubUserEmailList(@RequestBody() APIRequest<ParentUserIdReq> request)
			throws Exception {
		return iSubUser.selectSubUserEmailList(request);
	}

	@Override
	public APIResponse<GetUserSecurityLogResponse> loginHistoryList(@RequestBody() APIRequest<SubUserSecurityLogReq> request)
			throws Exception {
		return iSubUser.loginHistoryList(request);
	}

    @Override
    public APIResponse<SubAccountTransferResponse> subAccountTransfer(@RequestBody() APIRequest<SubAccountTransferRequest> request) throws Exception {
        return iSubUser.subAccountTransfer(request);
    }

    @Override
    public APIResponse<Integer> modifySubAccount(@RequestBody() APIRequest<ModifySubAccountRequest> request) throws Exception {
        return iSubUser.modifySubAccount(request);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<SubAccountMarginEnableResp> subAccountMarginEnable(@Validated @RequestBody()APIRequest<SubAccountMarginEnableRequest> request) throws Exception {
        SubAccountMarginEnableRequest requestBody=request.getBody();
        User subUser= iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(),requestBody.getEmail());
        //资管子账户不可修改邮箱
        if (BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        boolean isMarginExist = BitUtils.isTrue(subUser.getStatus(), Constant.USER_IS_EXIST_MARGIN_ACCOUNT);
        //margin账户不存在
        if (!isMarginExist){
            //enable=false,不存在要求禁用
            if (!requestBody.getEnable()){
                throw new BusinessException(AccountErrorCode.SUB_USER_MARGIN_IS_NOT_EXIST);
            }
            //enable=true，要求创建
            APIRequest<CreateMarginAccountRequest> originRequest = new APIRequest<CreateMarginAccountRequest>();
            originRequest.setLanguage(LanguageEnum.ZH_CN);
            originRequest.setTerminal(TerminalEnum.WEB);
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            CreateMarginAccountRequest createMarginAccountRequest=new CreateMarginAccountRequest();
            createMarginAccountRequest.setUserId(subUser.getUserId());
            createMarginAccountRequest.setParentUserId(requestBody.getParentUserId());
            APIResponse<CreateMarginUserResponse> apiResponse=iUser.createMarginAccount(APIRequest.instance(originRequest, createMarginAccountRequest));
            SubAccountMarginEnableResp resp=new SubAccountMarginEnableResp();
            resp.setEmail(requestBody.getEmail());
            resp.setIsMarginEnabled(null!=apiResponse.getData().getMarginTradingAccount());
            return APIResponse.getOKJsonResult(resp);
        }else{
            UserInfoVo userInfoVo = userInfoBusiness.getUserInfoByUserId(subUser.getUserId());
            //如果交易账户不存在，则未激活
            if(Objects.isNull(userInfoVo)||Objects.isNull(userInfoVo.getTradingAccount())){
                throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
            }
            //获取marign的userInfo
            User marginUser = userCommonBusiness.checkAndGetUserById(userInfoVo.getMarginUserId());
            userCommonBusiness.updateMarginTradeAccount(marginUser.getUserId(),requestBody.getEnable());
            SubAccountMarginEnableResp resp=new SubAccountMarginEnableResp();
            resp.setEmail(requestBody.getEmail());
            resp.setIsMarginEnabled(requestBody.getEnable());
            return APIResponse.getOKJsonResult(resp);
        }

    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<SubAccountFuturesEnableResp> subAccountFuturesEnable(@Validated @RequestBody() APIRequest<SubAccountFuturesEnableRequest> request) throws Exception {
        SubAccountFuturesEnableRequest requestBody=request.getBody();
        User subUser= iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(),requestBody.getEmail());
        //资管子账户不可修改邮箱
        if (BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //组装请求参数
        boolean isFutureExist = BitUtils.isTrue(subUser.getStatus(), Constant.USER_IS_EXIST_FUTURE_ACCOUNT);
        SubAccountFuturesEnableResp resp=new SubAccountFuturesEnableResp();
        if (!isFutureExist){
            if (!requestBody.getEnable()){
                throw new BusinessException(AccountErrorCode.SUB_USER_FUTURE_IS_NOT_EXIST);
            }
            APIRequest<CreateFutureAccountRequest> originRequest = new APIRequest<CreateFutureAccountRequest>();
            originRequest.setLanguage(LanguageEnum.ZH_CN);
            originRequest.setTerminal(TerminalEnum.WEB);
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            CreateFutureAccountRequest createFutureAccountRequest=new CreateFutureAccountRequest();
            createFutureAccountRequest.setUserId(subUser.getUserId());
            createFutureAccountRequest.setParentUserId(requestBody.getParentUserId());
            APIResponse<CreateFutureUserResponse> apiResponse=userFuture.createFutureAccount(APIRequest.instance(originRequest, createFutureAccountRequest));
            resp.setEmail(requestBody.getEmail());
            resp.setIsFuturesEnabled(null!=apiResponse.getData().getFutureTradingAccount());
            return APIResponse.getOKJsonResult(resp);
        }else{
            UserInfoVo userInfoVo = userInfoBusiness.getUserInfoByUserId(subUser.getUserId());
            //如果交易账户不存在，则未激活
            if(Objects.isNull(userInfoVo)||Objects.isNull(userInfoVo.getFutureUserId())){
                throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
            }
            UserInfoVo futurUserInfo = userInfoBusiness.getUserInfoByUserId(userInfoVo.getFutureUserId());
            if(Objects.isNull(futurUserInfo)||Objects.isNull(futurUserInfo.getMeTradingAccount())){
                throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
            }
            //获取marign的userInfo
            userCommonBusiness.updateFutureTradeAccount(userInfoVo.getFutureUserId(),requestBody.getEnable(),futurUserInfo.getMeTradingAccount());
            resp.setEmail(requestBody.getEmail());
            resp.setIsFuturesEnabled(requestBody.getEnable());
            return APIResponse.getOKJsonResult(resp);
        }

    }

    @Override
    public APIResponse<QuerySubAccountFutureAccountResp> queryFuturesAccount(@Validated @RequestBody() APIRequest<QuerySubAccountFutureAccountRequest> request) throws Exception {
        QuerySubAccountFutureAccountRequest requestBody=request.getBody();
        User subUser= iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(),requestBody.getEmail());
        QuerySubAccountFutureAccountResp resp=userFuture.queryFuturesAccount(subUser.getUserId());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<Long> checkRelationAndFutureAccountEnable(@Validated @RequestBody() APIRequest<QuerySubAccountFutureAccountRequest> request) throws Exception {
        QuerySubAccountFutureAccountRequest requestBody=request.getBody();
        User subUser= iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(),requestBody.getEmail());
        boolean isFutureExist = BitUtils.isTrue(subUser.getStatus(), Constant.USER_IS_EXIST_FUTURE_ACCOUNT);
        return APIResponse.getOKJsonResult(isFutureExist?subUser.getUserId():null);
    }

    @Override
    public APIResponse<QuerySubAccountFutureAccountSummaryResp> queryFuturesAccountSummary(@Validated @RequestBody() APIRequest<QuerySubAccountFutureAccountSummaryRequest> request) throws Exception {
        QuerySubAccountFutureAccountSummaryRequest requestBody=request.getBody();
        Long parentUserId=requestBody.getParentUserId();
        SubAccountSummaryQueryType subAccountSummaryQueryType=requestBody.getSubAccountSummaryQueryType();
        UserInfo parentUserInfo=null;
        List<UserInfo> subUserInfoList=null;
        if(SubAccountSummaryQueryType.ONLY_PARENT_ACCOUNT==subAccountSummaryQueryType){
            parentUserInfo=iSubUser.checkParentAndGetUserInfo(parentUserId);
        }else if(SubAccountSummaryQueryType.ONLY_SUB_ACCOUNT==subAccountSummaryQueryType){
            String email=requestBody.getEmail();
            Integer isSubUserEnabled=requestBody.getIsSubUserEnabled();
            subUserInfoList=iSubUser.checkParentAndGetSubUserInfoList(parentUserId,email,isSubUserEnabled);
        }else if(SubAccountSummaryQueryType.QUERY_ALL==subAccountSummaryQueryType){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        QuerySubAccountFutureAccountSummaryResp resp= userFuture.queryFuturesAccountSummary(parentUserInfo,subUserInfoList,subAccountSummaryQueryType,requestBody.getPage(),requestBody.getRows());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<List<FuturePositionRiskVO>> queryFuturesPositionRisk(@Validated @RequestBody() APIRequest<QueryFuturesPositionRiskRequest> request) throws Exception {
        QueryFuturesPositionRiskRequest requestBody=request.getBody();
        User subUser= iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(),requestBody.getEmail());
        List<FuturePositionRiskVO> resp=userFuture.queryFuturesPositionRisk(subUser.getUserId());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<QuerySubAccountMarginAccountSummaryResp> queryMarginAccountSummary(@Validated @RequestBody() APIRequest<QuerySubAccountMarginAccountSummaryRequest> request) throws Exception {
        QuerySubAccountMarginAccountSummaryRequest requestBody=request.getBody();
        String email=requestBody.getEmail();
        Integer isSubUserEnabled=requestBody.getIsSubUserEnabled();
        Long parentUserId=requestBody.getParentUserId();
        UserInfo parentUserInfo=iSubUser.checkParentAndGetUserInfo(parentUserId);
        List<UserInfo> subUserInfoList=iSubUser.checkParentAndGetSubUserInfoList(parentUserId,email,isSubUserEnabled);
        QuerySubAccountMarginAccountSummaryResp resp= userFuture.queryMarginAccountSummary(parentUserInfo,subUserInfoList,requestBody.getPage(),requestBody.getRows());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<QuerySubAccountMarginAccountResp> queryMarginAccount(@Validated @RequestBody() APIRequest<QuerySubAccountMarginAccountRequest> request) throws Exception {
        QuerySubAccountMarginAccountRequest requestBody=request.getBody();
        User subUser= iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(),requestBody.getEmail());
        QuerySubAccountMarginAccountResp resp=userFuture.queryMarginAccount(subUser,requestBody.getMarginPeriodType());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<ResendSendActiveCodeResponse> resendSubUserRegisterMail(@Validated @RequestBody() APIRequest<ResendSubUserRegisterMailReq> request) throws Exception {
        return iSubUser.resendSubUserRegisterMail(request);
    }

    @Override
    public APIResponse<SubUserAssetBtcResponse> subUserAssetBtcList(@Validated @RequestBody() APIRequest<SubUserAssetBtcRequest> request) throws Exception {
        return iSubUser.subUserAssetBtcList(request);
    }

    @Override
    public APIResponse<BigDecimal> parentUserAssetBtc(@Validated @RequestBody() APIRequest<ParentUserIdReq> request) throws Exception {
        return iSubUser.parentUserAssetBtc(request);
    }

    @Override
    public APIResponse<BigDecimal> allSubUserAssetBtc(@Validated @RequestBody() APIRequest<ParentUserIdReq> request) throws Exception {
        return iSubUser.allSubUserAssetBtc(request);
    }

    @Override
    public APIResponse<List<SubUserCurrencyBalanceResp>> subUserCurrencyBalance(@Validated @RequestBody() APIRequest<SubUserCurrencyBalanceReq> request) throws Exception {
        return iSubUser.subUserCurrencyBalance(request);
    }

    @Override
    public APIResponse<SubAccountTransferHistoryInfoVo> getSubUserTransferByTranId(@Validated @RequestBody() APIRequest<SubUserTransferByTranIdReq> request) throws Exception {
        return iSubUser.getSubUserTransferByTranId(request);
    }

    @Override
    public APIResponse<SubAccountTransferVersionForSubToSubResponse> subAccountTransferVersionForSubToSub(@Validated @RequestBody() APIRequest<SubAccountTransferVersionForSubToSubRequest> request) throws Exception {
        return iSubUser.subAccountTransferVersionForSubToSub(request);
    }

    @Override
    public APIResponse<SubAccountTransferVersionForSubToMasterResponse> subAccountTransferVersionForSubToMaster(@Validated @RequestBody() APIRequest<SubAccountTransferVersionForSubToMasterRequest> request) throws Exception {
        return iSubUser.subAccountTransferVersionForSubToMaster(request);

    }

    @Override
    public APIResponse<List<SubAccountTranHisResForSapiVersion>> subUserHistoryVersionForSapi(@Validated @RequestBody() APIRequest<SubUserHistoryVersionForSapiRequest> request) throws Exception {
        return iSubUser.subUserHistoryVersionForSapi(request);

    }

    @Override
    public APIResponse<Integer> updateSubUserRemark(@Validated @RequestBody() APIRequest<UpdateSubUserRemarkRequest> request) throws Exception {
        return iSubUser.updateSubUserRemark(request);
    }

    @Override
    public APIResponse<Boolean> subAccountFutureAssetTransfer(@Validated @RequestBody APIRequest<SubAccountFutureTransferReq> request)throws Exception{
        return iSubUser.subAccountFutureAssetTransfer(request);
    }

    @Override
    public APIResponse<List<SubUserBindingVo>> queryFutureSubUserBinding(@Validated @RequestBody APIRequest<IdRequest> request) throws Exception {
        return iSubUser.queryFutureSubUserBinding(request);
    }

    @Override
    public APIResponse<List<UserInfoVo>> checkParentAndGetSubUserInfoList(@Validated @RequestBody APIRequest<CheckParentAndGetSubUserInfoListRequest> request) throws Exception {
        CheckParentAndGetSubUserInfoListRequest requestBody = request.getBody();
        List<UserInfo> userInfos = iSubUser.checkParentAndGetSubUserInfoList(requestBody.getParentUserId(), requestBody.getEmail(), requestBody.getIsSubUserEnabled());
        
        List<UserInfoVo> result = userInfos.stream().map(x -> {
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(x, userInfoVo);
            return userInfoVo;
        }).collect(Collectors.toList());
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<UserInfoVo> checkParentAndGetUserInfo(@Validated @RequestBody APIRequest<IdRequest> request) throws Exception {
        Long parentUserId = request.getBody().getUserId();
        UserInfo userInfo = iSubUser.checkParentAndGetUserInfo(parentUserId);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        return APIResponse.getOKJsonResult(userInfoVo);
    }

    public APIResponse<UserVo> checkParentAndSubUserBinding(@Validated @RequestBody APIRequest<CheckParentAndSubUserBindingRequest> request) throws Exception {
        CheckParentAndSubUserBindingRequest requestBody = request.getBody();
        User user = iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(), requestBody.getEmail());
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return APIResponse.getOKJsonResult(userVo);
    }

}
