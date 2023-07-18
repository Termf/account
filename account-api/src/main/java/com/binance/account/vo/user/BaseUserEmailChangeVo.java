package com.binance.account.vo.user;

import lombok.Data;

import java.util.Date;

@Data
public class BaseUserEmailChangeVo {
    private Long id;

    private Long userId;

    //流程id
    private String flowId;

    //0: 初始化流程 1:face 已验证 2:老邮箱 已验证 3:新邮箱 已验证 4:审核中 5:已取消 6:拒绝 7:通过
    private Byte status;

    private String oldEmail;

    private String newEmail;

    private Date createdAt;

    private Date updatedAt;

    //用户备注
    private String userRemark;

    //提现额度备注
    private String withdrawalRemark;

    //失败原因
    private String failReason;

    private Byte availableType;//0:老邮箱可用， 1：老邮箱不可用

}
