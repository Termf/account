package com.binance.account.vo.tag.response;

import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.master.commons.ToString;
import lombok.Data;

/**
 * @author lufei
 * @date 2019/2/21
 */
@Data
public class TagUserStatusVo extends ToString {

    private static final long serialVersionUID = 4818439441074165090L;

    private Long userId;

    private String tagName;

    private UserStatusEx userStatusEx;

}
