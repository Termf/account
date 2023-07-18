package com.binance.account.vo.user.ex;

import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.util.BitUtils;
import com.binance.master.commons.ToString;
import com.binance.master.constant.Constant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户转态
 *
 * @author wang-bijie
 */
@ApiModel(description = "用户状态", value = "用户状态")
@Getter
@Setter
@NoArgsConstructor
public class UserStatusEx extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 745013793286943653L;

    @ApiModelProperty(readOnly = true, notes = "用户是否激活")
    private Boolean isUserActive;

    @ApiModelProperty(readOnly = true, notes = "用户是否禁用")
    private Boolean isUserDisabled;

    @ApiModelProperty(readOnly = true, notes = "用户是否禁用登录")
    private Boolean isUserDisabledLogin;

    @ApiModelProperty(readOnly = true, notes = "用户锁定")
    private Boolean isUserLock;

    @ApiModelProperty(readOnly = true, notes = "特殊用户")
    private Boolean isUserSpecial;

    @ApiModelProperty(readOnly = true, notes = "种子用户")
    private Boolean isUserSend;

    @ApiModelProperty(readOnly = true, notes = "协议确定")
    private Boolean isUserProtocol;

    @ApiModelProperty(readOnly = true, notes = "用户手机验证")
    private Boolean isUserMobile;

    @ApiModelProperty(readOnly = true, notes = "用户强制修改密码")
    private Boolean isUserForcedPassword;

    @ApiModelProperty(readOnly = true, notes = "是否是子账号")
    private Boolean isSubUser;

    @ApiModelProperty(readOnly = true, notes = "子账户是否被母账户启用")
    private Boolean isSubUserEnabled;

    @ApiModelProperty(readOnly = true, notes = "是否是资管子账户")
    private Boolean isAssetSubUser;

    @ApiModelProperty(readOnly = true, notes = "资管子账户是否被母账户启用")
    private Boolean isAssetSubUserEnabled;

    @ApiModelProperty(readOnly = true, notes = "申购是否禁用")
    private Boolean isUserPurchase;

    @ApiModelProperty(readOnly = true, notes = "交易是否禁用")
    private Boolean isUserTrade;

    @ApiModelProperty(readOnly = true, notes = "子母账户功能是否开启")
    private Boolean isSubUserFunctionEnabled;

    @ApiModelProperty(readOnly = true, notes = "app交易是否禁用")
    private Boolean isUserTradeApp;

    @ApiModelProperty(readOnly = true, notes = "api交易是否禁用")
    private Boolean isUserTradeApi;

    @ApiModelProperty(readOnly = true, notes = "BNB手续费开关是否开启")
    private Boolean isUserFee;

    @ApiModelProperty(readOnly = true, notes = "是否开启谷歌2次验证")
    private Boolean isUserGoogle;

    @ApiModelProperty(readOnly = true, notes = "是否开启提币白名单")
    private Boolean isWithdrawWhite;

    @ApiModelProperty(readOnly = true, notes = "是否删除用户")
    private Boolean isUserDelete;

    @ApiModelProperty(readOnly = true, notes = "用户是否实名认证")
    private Boolean isUserCertification;

    @ApiModelProperty(readOnly = true, notes = "用户实名认证类型。0：个人，1：企业")
    private Boolean userCertificationType;


    @ApiModelProperty(readOnly = true, notes = "是否是margin_user")
    private Boolean isMarginUser;

    @ApiModelProperty(readOnly = true, notes = "是否拥有margin账户")
    private Boolean isExistMarginAccount;

    @ApiModelProperty(readOnly = true, notes = "是否是fiat_user")
    private Boolean isFiatUser;

    @ApiModelProperty(readOnly = true, notes = "是否拥有主站fiat账户")
    private Boolean isExistFiatAccount;

    @ApiModelProperty(readOnly = true, notes = "是否是future_user")
    private Boolean isFutureUser;

    @ApiModelProperty(readOnly = true, notes = "是否拥有future账户")
    private Boolean isExistFutureAccount;
    @ApiModelProperty(readOnly = true, notes = "是否签署开通法币交易协议")
    private Boolean isFiatProtocolConfirm;

    @ApiModelProperty(readOnly = true, notes = "是否禁止期货内部划转")
    private Boolean disableFutureInternalTransfer;

    @ApiModelProperty(readOnly = true, notes = "broker子母账户功能是否开启")
    private Boolean isBrokerSubUserFunctionEnabled;

    @ApiModelProperty(readOnly = true, notes = "是否是broker子账号")
    private Boolean isBrokerSubUser;

    @ApiModelProperty(readOnly = true, notes = "broker子账户是否被broker母账户启用")
    private Boolean isBrokerSubUserEnabled;

    @ApiModelProperty(readOnly = true, notes = "是否开启快速提币")
    private Boolean userFastWithdrawEnabled;

    @ApiModelProperty(value = "是否提交过返佣设置的表格")
    private Boolean isReferralSettingSubmitted;

    @ApiModelProperty(value = "是否禁止broker划转")
    private Boolean isForbiddenBrokerTrasnfer;



    @ApiModelProperty(readOnly = true, notes = "是否是矿池账户")
    private Boolean isMiningUser;

    @ApiModelProperty(readOnly = true, notes = "是否拥有矿池账户")
    private Boolean isExistMiningAccount;

    @ApiModelProperty(value = "是否开启资金密码")
    private Boolean isUserFundPassword;

    @ApiModelProperty(readOnly = true, notes = "是否签署leverage token风险协议")
    private Boolean isSignedLVTRiskAgreement;


    @ApiModelProperty(readOnly = true, notes = "是否是isolated_margin_user")
    private Boolean isIsolatedMarginUser;

    @ApiModelProperty(readOnly = true, notes = "是否拥有isolated_margin账户")
    private Boolean isExistIsolatedMarginAccount;

    @ApiModelProperty(readOnly = true, notes = "是否是无邮箱子账号")
    private Boolean isNoEmailSubUser;



    @ApiModelProperty(readOnly = true, notes = "是否是手机号注册的用户")
    private Boolean isMobileUser;

    @ApiModelProperty(readOnly = true, notes = "是否没有绑定邮箱")
    private Boolean isUserNotBindEmail;


    @ApiModelProperty(readOnly = true, notes = "是否是一键注册用户")
    private Boolean isOneButtonRegisterUser;

    @ApiModelProperty(readOnly = true, notes = "isCardUser")
    private Boolean isCardUser;
    @ApiModelProperty(readOnly = true, notes = "是否拥有Card账户")
    private Boolean isExistCardAccount;

    @ApiModelProperty(readOnly = true, notes = "是否是WaasUser")
    private Boolean isWaasUser;

    public UserStatusEx(Long status) {
        super();
        this.isUserActive = BitUtils.isTrue(status, Constant.USER_ACTIVE);
        this.isUserDisabled = BitUtils.isTrue(status, Constant.USER_DISABLED);
        this.isUserLock = BitUtils.isTrue(status, Constant.USER_LOCK);
        this.isUserSpecial = BitUtils.isTrue(status, Constant.USER_SPECIAL);
        this.isUserSend = BitUtils.isTrue(status, Constant.USER_SEND);
        this.isUserProtocol = BitUtils.isTrue(status, Constant.USER_PROTOCOL);
        this.isUserMobile = BitUtils.isTrue(status, Constant.USER_MOBILE);
        this.isUserForcedPassword = BitUtils.isTrue(status, Constant.USER_FORCED_PASSWORD);
        this.isSubUser = BitUtils.isTrue(status, Constant.USER_IS_SUBUSER);
        this.isSubUserEnabled = BitUtils.isTrue(status, Constant.USER_IS_SUB_USER_ENABLED);

        this.isAssetSubUser = BitUtils.isTrue(status, Constant.USER_IS_ASSET_SUBUSER);
        this.isAssetSubUserEnabled = BitUtils.isTrue(status, Constant.USER_IS_ASSET_SUB_USER_ENABLED);

        this.isUserPurchase = BitUtils.isTrue(status, Constant.USER_PURCHASE);
        this.isUserTrade = BitUtils.isTrue(status, Constant.USER_TRADE);
        this.isSubUserFunctionEnabled = BitUtils.isTrue(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        this.isUserTradeApp = BitUtils.isTrue(status, Constant.USER_TRADE_APP);
        this.isUserTradeApi = BitUtils.isTrue(status, Constant.USER_TRADE_API);
        this.isUserFee = BitUtils.isTrue(status, Constant.USER_FEE);
        this.isUserGoogle = BitUtils.isTrue(status, Constant.USER_GOOGLE);
        this.isWithdrawWhite = BitUtils.isTrue(status, Constant.USER_WITHDRAW_WHITE);
        this.isUserDelete = BitUtils.isTrue(status, Constant.USER_DELETE);
        this.isUserCertification = BitUtils.isTrue(status, Constant.USER_CERTIFICATION);
        this.userCertificationType = BitUtils.isTrue(status, Constant.USER_CERTIFICATION_TYPE);
        this.isMarginUser = BitUtils.isTrue(status, Constant.USER_IS_MARGIN_USER);
        this.isExistMarginAccount = BitUtils.isTrue(status, Constant.USER_IS_EXIST_MARGIN_ACCOUNT);
        this.isFiatUser = BitUtils.isTrue(status, Constant.USER_IS_FIAT_USER);
        this.isExistFiatAccount = BitUtils.isTrue(status, Constant.USER_IS_EXIST_FIAT_ACCOUNT);
        this.isUserDisabledLogin = BitUtils.isTrue(status, Constant.USER_LOGIN);
        this.isFutureUser = BitUtils.isTrue(status,  Constant.USER_IS_FUTURE_USER);
        this.isExistFutureAccount = BitUtils.isTrue(status,  Constant.USER_IS_EXIST_FUTURE_ACCOUNT);
        this.disableFutureInternalTransfer = BitUtils.isTrue(status, Constant.DISABLE_FUTURE_INTERNAL_TRANSFER);

        this.isBrokerSubUserFunctionEnabled = BitUtils.isTrue(status, Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED);
        this.isBrokerSubUser = BitUtils.isTrue(status, Constant.USER_IS_BROKER_SUBUSER);
        this.isBrokerSubUserEnabled = BitUtils.isTrue(status, Constant.USER_IS_BROKER_SUB_USER_ENABLED);
        this.isFiatProtocolConfirm = BitUtils.isTrue(status, Constant.FIAT_PROTOCOL_CONFIRM);
        this.userFastWithdrawEnabled = !BitUtils.isTrue(status, Constant.USER_FAST_WITHDRAW_ENABLED);
        this.isReferralSettingSubmitted = BitUtils.isTrue(status, Constant.USER_TRADE_COMMISSION_ENABLED);

        this.isIsolatedMarginUser = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_ISOLATED_MARGIN_USER);
        this.isExistIsolatedMarginAccount = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_EXIST_ISOLATED_MARGIN_ACCOUNT);
        this.isExistCardAccount = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_EXIST_CARD_ACCOUNT);


        this.isForbiddenBrokerTrasnfer = BitUtils.isTrue(status, AccountCommonConstant.FORBIDDEN_BROKER_TRANSFER);


        this.isMiningUser = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_MINING_USER);
        this.isCardUser = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_CARD_USER);
        this.isExistMiningAccount = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_EXIST_MINING_ACCOUNT);

        this.isIsolatedMarginUser = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_ISOLATED_MARGIN_USER);
        this.isExistIsolatedMarginAccount = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_EXIST_ISOLATED_MARGIN_ACCOUNT);






        this.isNoEmailSubUser = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_NO_EMAIL_SUB_USER);



        this.isUserFundPassword = BitUtils.isTrue(status, AccountCommonConstant.USER_FUND_PASSWORD);

        this.isSignedLVTRiskAgreement = BitUtils.isTrue(status, AccountCommonConstant.SIGNED_LVT_RISK_AGREEMENT);



        this.isMobileUser = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_MOBILE_USER);
        this.isUserNotBindEmail = BitUtils.isTrue(status, AccountCommonConstant.USER_NOT_BIND_EMAIL);

        this.isWaasUser = BitUtils.isTrue(status, AccountCommonConstant.USER_IS_WAAS_USER);
        this.isOneButtonRegisterUser = BitUtils.isTrue(status, AccountCommonConstant.ONE_BUTTON_REGISTER_USER);
    }
}
