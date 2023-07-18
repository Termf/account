package com.binance.account.vo.user.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("获取用户邮箱Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class GetUserEmailResponse implements Serializable {

    private static final long serialVersionUID = 8661688712318209403L;

    @ApiModelProperty("邮箱")
    private String email;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
