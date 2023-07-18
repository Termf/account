package com.binance.account.vo.reset;

import com.binance.account.common.enums.UserSecurityResetAnswerResult;
import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author liliang1
 * @date 2019-01-23 11:03
 */
@Setter
@Getter
public class ResetAnswerLogVo extends ToString {

    private static final long serialVersionUID = 7299152482209424753L;

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
