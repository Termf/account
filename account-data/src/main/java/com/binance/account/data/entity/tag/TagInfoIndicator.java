package com.binance.account.data.entity.tag;

import java.math.BigDecimal;

import com.binance.master.commons.ToString;

import lombok.Data;

/**
 * @author lufei
 * @date 2018/5/15
 */
@Data
public class TagInfoIndicator extends ToString {
    private static final long serialVersionUID = 2271050161409740174L;
    /**
     * 标签用户表ID
     */
    private Long indicatorId;
    /**
     * 标签组表ID
     */
    private Long categoryId;
    /**
     * 标签组名称
     */
    private String categoryName;
    /**
     * 标签ID
     */
    private Long tagId;
    /**
     * 标签名称
     */
    private String tagName;
    /**
     * 标签最小值
     */
    private BigDecimal minValue;
    /**
     * 标签最大值
     */
    private BigDecimal maxValue;
    /**
     * 标签值
     */
    private String value;
    /**
     * 标签值范围
     */
    private String valueRange;
    /**
     * 用户标签值
     */
    private String indicatorValue;
    /**
     * 备注
     */
    private String remark;
    /**
     * 最后修改人
     */
    private String lastUpdatedBy;

}
