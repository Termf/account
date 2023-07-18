package com.binance.account.vo.user.request;

import com.binance.account.vo.user.BaseUserEmailChangeVo;
import lombok.Data;

import java.util.Date;

@Data
public class UserEmailChangeRequest extends BaseUserEmailChangeVo {

    private int limit;

    private int offset;

    private String start;

    private String end;

}
