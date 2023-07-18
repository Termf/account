package com.binance.account.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.UserConst;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.AccountConstants;
import com.binance.account.constants.enums.MatchBoxAccountRestrictionModeEnum;
import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.constants.enums.UserTypeEnum;
import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.margin.IsolatedMarginUserBinding;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.agent.UserAgentLogMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.margin.IsolatedMarginUserBindingMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.margin.MarginAccountApiClient;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.integration.risk.RiskSecurityApiClient;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.subuser.ISubUser;
import com.binance.account.service.subuser.impl.CheckSubUserBusiness;
import com.binance.account.service.user.IUserMargin;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.margin.request.CheckIsolatedMarginUserRelationShipReq;
import com.binance.account.vo.margin.request.CreateIsolatedMarginUserReq;
import com.binance.account.vo.margin.request.GetIsolatedMarginUserListReq;
import com.binance.account.vo.margin.request.GetRootUserIdByIsolatedMarginUserIdReq;
import com.binance.account.vo.margin.response.CreateIsolatedMarginUserResp;
import com.binance.account.vo.margin.response.GetIsolatedMarginUserListResp;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.MainMarginAccountTransferRequest;
import com.binance.account.vo.user.response.CreateMarginUserResponse;
import com.binance.account.vo.user.response.MainMarginAccountTransferResponse;
import com.binance.account.vo.user.response.MarginUserTypeResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIResponse;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.risk.vo.CheckUserRiskRequestVo;
import com.google.common.collect.Lists;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.javasimon.aop.Monitored;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.binance.account.service.user.impl.UserBusiness.DEFAULT_RESULT;

/**
 * @author lufei
 * @date 2019/3/8
 */
@Log4j2
@Service
@Monitored
public class UserMarginBusiness implements IUserMargin {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ISubUser iSubUser;
    @Autowired
    private CheckSubUserBusiness checkSubUserBusiness;
    @Autowired
    private MarginAccountApiClient marginAccountApiClient;
    @Autowired
    private RiskSecurityApiClient riskSecurityApiClient;
    @Autowired
    private UserKycApproveMapper userKycApproveMapper;
    @Autowired
    private SubUserBindingMapper subUserBindingMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserTradingAccountMapper userTradingAccountMapper;
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserCommonValidateService userCommonValidateService;
    @Autowired
    private MatchboxApiClient matchboxApiClient;
    @Autowired
    private IsolatedMarginUserBindingMapper isolatedMarginUserBindingMapper;
    @Resource
    protected UserSecurityMapper userSecurityMapper;
    @Resource
    protected UserCommonBusiness userCommonBusiness;
    @Autowired
    private ISysConfig iSysConfig;
    @Resource
    private IUserSecurity userSecurityBusiness;
    @Resource
    protected IMsgNotification iMsgNotification;
    @Autowired
    private UserAgentLogMapper userAgentLogMapper;




