package com.binance.account.vo.reset;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.vo.reset.response.ResetUserAnswerBody.ResetAnswersInfo;
import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author liliang1
 * @date 2019-01-23 20:04
 */
@Setter
@Getter
public class UserSecurityResetVo extends ToString {

    private static final long serialVersionUID = -6838532382460990523L;

    private String id;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    private UserSecurityResetType type;

    private UserSecurityResetStatus status;

    private Integer certificateType;

    private String front;

    private String back;

    private String hand;

    private Date auditTime;

    private String auditMsg;

    private Integer questionFailTimes;

    private Integer questionSeq;

    private Integer questionScore;

    private String scanReference;

    private String jumioToken;

    private String jumioStatus;

    private String issuingCountry;

    private String idNumber;

    private String documentType;

    private String applyIp;

    private String jumioIp;

    private String faceIp;

    private String failReason;

    private String faceStatus;

    private String faceRemark;


    // 列表查询结果附加信息
    private String email;

    private String userRemark;

    private String withdrawModifyCause;

    private List<ResetAnswerLogVo> answerLogs;

    //新重置答题流程下的答题记录,老的兼容保持不变（不在当前返回对应的列表）
    @Deprecated
    private List<ResetAnswersInfo> newAnswerLogs;
    // 新重置答题次数
    private Integer answerCount;
}
