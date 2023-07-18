package com.binance.account.vo.user.response;

import lombok.Data;

import java.util.List;

/**
 * @author lufei
 * @date 2019/3/8
 */
@Data
public class MarginUserTypeResponse {

    private UserType userType;
    private Long parentUserId;
    private List<Long> subUserIds;
    private Long marginUserId;

    public enum UserType {
        NORMAL,
        PARENT,
        SUB,
        MARGIN
    }
}
