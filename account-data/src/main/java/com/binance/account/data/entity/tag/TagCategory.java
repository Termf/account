package com.binance.account.data.entity.tag;

import java.util.Date;

import com.binance.master.commons.ToString;

import lombok.Data;

/**
 * 标签组
 */
@Data
public class TagCategory extends ToString {

    private static final long serialVersionUID = -8128522000084119005L;
    /**
     * 标签组ID
     */
    private Long id;
    /**
     * 父标签组ID
     */
    private Long pid;
    /**
     * 标签组名称
     */
    private String name;

    private Date createTime;

    private Date updateTime;

}
