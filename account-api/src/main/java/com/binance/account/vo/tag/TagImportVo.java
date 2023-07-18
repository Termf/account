package com.binance.account.vo.tag;

import com.binance.master.commons.ToString;

import lombok.Data;

/**
 * @author lufei
 * @date 2018/6/25
 */
@Data
public class TagImportVo extends ToString {
    private static final long serialVersionUID = -8059698267720360048L;
    /**
     * 客户号
     */
    private String userId;
    /**
     * 标签名
     */
    private String tagName;
    /**
     * 标签值
     */
    private String value;
    /**
     * 备注
     */
    private String remark;

    private String tid;

    /**
     * 标签详情字段0
     */
    private String f0;
    private String f1;
    private String f2;
    private String f3;
    private String f4;
    private String f5;
    private String f6;
    private String f7;
    private String f8;
    private String f9;

}
