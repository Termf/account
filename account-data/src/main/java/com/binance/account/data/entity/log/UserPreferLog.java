package com.binance.account.data.entity.log;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: mingming.sheng
 * @Date: 2020/3/19 2:20 下午
 */
@Data
public class UserPreferLog implements Serializable {

    private static final long serialVersionUID = 7943285873218151617L;
    private Long id;

    private Long userId;

    private String preferType;

    private String preferVal;

    private Date createTime;
}
