package com.binance.account.vo.user.response;

import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.user.ex.OrderConfirmStatus;
import com.binance.account.vo.user.ex.UserSecurityKeyStatus;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BaseDetailResponse extends ToString {

    private static final long serialVersionUID = 6473498312315361723L;

    @ApiModelProperty(name = "账号")
    private String email;

    @ApiModelProperty(
            name = "用户状态：使用long类型的2进制占位可以有64个状态统一0:否1:是,1:激活状态,2:禁用状态,3:锁定状态,4:特殊用户,5:种子用户,6:协议是否确认,7:手机验证,8:强制修改密码,9:是否有子账号,10:是否可以申购,11:是否可以交易,12:微信绑定,13:APP是否可以交易,14:api是否可以交易,15:BNB手续费开关")
    private Long status;

    @ApiModelProperty(name = "推荐人")
    private Long agentId;

    @ApiModelProperty(name = "经纪人返佣比例")
    private BigDecimal agentRewardRatio;

    @ApiModelProperty(name = "被推荐人(当前userID)返佣比例")
    private BigDecimal referralRewardRatio;

    @ApiModelProperty(name = "被动方手续费")
    private BigDecimal makerCommission;

    @ApiModelProperty(name = "主动方手续费")
    private BigDecimal takerCommission;

    @ApiModelProperty(name = "买方交易手续费")
    private BigDecimal buyerCommission;

    @ApiModelProperty(name = "卖方交易手续费")
    private BigDecimal sellerCommission;

    @ApiModelProperty(name = "单日最大出金总金额")
    private BigDecimal dailyWithdrawCap;

    @ApiModelProperty(name = "手机国家编码")
    private String mobileCode;

    @ApiModelProperty(name = "手机")
    private String mobile;

    @ApiModelProperty(name = "用户安全级别:1:普通,2:身份认证,3:?")
    private Integer securityLevel;

    @ApiModelProperty(name = "0-审核中, 1-通过,2-拒绝")
    private Integer certificateStatus;

    @ApiModelProperty(name = "certificateMessage")
    private String certificateMessage;

    @ApiModelProperty(name = "认证类型 1个人 2企业")
    private Integer certificateType;

    @ApiModelProperty(name = "firstName")
    private String firstName;
    
    @ApiModelProperty(name = "middleName")
    private String middleName;

    @ApiModelProperty(name = "lastName")
    private String lastName;

    @ApiModelProperty(name = "companyName")
    private String companyName;

    @ApiModelProperty(name = "lastUserSecurityLog")
    private UserSecurityLogVo lastUserSecurityLog;

    @ApiModelProperty(name = "userStatusEx")
    private UserStatusEx userStatusEx;

    @ApiModelProperty(name = "交易级别")
    private Integer tradeLevel;

    @ApiModelProperty(name = "认证地址信息")
    private String certificateAddress;
    
    @ApiModelProperty(name = "防钓鱼码")
    private String antiPhishingCode; //

    @ApiModelProperty(name = "用户备注")
    private String remark;

    @ApiModelProperty(name = "margin账户的userid")
    private Long marginUserId;

    @ApiModelProperty(name = "主站法币账户的userid")
    private Long fiatUserId;

    @ApiModelProperty(name = "Security Key status")
    private UserSecurityKeyStatus securityKeyStatus;

    @ApiModelProperty(name = "下单确认配置状态")
    private OrderConfirmStatus orderConfirmStatus;
    @ApiModelProperty(name = "昵称")
    private String nickName;
    @ApiModelProperty(name = "昵称背景色")
    private String nickColor;

    @ApiModelProperty(name = "是否初始化了资金密码")
    private boolean InitFundPasswordOrNot;

    @ApiModelProperty(value = "不同级别对应的提现额度")
    private List<BigDecimal> levelWithdraw;
}
