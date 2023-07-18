package com.binance.account.data.entity.certificate;

import lombok.Data;

import java.util.Date;

/**
 * 老的KYC审核，JUMIO重新扫描失败记录
 * @author lw
 *         <p>
 *         2018/07/09
 */
@Data
public class UserKycRescanFail {

    private long userId;

    private Date createTime;

}