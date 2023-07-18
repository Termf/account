package com.binance.account.vo.user.response;

import com.binance.account.vo.user.ex.UserIndexEx;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author lufei
 * @date 2018/6/8
 */
@ApiModel("批量获取用户邮箱Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class GetUserEmailsResponse implements Serializable {

    private static final long serialVersionUID = 3199373350860361307L;

    private List<UserIndexEx> userIndexList;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }

}
