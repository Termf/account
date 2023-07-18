package com.binance.account.domain.bo;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Date;
import java.util.List;


@Getter
@Setter
public class UserOperationLogQueryObject {

    private Long userId;

    private String email;

    private Collection<String> operations;

    private String ip;

    private String clientType;

    private String apikey;

    private String request;

    private String response;

    private String responseStatus;

    private Date requestTimeFrom;

    private Date requestTimeTo;

    private List<String> realIpList;

    private Integer likeSearch;

    private Integer limit;

    private Integer offset;

    // failReason is not null or responseStatus == true
    private boolean successOrHavingFailReason;

    // failReason is not null
    private boolean havingFailReason;
}
