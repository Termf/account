package com.binance.account.domain.bo;

/**
 * @author lufei
 * @date 2018/5/16
 */
public class TagTreeNode {
    private String id;
    private String text;
    private String state;
    private Object attributes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Object getAttributes() {
        return attributes;
    }

    public void setAttributes(Object attributes) {
        this.attributes = attributes;
    }
}
