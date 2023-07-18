package com.binance.account.vo.certificate.request;

import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.common.query.Pagination;
import lombok.Getter;
import lombok.Setter;

/**
 * channel用户 world check 翻页查询
 * @author mikiya.chen
 * @date 2020/3/10 4:55 下午
 */
@Getter
@Setter
public class UserChannelWckQuery extends Pagination {

    private Long userId;
    private String email;

    private WckChannelStatus status;

    private Long auditorId;
    private Integer auditorSeq;

    private String country;

}
