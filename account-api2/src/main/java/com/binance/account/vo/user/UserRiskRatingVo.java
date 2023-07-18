package com.binance.account.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhangxi
 */
@Data
public class UserRiskRatingVo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 5325249157830490656L;

    private Long id;

    private Long userId;

    private Integer customerRisk1Score;

    private Integer customerRisk2Score;
    
    private Integer nationalityRiskScore;
    
    private Integer residenceRiskScore;
    
    private Integer behaviourRiskScore;

    private Integer transactionValueScore;
    
    private Integer totalScore;
    
    private Date createTime;
    
    private String email;

   
}