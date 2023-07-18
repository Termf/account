package com.binance.account.vo.security.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class FindTodaysUserOperationLogsRequest implements Serializable {

    private static final long serialVersionUID = 4025182198374666644L;

    private String operation;

    private String ip;

    private Long excludeUserId;
}