    @Override
    public APIResponse<MarginUserTypeResponse> allUserInfo(UserIdRequest request) {
        Long userId = request.getUserId();
        User user = checkSubUserBusiness.checkAndGetUserById(userId);
        UserStatusEx userStatusEx = new UserStatusEx(user.getStatus());
        MarginUserTypeResponse response = new MarginUserTypeResponse();
        if(userStatusEx.getIsMarginUser()){
            UserInfo marginQuery = new UserInfo();
            marginQuery.setMarginUserId(userId);
            List<Long> parentUserIds = userInfoMapper.queryUserId(marginQuery);
            if(CollectionUtils.isEmpty(parentUserIds) || parentUserIds.size() > 1){
                log.warn("UserMarginBusiness.allUserInfo用户:{},状态:{}是margin账户,母账户查找失败", userId, user.getStatus());
                throw new BusinessException(GeneralCode.SYS_VALID);
            }
            List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(parentUserIds.get(0));
            response.setUserType(MarginUserTypeResponse.UserType.MARGIN);
            response.setParentUserId(parentUserIds.get(0));
            response.setMarginUserId(userId);
            response.setSubUserIds(subUserBindings.stream().map(e->e.getSubUserId()).collect(Collectors.toList()));
        }else if(checkSubUserBusiness.isSubUserFunctionEnabled(user.getStatus())){
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
            List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(userId);
            response.setUserType(MarginUserTypeResponse.UserType.PARENT);
            response.setParentUserId(userId);
            response.setMarginUserId(userInfo.getMarginUserId());
            response.setSubUserIds(subUserBindings.stream().map(e->e.getSubUserId()).collect(Collectors.toList()));
        }else if(checkSubUserBusiness.isSubUser(user)){
            SubUserBinding subUserBinding = subUserBindingMapper.selectBySubUserId(userId);
            List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(subUserBinding.getParentUserId());
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getParentUserId());
            response.setUserType(MarginUserTypeResponse.UserType.SUB);
            response.setParentUserId(subUserBinding.getParentUserId());
            response.setMarginUserId(userInfo.getMarginUserId());
            response.setSubUserIds(subUserBindings.stream().map(e->e.getSubUserId()).collect(Collectors.toList()));
        }else{
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
            response.setUserType(MarginUserTypeResponse.UserType.NORMAL);
            response.setParentUserId(null);
            response.setMarginUserId(userInfo.getMarginUserId());
            response.setSubUserIds(Collections.emptyList());
        }
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<MarginUserTypeResponse> globalUserInfo(UserIdRequest request) {
        APIResponse<MarginUserTypeResponse> response = this.allUserInfo(request);
        if(response.getData().getSubUserIds() != null && response.getData().getSubUserIds().size() > 0){//子账户添加margin账户
            List<Long> subUserIds = response.getData().getSubUserIds();
            List<UserInfo> userInfos = this.userInfoMapper.selectUserInfoList(subUserIds);
            for(UserInfo userInfo : userInfos){
                if(userInfo.getMarginUserId() != null){
                    subUserIds.add(userInfo.getMarginUserId());
                }
            }
        }
        return response;
    }

    public APIResponse<MainMarginAccountTransferResponse> subMainMarginAccountTransfer(MainMarginAccountTransferRequest requestBody)throws Exception{
        //1.check 字母账号关系
        User subUser= iSubUser.checkParentAndSubUserBinding(requestBody.getParentUserId(),requestBody.getSubEmail());
        UserInfo subUserInfo = userInfoMapper.selectByPrimaryKey(subUser.getUserId());
        if (subUserInfo == null || subUserInfo.getMarginUserId() == null){
            throw new BusinessException(AccountErrorCode.MARGIN_ACCOUNT_IS_NOT_EXIST);
        }
        //2.确定划转from to
        Long senderUserId = requestBody.getType() == 1?subUser.getUserId():subUserInfo.getMarginUserId();
        Long recipientUserId = requestBody.getType() == 1?subUserInfo.getMarginUserId():subUser.getUserId();
        //3.由于margin main之间无限制，则划转
        String asset = requestBody.getAsset();// 资产名字
        BigDecimal amount = requestBody.getAmount();// 划转数量
        // 3 验证转账参数之间关系的合法性
        checkSubUserBusiness.validateSubMarginMainAccountTransfer(requestBody.getParentUserId(), senderUserId, recipientUserId, asset, amount,requestBody.getType());
        // 4 开始正式进行转账操作
        Long transactionId =
                marginAccountApiClient.marginTransfer(subUser.getUserId(), asset, amount,requestBody.getType());
        MainMarginAccountTransferResponse resp = new MainMarginAccountTransferResponse();
        resp.setTranId(transactionId);
        return APIResponse.getOKJsonResult(resp);
    }

