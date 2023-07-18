package com.binance.account.vo.tag.response;

import lombok.Data;

/**
 * @author lufei
 * @date 2018/6/21
 */
@Data
public class TagTreeResponse{
    private static final long serialVersionUID = -873721038465052424L;
    private String id;
    private String text;
    private String state = "open";
    private boolean checked = false;
    private Object attributes;
}
