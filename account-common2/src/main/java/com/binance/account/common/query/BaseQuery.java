package com.binance.account.common.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class BaseQuery extends Pagination {

    private static final long serialVersionUID = 8490224720514351627L;

    private String id;

    private Long userId;

    private Set<Long> userIds;

    private String email;

    private String status;

    private String type;

    private Date startCreateTime;

    private Date endCreateTime;

    public String getEmail() {
        return email==null ? null : email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