    /**
     * IsolatedMargin账户的创建方法
     * 业务层面：一个主账户下面可以创建多个isolatedMarginUser
     * 逻辑很简单，任何一部事务操作失败会回滚上下文中的所有操作，这样的话数据比较干净
     * 所以这个接口不应该去做幂等，而是应该通过数据库唯一索引触发事务回滚
     * 注意：AopContext.currentProxy()是为了强制开启spring动态代理来托管事务,反正会失效
     */
    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
//    @UserPermissionValidate(userId = "#req.rootUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_ISOLATED_MARGIN)
    public CreateIsolatedMarginUserResp createIsolatedMarginUser(CreateIsolatedMarginUserReq req) throws Exception {
        log.info("UserMarginBusiness.createIsolatedMarginUser req:{}", JSON.toJSONString(req));

        //校验并且获取主账户信息  ,rootuser这里解释为主账户
        Pair<User, UserInfo> rootTuple = checkAndGetUserByIdForIsolatedMarginVersion(req.getRootUserId(),null);
        //获取主账号相关信息
        User rootUser = rootTuple.getLeft();
        UserInfo rootUserInfo = rootTuple.getRight();
        UserStatusEx rootUserStatusEx=new UserStatusEx(rootUser.getStatus());
        //开始创建IsolatedMargin账号相关信息
        String vritualEmail = createIsolatedMarginVirtualEmail(rootUser.getEmail(), rootUser.getUserId(),req.getSymbols());
        User oldIsolatedUser= userMapper.queryByEmail(vritualEmail);
        if(null!=oldIsolatedUser){
            CreateIsolatedMarginUserResp oldresp = new CreateIsolatedMarginUserResp();
            oldresp.setRootUserId(rootUserInfo.getUserId());
            oldresp.setRootTradingAccount(rootUserInfo.getTradingAccount());
            oldresp.setIsolatedMarginUserId(oldIsolatedUser.getUserId());
            UserInfo oldIsolatedMarginUserInfo = userInfoMapper.selectByPrimaryKey(oldIsolatedUser.getUserId());
            if (null == oldIsolatedMarginUserInfo) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            oldresp.setIsolatedMarginTradingAccount(oldIsolatedMarginUserInfo.getTradingAccount());
            oldresp.setIsSubUser(rootUserStatusEx.getIsSubUser());
            oldresp.setIsBrokerSubUser(rootUserStatusEx.getIsBrokerSubUser());
            log.info("return oldresp={}",JsonUtils.toJsonNotNullKey(oldresp));
            return oldresp;
        }
        User marginUser = ((UserMarginBusiness) AopContext.currentProxy()).createIsolatedMarginUser(vritualEmail);
        // 创建IsolatedMargin账号Security信息
        ((UserMarginBusiness) AopContext.currentProxy()).createIsolatedMarginUserSecurity(marginUser.getUserId(), marginUser.getEmail());
        // 创建IsolatedMargin账号info信息
        UserInfo marginUserInfo = ((UserMarginBusiness) AopContext.currentProxy()).createIsolatedMarginUserInfo(rootUserInfo, marginUser.getUserId());
        // 绑定主账号、isolated margin账号关系
       ((UserMarginBusiness) AopContext.currentProxy()).createIsolatedMarginUserBinding(rootUserInfo.getUserId(), marginUser.getUserId(),null);
        //更新主账户的状态(这步操作也是幂等的),这是用来标明这个账户是否拥有margin账户
        rootUser.setStatus(BitUtils.enable(rootUser.getStatus(), AccountCommonConstant.USER_IS_EXIST_ISOLATED_MARGIN_ACCOUNT));
        userMapper.updateByEmailSelective(rootUser);
        // 创建IsolatedMargin交易账户
        //这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
        Long marginTradingAccount = matchboxApiClient.postAccountWithRestrictionMode(marginUserInfo, MatchBoxAccountTypeEnum.ISOLATED_MARGIN,MatchBoxAccountRestrictionModeEnum.SINGLE,req.getSymbols(),"0");
        marginUserInfo.setTradingAccount(marginTradingAccount);
        this.userInfoMapper.updateByPrimaryKeySelective(marginUserInfo);
        UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
        userTradingAccount.setTradingAccount(marginTradingAccount);
        userTradingAccount.setUserId(marginUserInfo.getUserId());
        this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
        log.info("createIsolatedMarginUser.postAccount insert:{}", JSON.toJSONString(userTradingAccount));
        Boolean isSubUser=rootUserStatusEx.getIsSubUser();
        //还需要更新是否燃烧bnb的标志位
        AsyncTaskExecutor.execute(() -> {
            HintManager hintManager = null;
            try {
                TimeUnit.SECONDS.sleep(1);
                hintManager = HintManager.getInstance();
                hintManager.setMasterRouteOnly();
                log.info("updateIsolatedMarginBNBFee start userid={}",marginUserInfo.getUserId());
                if (BitUtils.isTrue(rootUser.getStatus(), Constant.USER_FEE)) {
                    userSecurityBusiness.setIsolatedMarginBnbFee(marginUserInfo.getUserId(),true);
                }else{
                    userSecurityBusiness.setIsolatedMarginBnbFee(marginUserInfo.getUserId(),false);
                }
                log.info("updateIsolatedMarginBNBFee finish userid={}",marginUserInfo.getUserId());
            } catch (Exception e) {
                log.error("updateIsolatedMarginBNBFee sleep exception", e);
            }finally {
                if (null != hintManager) {
                    hintManager.close();
                }
            }
            try {
                // 加入推荐记录表
                insertToAgentLog(req.getRootUserId(), marginUser.getUserId(), marginUser.getEmail());
            } catch (Exception e) {
                log.error("insertToAgentLog exception", e);
            }
            try{
                UserStatusEx userStatusEx=new UserStatusEx(rootUser.getStatus());
                if(userStatusEx.getIsSubUserFunctionEnabled()){
                    Map<String, Object> dataMsg = new HashMap<>();
                    dataMsg.put("parentUserId", rootUser.getUserId());
                    MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE_ONLY_PARENT_MARGIN, dataMsg);
                    log.info("iMsgNotification sendUserProductFeeMsgFroMargin:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                    iMsgNotification.send(msg);
                }else if(userStatusEx.getIsSubUser()){
                    Map<String, Object> dataMsg = new HashMap<>();
                    SubUserBinding subUserBinding=subUserBindingMapper.selectBySubUserId(rootUser.getUserId());
                    if(null!=subUserBinding){
                        dataMsg.put("parentUserId", subUserBinding.getParentUserId());
                        dataMsg.put("subUserId", rootUser.getUserId());
                        MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE, dataMsg);
                        log.info("iMsgNotification sendUserProductFeeMsgFroMargin:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                        iMsgNotification.send(msg);
                    }
                }else {
                    log.info("rootUserId is not parentUser or subUser:{}", rootUser.getUserId());

                }
            }catch (Exception e){
                log.error("sendUserProductFeeMsgFroMargin exception", e);

            }

        });
        log.info("UserMarginBusiness.createIsolatedMarginUser end");
        //发送消息回写给pnk库 后续需要删除，临时双写
        sendRegisterMqMsgForIsolatedMargin(marginUser, marginUserInfo);
        CreateIsolatedMarginUserResp createIsolatedMarginUserResp = new CreateIsolatedMarginUserResp();
        createIsolatedMarginUserResp.setRootUserId(rootUserInfo.getUserId());
        createIsolatedMarginUserResp.setRootTradingAccount(rootUserInfo.getTradingAccount());
        createIsolatedMarginUserResp.setIsolatedMarginUserId(marginUserInfo.getUserId());
        createIsolatedMarginUserResp.setIsolatedMarginTradingAccount(marginTradingAccount);
        createIsolatedMarginUserResp.setIsSubUser(rootUserStatusEx.getIsSubUser());
        createIsolatedMarginUserResp.setIsBrokerSubUser(rootUserStatusEx.getIsBrokerSubUser());
        return createIsolatedMarginUserResp;
    }

