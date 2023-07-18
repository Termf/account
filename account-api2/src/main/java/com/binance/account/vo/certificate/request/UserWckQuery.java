package com.binance.account.vo.certificate.request;

import com.binance.account.common.enums.WckStatus;
import com.binance.account.common.query.Pagination;
import lombok.Getter;
import lombok.Setter;

/**
 * world check 翻页查询
 * Created by Shining.Cai on 2018/09/10.
 **/
@Getter
@Setter
public class UserWckQuery extends Pagination {

    private Long userId;
    private String email;

    private WckStatus status;

    private Long auditorId;
    private Integer auditorSeq;
    
    private String country;
}
