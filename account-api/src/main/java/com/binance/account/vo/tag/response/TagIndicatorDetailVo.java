package com.binance.account.vo.tag.response;

import com.binance.master.commons.ToString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lufei
 * @date 2018/9/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagIndicatorDetailVo extends ToString {
    private static final long serialVersionUID = 1249625986620795392L;

    /**
     * 字段
     */
    private String field;
    /**
     * 字段值
     */
    private String value;
    /**
     * 属性
     */
    private String attr;
    /**
     * 类型
     */
    private String type;
    /**
     * 范围
     */
    private String range;
    /**
     * 是否必填
     */
    private String must;

}
