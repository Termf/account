// Generated by the devefx compiler. DO NOT EDIT!
package com.binance.account.data.entity.user;

import com.binance.master.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * UserIp
 *
 * @date 2018-01-09 11:49:50
 */
@Getter
@Setter
@AllArgsConstructor
public class UserIp implements Serializable {

    private static final long serialVersionUID = 1473816277651908608L;
    private Long userId; // 用户id
    private String ip; // ip
    private Date insertTime;

    public UserIp() {
        super();
    }

    public UserIp(Long userId, String ip) {
        super();
        this.userId = userId;
        this.ip = ip;
    }

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
