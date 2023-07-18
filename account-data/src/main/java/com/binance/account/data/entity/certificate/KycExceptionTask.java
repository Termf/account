package com.binance.account.data.entity.certificate;

import java.util.Date;

import com.binance.master.commons.ToString;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KycExceptionTask extends ToString{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3998701473241928058L;

	private Long userId;

    private String taskType;

    private String taskMemo;

    private String executeParam;

    private String executeStatus;

    private Date createTime;

    private Date updateTime;

    private Date executeTime;
}