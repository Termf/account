package com.binance.account.data.entity.user;

import com.binance.master.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 做市商
 *
 * @author zhaochenkai
 * @date 2019-11-05
 */
@Getter
@Setter
public class MarketMakerUser implements Serializable {

    private static final long serialVersionUID = 2305192552526120808L;

    private Long userId;
    private String email;
    private String remark;
    private Date createTime;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }

}
