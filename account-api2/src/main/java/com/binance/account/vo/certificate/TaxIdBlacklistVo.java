package com.binance.account.vo.certificate;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TaxIdBlacklistVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    // taxId
    private String taxId;
    // 操作者
    private String creator;
    // 备注
    private String remark;
    // 创建时间
    private Date createTime;
}
