package com.binance.account.data.entity.user;

import com.binance.master.utils.StringUtils;
import lombok.Data;

@Data
public class FutureInvitationLog {
    private String invitationCode; //
    private Integer status;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
