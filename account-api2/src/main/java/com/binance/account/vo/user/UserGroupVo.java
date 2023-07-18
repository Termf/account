package com.binance.account.vo.user;

import lombok.Data;

import java.util.Set;

@Data
public class UserGroupVo {

    private Long parentUserId;

    private Set<Long> userIds;

}