    @Override
    public GetIsolatedMarginUserListResp getIsolatedMarginUserList(GetIsolatedMarginUserListReq req) throws Exception {
        Long rootUserId=req.getRootUserId();
        List<Long> isolatedMarginUserIdList=isolatedMarginUserBindingMapper.selectisolatedMarginUserIdsByRootUserId(rootUserId);
        GetIsolatedMarginUserListResp resp=new GetIsolatedMarginUserListResp();
        if(CollectionUtils.isEmpty(isolatedMarginUserIdList)){
            return resp;
        }
        resp.setTotal(Integer.valueOf(isolatedMarginUserIdList.size()).longValue());
        resp.setIsolatedMarginUserIdList(isolatedMarginUserIdList);
        return resp;
    }

    @Override
    public Long getRootUserIdByIsolatedMarginUserId(GetRootUserIdByIsolatedMarginUserIdReq req) throws Exception {
        Long isolatedMarginUserId=req.getIsolatedMarginUserId();
        IsolatedMarginUserBinding isolatedMarginUserBinding=isolatedMarginUserBindingMapper.selectByIsolatedMarginUserId(isolatedMarginUserId);
        if(null==isolatedMarginUserBinding){
            return null;
        }else{
            return isolatedMarginUserBinding.getRootUserId();
        }
    }

