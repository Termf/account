package com.binance.account.vo.user.response;

import com.binance.account.vo.user.UserAgentRewardVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@ApiModel("获取推荐人信息Response")
@Data
public class GetUserAgentDetailResponse implements Serializable {

    private static final long serialVersionUID = -348002278206055883L;
    private Long userId; // 用户Id
    private String email;//邮箱
    private Long parent; // 主账户
    private Long agentId; // 推荐人
    private BigDecimal agentRewardRatio; // 经纪人返佣比例
    private Long tradingAccount; // 用户交易账户
    private BigDecimal makerCommission; // 被动方手续费
    private BigDecimal takerCommission; // 主动方手续费
    private BigDecimal buyerCommission; // 买方交易手续费
    private BigDecimal sellerCommission; // 卖方交易手续费
    private BigDecimal dailyWithdrawCap; // 单日最大出金总金额
    private Integer dailyWithdrawCountLimit; // 单日最大出金次数
    private BigDecimal autoWithdrawAuditThreshold; // 免审核额度
    private String nickName; // 昵称
    private String remark; // 备注
    private String trackSource; // 注册渠道
    private Date updateTime; // 更新时间
    private Date insertTime; // 创建时间
    private Integer tradeLevel;//交易级别


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
