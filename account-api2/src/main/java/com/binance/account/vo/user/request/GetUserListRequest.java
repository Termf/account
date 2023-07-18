package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * @author lufei
 * @date 2018/5/7
 */
@ApiModel("批量获取用户信息Request")
@Getter
@Setter
public class GetUserListRequest implements Serializable {

    private static final long serialVersionUID = 4460737869899157395L;

    @ApiModelProperty(name = "用户ID", required = true)
    @NotEmpty
    private List<Long> userIds;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
