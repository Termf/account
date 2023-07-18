package com.binance.account.common.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class WebAuthnAdminQuery implements Serializable {

    private static final long serialVersionUID = 1959971212072540813L;

    private Long userId;

    private String origin;

    private Integer page;

    private Integer pageSize;

    private int start;

    private int rows;

    public int getStart() {
        int all = this.getRows();
        return (this.page == null || this.page <= 0 ? 0 : this.page - 1) * all;
    }

    public int getRows() {
        return this.pageSize == null || this.pageSize <= 0 ? 20 : this.pageSize;
    }
}
