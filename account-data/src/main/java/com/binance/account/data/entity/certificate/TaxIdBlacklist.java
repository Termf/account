package com.binance.account.data.entity.certificate;

import lombok.Data;

import java.util.Date;

@Data
public class TaxIdBlacklist {

    private Long id;

    private String taxId;

    private String creator;

    private String remark;

    private Date createTime;

    private Date updateTime;


}