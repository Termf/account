package com.binance.account.data.entity.tag;

import com.binance.master.commons.ToString;
import lombok.Data;

/**
 * @author lufei
 * @date 2018/9/28
 */
@Data
public class TagDetailDefine extends ToString {
    private static final long serialVersionUID = -805431629534885989L;
    private Integer id;
    private Long tagId;
    /**
     * 详情字段0名称
     */
    private String f0Name;
    /**
     * 详情字段0类型
     */
    private String f0Type;
    /**
     * 详情字段0范围
     */
    private String f0Range;
    /**
     * 详情字段0是否必填
     */
    private String f0Must;

    private String f1Name;
    private String f1Type;
    private String f1Range;
    private String f1Must;
    private String f2Name;
    private String f2Type;
    private String f2Range;
    private String f2Must;
    private String f3Name;
    private String f3Type;
    private String f3Range;
    private String f3Must;
    private String f4Name;
    private String f4Type;
    private String f4Range;
    private String f4Must;
    private String f5Name;
    private String f5Type;
    private String f5Range;
    private String f5Must;
    private String f6Name;
    private String f6Type;
    private String f6Range;
    private String f6Must;
    private String f7Name;
    private String f7Type;
    private String f7Range;
    private String f7Must;
    private String f8Name;
    private String f8Type;
    private String f8Range;
    private String f8Must;
    private String f9Name;
    private String f9Type;
    private String f9Range;
    private String f9Must;

}
