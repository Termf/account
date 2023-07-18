package com.binance.account.data.entity.certificate;

import lombok.Data;

import java.util.Date;

/**
 * 老的KYC审核，JUMIO重新扫描
 * @author lw
 *         <p>
 *         2018/07/06
 */
@Data
public class UserKycRescan{

    private long userId;
    /**
     * jumio Token 用于查询状态
     */
    private String scanReference;

    private Date createTime;

    private int status;
}