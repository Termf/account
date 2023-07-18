package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.SubUserEmailVo;
import lombok.Data;

import java.util.List;

/**
 * Created by Fei.Huang on 2018/10/19.
 */
@Data
public class SubUserTypeResponse {

    private UserType userType;

    private Long parentUserId;
    private List<Long> subUserIds;
    private List<SubUserEmailVo> subUserIdEmails;

    public enum UserType {
        NORMAL,
        PARENT,
        SUB
    }
}