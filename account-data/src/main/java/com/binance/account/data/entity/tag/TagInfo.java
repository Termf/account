package com.binance.account.data.entity.tag;

import java.math.BigDecimal;
import java.util.Date;

import com.binance.master.commons.ToString;

import lombok.Data;

/**
 * 标签表
 */
@Data
public class TagInfo extends ToString {
    private static final long serialVersionUID = 2308289413371586436L;
    /**
     * 标签ID
     */
    private Long id;
    /**
     * 父标签ID
     */
    private Long pid;
    /**
     * 标签类ID
     */
    private Long categoryId;
    /**
     * 标签名称
     */
    private String name;
    /**
     * 最小值
     */
    private BigDecimal min;
    /**
     * 最大值
     */
    private BigDecimal max;
    /**
     * 标签值
     */
    private String value;

    private Date createTime;

    private Date updateTime;
}
