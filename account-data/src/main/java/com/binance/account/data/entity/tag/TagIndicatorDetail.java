package com.binance.account.data.entity.tag;

import com.binance.master.commons.ToString;
import lombok.Data;

/**
 * @author lufei
 * @date 2018/9/28
 */
@Data
public class TagIndicatorDetail extends ToString {

    private static final long serialVersionUID = 1352219145955635091L;
    private Long id;
    /**
     * 用户标签关系ID
     */
    private Long indicatorId;
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
