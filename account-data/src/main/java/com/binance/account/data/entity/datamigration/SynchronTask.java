package com.binance.account.data.entity.datamigration;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SynchronTask extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = -5406947564906723522L;

    private Long id;

    private Long begin;

    private Long end;

    private Date insertTime;

    private Date updateTime;

}
