package com.binance.account.vo.security.request;

import com.binance.account.vo.Page;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class UserOperationLogRequest extends Page implements Serializable {

    private static final long serialVersionUID = -2358755512717229786L;

    private Long userId;

    private String email;

    private String operation;

    private String ip;

    private String clientType;

    private String apikey;

    private String request;

    private String response;

    private Date requestTimeFrom;

    private Date requestTimeTo;

    private List<String> realIpList;

    private Integer likeSearch;

}
