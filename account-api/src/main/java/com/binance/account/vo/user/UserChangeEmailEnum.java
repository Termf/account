package com.binance.account.vo.user;

public enum UserChangeEmailEnum {
    INIT(0,"初始化"),
    FACE_VALID(1,"face 已验证"),
    OLD_EMAIL_VALID(2,"老邮箱 已验证"),
    NEW_EMAIL_VALID(3,"新邮箱 已填写"),
    REVIEW(4,"审核中"),
    CANCEL(5,"已取消"),
    REFUSE(6,"拒绝"),
    PASS(7,"通过")
    ;


    UserChangeEmailEnum(int status, String name) {
        this.status = status;
        this.name = name;
    }

    private int status;

    private String name;

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

}
