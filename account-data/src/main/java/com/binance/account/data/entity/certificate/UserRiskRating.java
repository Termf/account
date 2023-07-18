package com.binance.account.data.entity.certificate;

import java.util.Date;

import lombok.Data;

/**
 * 风险评级
 */
@Data
public class UserRiskRating{
    /**
     * 
     */
    private static final long serialVersionUID = -833591398431756696L;
    
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

}