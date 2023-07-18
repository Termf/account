package com.binance.account.domain.bo;

import com.binance.master.commons.ToString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountMsgNotification extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String routingKey;
    private Object data;

}
