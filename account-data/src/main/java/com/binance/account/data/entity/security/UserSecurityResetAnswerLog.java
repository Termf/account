package com.binance.account.data.entity.security;

import com.binance.account.common.enums.UserSecurityResetAnswerResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 重置流程的答题结果
 * @author liliang1
 */
@Setter
@Getter
public class UserSecurityResetAnswerLog implements Serializable {
    private static final long serialVersionUID = 2928013251495882652L;

    private Long id;

    private String resetId;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    private UserSecurityResetAnswerResult result;

    private Integer questionSeq;

    private Integer questionScore;

    private Integer totalScore;

    private String answer;
}