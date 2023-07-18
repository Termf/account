package com.binance.account.vo.tag.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.binance.account.vo.tag.TagDetailVo;
import com.binance.master.commons.ToString;

import lombok.Data;

/**
 * 标签表
 */
@Data
public class TagInfoResponse extends ToString {
    private static final long serialVersionUID = 508204721281317186L;
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
    /**
     * 标签组名称
     */
    private String categoryName;

    /**
     * 父标签组ID
     */
    private Long pCategoryId;
    /**
     * 父标签名称
     */
    private String pName;
    /**
     * 父标签组名称
     */
    private String pCategoryName;

    /**
     * 值范围
     */
    private String valueRange;
    /**
     * 标签属性详情
     */
    private List<TagDetailVo> tagDetail;
}
