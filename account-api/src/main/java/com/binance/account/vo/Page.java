package com.binance.account.vo;


import java.io.Serializable;

public class Page implements Serializable {
    private static final long serialVersionUID = 2373772276976425915L;
    public static final int NO_ROW_OFFSET = 0;
    public static final int NO_ROW_LIMIT = 2147483647;
    private int offset = 0;
    private int limit = 2147483647;

    public Page() {
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        if (offset <= 0) {
            offset = 0;
        }

        this.offset = offset;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
