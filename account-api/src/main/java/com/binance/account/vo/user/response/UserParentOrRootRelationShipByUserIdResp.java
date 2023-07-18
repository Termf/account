package com.binance.account.vo.user.response;

import com.binance.account.vo.user.enums.UserTypeEnum;
import lombok.Data;

@Data

public class UserParentOrRootRelationShipByUserIdResp {
    private Long rootUserId; // 主账户user

    private Long parentUserId; // 母账户userId

    private UserTypeEnum userTypeEnum;//用户类型

}
