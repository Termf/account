package com.binance.account.data.entity.user;

import lombok.Data;

/**
 * 只有账户的上下级关系
 */
@Data
public class RelationUserInfo {

    private Long parentUserId;

    private Long userId;

}
