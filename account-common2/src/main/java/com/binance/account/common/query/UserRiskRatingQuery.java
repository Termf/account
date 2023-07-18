package com.binance.account.common.query;

import lombok.Data;
import lombok.ToString;

/**
 * Created by zhangxi on 2019/2/13.
 */
@Data
@ToString
public class UserRiskRatingQuery extends BaseQuery {


    /**
     * 
     */
    private static final long serialVersionUID = 3979740445516352498L;
    private String flag;

}
