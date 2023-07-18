package com.binance.account.vo.tag.response;

import java.util.Date;

import com.binance.master.commons.ToString;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签组
 */
@Data
@NoArgsConstructor
public class TagCategoryResponse extends ToString {

    private static final long serialVersionUID = 510638718284528450L;
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
    /**
     * 父标签组名称
     */
    private String pName;

}