    @Override
    public Boolean checkIsolatedMarginRelationShip(CheckIsolatedMarginUserRelationShipReq req) throws Exception {
        IsolatedMarginUserBinding isolatedMarginUserBinding=isolatedMarginUserBindingMapper.selectisolatedMarginUserIdsByRootUserIdAndIsolatedMarginUserId(req.getRootUserId(),req.getIsolatedMarginUser());
        if(null==isolatedMarginUserBinding){
            return false;
        }else{
            return true;
        }
    }


    /**
     * 校验用户信息（Isolated margin version）
     *
     * @return Pair 返回的是一个元组，主要是不想再单独包个对象了，为了简单
     */
    protected Pair<User, UserInfo> checkAndGetUserByIdForIsolatedMarginVersion(Long userId,Long parentUserId) throws Exception {
        if (null == userId) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserKycApprove userKyc = userKycApproveMapper.selectByPrimaryKey(userId);
        Boolean checkRiskResult=riskSecurityApiClient.checkUserRisk(userId, CheckUserRiskRequestVo.RiskScenario.Margin);
        if(null!=checkRiskResult && checkRiskResult.booleanValue()&& null==userKyc){
            throw new BusinessException(AccountErrorCode.PLEASE_FINISH_KYC_FIRST_BEFORE_OPENNING_MARGIN_ACCOUNT);
        }
        final User rootUser = userMapper.queryByEmail(userIndex.getEmail());
        if (null == rootUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        //检查传进来的userid是否是margin的userid
        //当前账号不能是margin 账号
        if (BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_MARGIN_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }
        //当前账号不能是future
        if (BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_FUTURE_USER)) {
            throw new BusinessException(AccountErrorCode.SUB_USER_CANNOT_CREATE_FUTURE_ACCOUNT);
        }
        //当前账号没有激活
        if (!BitUtils.isEnable(rootUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(AccountErrorCode.ACTIVE_MARGIN_ACCOUNT_FAILED);
        }

        //当前账号不能是isolated margin 账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_ISOLATED_MARGIN_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }

        //当前账号不能是法币 账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_FIAT_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }

        //当前账号不能是矿池账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_MINING_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }





        //当前账号是否是子账号
        Boolean isSubUser=BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        Boolean needCheckSubUserValidate= isSubUser && Objects.isNull(parentUserId);
        if(needCheckSubUserValidate){
            userCommonValidateService.checkCountryBackListByIp(userId);
        }

        //当前账号必须开通全仓margin账号
        if (isSubUser && !BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_EXIST_MARGIN_ACCOUNT)) {
            throw new BusinessException(AccountErrorCode.PLEASE_OPEN_MARGIN_ACCOUNT_BEFORE_CREATE_ISOLATED_MARGIN);
        }


        //当前账号是否绑定手机验证或者google验证,只要有一个验证过我们就算通过
        Boolean isPass2FA = BitUtils.isEnable(rootUser.getStatus(), Constant.USER_MOBILE) || BitUtils.isEnable(rootUser.getStatus(), Constant.USER_GOOGLE);

        //判断用户的kyc国家是否在黑名单
        if(!isSubUser){
            userCommonValidateService.checkKycCountryBackList(userId);
        }
        UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(userId);
        //当前账号没有激活
        if (null == rootUserInfo.getTradingAccount()) {
            throw new BusinessException(AccountErrorCode.ACTIVE_MARGIN_ACCOUNT_FAILED);
        }
        Pair<User, UserInfo> twoTuple = Pair.of(rootUser, rootUserInfo);
        return twoTuple;
    }


