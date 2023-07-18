package com.binance.account.vo.tag.response;

import java.util.Date;

import com.binance.master.commons.ToString;

import lombok.Data;

/**
 * 标签用户表
 * @author lufei
 */
@Data
public class TagIndicatorResponse extends ToString {
    private static final long serialVersionUID = -2078126336844428705L;
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

    private Date updateTime;

    private String email;
}
