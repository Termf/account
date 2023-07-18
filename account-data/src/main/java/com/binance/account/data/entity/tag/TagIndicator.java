package com.binance.account.data.entity.tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.binance.master.commons.ToString;

import lombok.Data;

/**
 * 标签用户表
 */
@Data
public class TagIndicator extends ToString {
    private static final long serialVersionUID = 5955972633039264985L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 标签ID
     */
    private Long tagId;
    /**
     * 标签类名称
     */
    private String categoryName;
    /**
     * 标签名称
     */
    private String tagName;
    /**
     * 值
     */
    private String value;
    /**
     * 备注
     */
    private String remark;
    /**
     * 最后修改人
     */
    private String lastUpdatedBy;
    /**
     * 标签详情
     */
    private List<TagIndicatorDetail> details = new ArrayList<>();

    private Date updateTime;

}
