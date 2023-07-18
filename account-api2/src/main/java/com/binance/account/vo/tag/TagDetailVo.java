package com.binance.account.vo.tag;

import com.binance.master.commons.ToString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lufei
 * @date 2018/9/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDetailVo extends ToString {
    private static final long serialVersionUID = -1299717389331312847L;
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
