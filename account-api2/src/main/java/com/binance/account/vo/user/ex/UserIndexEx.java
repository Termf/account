package com.binance.account.vo.user.ex;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/6/8
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserIndexEx extends ToString {

    private static final long serialVersionUID = -8072141010728799064L;

    @ApiModelProperty(readOnly = true, notes = "用户ID")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "登录邮箱")
    private String email;

}
