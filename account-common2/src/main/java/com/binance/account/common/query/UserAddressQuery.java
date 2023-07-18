package com.binance.account.common.query;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * Created by alex on 2018/7/20.
 */
@Data
@ToString
public class UserAddressQuery extends BaseQuery {

    private static final long serialVersionUID = 23857129882919234L;

    private String firstName;

    private String lastName;

    private String country;

    private String city;

    private Date startCreateTime;

    private Date endCreateTime;

}
