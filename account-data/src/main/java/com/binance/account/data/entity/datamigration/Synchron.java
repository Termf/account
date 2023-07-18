package com.binance.account.data.entity.datamigration;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Synchron extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = 6607714505072294459L;

    private Long userId;

    private Date insertTime;

}