    /**
     * 创建一个虚拟邮箱
     * */
    protected String createIsolatedMarginVirtualEmail(String email,Long userId,String symbol){
        String[] emailArray=email.split("@");
        String virtualEmail=emailArray[0]+"_"+String.valueOf(userId)+"_isolatedMargin_"+symbol+"@"+emailArray[1];
        log.info("createIsolatedMarginVirtualEmail originemail={},userid={},virtualEmail={}",email,userId,virtualEmail);
        return virtualEmail.toLowerCase();
    }


    /**
     * 创建Isolated Margin用户Security信息（不幂等，但是会回滚）
     *
     * @param userId
     * @param userEmail
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void createIsolatedMarginUserSecurity(final Long userId, final String userEmail) {
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(userId);
        userSecurity.setEmail(userEmail);
        userSecurity.setAntiPhishingCode("");
        userSecurity.setSecurityLevel(1);
        userSecurity.setMobileCode("");
        userSecurity.setMobile("");
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLoginFailedTime(DateUtils.getNewDate());
        userSecurity.setAuthKey("");
        userSecurity.setLastLoginTime(DateUtils.getNewDate());
        userSecurity.setLockEndTime(DateUtils.getNewDate());
        userSecurity.setInsertTime(DateUtils.getNewDate());
        userSecurity.setUpdateTime(DateUtils.getNewDate());
        userSecurity.setWithdrawSecurityStatus(1);
        userSecurity.setWithdrawSecurityAutoStatus(1);
        userSecurityMapper.insert(userSecurity);
    }


    /**
     * 创建isolated margin账户逻辑（不幂等，但是会回滚）
     *
     * @param vritualEmail
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected User createIsolatedMarginUser(final String vritualEmail) throws NoSuchAlgorithmException {
        //这里加了事务回滚，所以如果报错数据直接回滚
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(vritualEmail);
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User marginUser = User.buildRegisterObject(userIndex, "123456", cipherCode);
        //实际上margin账号的user并不需要密码和salt所以设置为空字符串
        marginUser.setPassword("");
        marginUser.setSalt("");
        //因为是isolated margin账号所以只有交易功能，还有需要标志成isolated margin
        Long status = marginUser.getStatus();
        status = BitUtils.enable(status, AccountCommonConstant.USER_IS_ISOLATED_MARGIN_USER);
        marginUser.setStatus(status);
        userMapper.insert(marginUser);
        return marginUser;

    }


    /**
     * 创建isolated margin用户信息（不幂等，但是会回滚）
     *
     * @param rootUserInfo
     * @param marginUserId
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected UserInfo createIsolatedMarginUserInfo(UserInfo rootUserInfo, Long marginUserId) {
        //逻辑很简单，从主账号的userinfo把信息都copy过来就完事了
        UserInfo marginUserInfo = new UserInfo();
        marginUserInfo.setUserId(marginUserId);
        // 经纪人返佣比例
        marginUserInfo.setAgentRewardRatio(rootUserInfo.getAgentRewardRatio());
        // 被推荐人返佣比例
        marginUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        // 用户交易账号 激活时创建
        marginUserInfo.setTradingAccount(null);
        // 被动方手续费
        marginUserInfo.setMakerCommission(rootUserInfo.getMakerCommission());
        // 主动方手续费
        marginUserInfo.setTakerCommission(rootUserInfo.getTakerCommission());
        // 买方交易手续费
        marginUserInfo.setBuyerCommission(rootUserInfo.getBuyerCommission());
        // 卖方交易手续费
        marginUserInfo.setSellerCommission(rootUserInfo.getSellerCommission());
        // 单日最大出金总金额
        marginUserInfo.setDailyWithdrawCap(rootUserInfo.getDailyWithdrawCap());
        // 单日最大出金次数
        marginUserInfo.setDailyWithdrawCountLimit(rootUserInfo.getDailyWithdrawCountLimit());
        // 免审核额度
        marginUserInfo.setAutoWithdrawAuditThreshold(rootUserInfo.getAutoWithdrawAuditThreshold());
        // 交易等级
        marginUserInfo.setTradeLevel(rootUserInfo.getTradeLevel());
        // 新返佣比例
        marginUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        marginUserInfo.setNickName("");
        marginUserInfo.setRemark("");
        marginUserInfo.setTrackSource(rootUserInfo.getTrackSource());
        marginUserInfo.setInsertTime(DateUtils.getNewDate());
        marginUserInfo.setUpdateTime(DateUtils.getNewDate());
        marginUserInfo.setAccountType(UserTypeEnum.ISOLATED_MARGIN.name());
        // 推荐人
        marginUserInfo.setAgentId(rootUserInfo.getAgentId());
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            marginUserInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(rootUserInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            marginUserInfo.setAgentId(null);
        }
        if (marginUserInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long defaultAgentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
            marginUserInfo.setAgentId(defaultAgentId);
        }
        log.info("register:插入userInfo信息");
        // 插入用户信息
        userInfoMapper.insertSelective(marginUserInfo);
        return marginUserInfo;
    }

    private void insertToAgentLog(Long parentUserId, Long currentUserId, String currentEmail) {
        UserAgentLog existParent = userAgentLogMapper.selectByReferralUserId(parentUserId);
        if (existParent == null) {
            return;
        }
        UserAgentLog userAgentLog = new UserAgentLog();
        userAgentLog.setAgentCode(existParent.getAgentCode());
        userAgentLog.setUserId(existParent.getUserId());
        userAgentLog.setReferralUser(currentUserId);
        userAgentLog.setReferralEmail(currentEmail);
        User user = userMapper.queryByEmail(currentEmail);
        if (user != null){
            userAgentLog.setUserType(com.binance.account.constants.enums.UserTypeEnum.getAccountType(user.getStatus()));
        }
        userAgentLogMapper.insertSelective(userAgentLog);
    }


    /**
     * 发送用户注册MQ消息至PNK同步数据（针对创建isolated margin账户）
     *
     * @param user
     * @param userInfo
     */
    public void sendRegisterMqMsgForIsolatedMargin(User user, UserInfo userInfo) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, user.getEmail());
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("registerToken", "");
        dataMsg.put("code", "");
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        if (WebUtils.getHttpServletRequest() != null) {
            dataMsg.put("ipAddress", WebUtils.getRequestIp());
        }
        dataMsg.put("tradingAccount", userInfo.getTradingAccount());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.CREATE_MARGIN, dataMsg);
        log.info("iMsgNotification sendRegisterMqMsgForIsolatedMargin:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        iMsgNotification.send(msg);
    }


    /**
     * 主账号绑定子账号
     *
     * @param rootUserId
     * @param isolatedMarginUserId
     * @param remark
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected int createIsolatedMarginUserBinding(final Long rootUserId, final Long isolatedMarginUserId, final String remark) {
        IsolatedMarginUserBinding isolatedMarginUserBinding = new IsolatedMarginUserBinding();
        isolatedMarginUserBinding.setRootUserId(rootUserId);
        isolatedMarginUserBinding.setIsolatedMarginUserId(isolatedMarginUserId);
        isolatedMarginUserBinding.setRemark(remark);
        return isolatedMarginUserBindingMapper.insert(isolatedMarginUserBinding);
    }






}
