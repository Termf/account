// Generated by the devefx compiler. DO NOT EDIT!
package com.binance.account.data.entity.user;

import com.binance.master.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserMobileIndex implements Serializable {

    private static final long serialVersionUID = -2388372819700287433L;

    private String mobile;

    private String country;

    private Long userId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}