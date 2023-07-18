package com.binance.account.vo.user;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author liliang1
 * @date 2018-10-17 20:02
 */
@Setter
@Getter
public class UserIpLikeVo extends ToString {

    private static final long serialVersionUID = 2355241415359988586L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("需要检查的IP对象")
    @NotNull
    private List<IpLikeVo> ipList;

    @Setter
    @Getter
    public static class IpLikeVo extends ToString {

        private static final long serialVersionUID = 8928591783712374411L;

        @ApiModelProperty("需要检查的IP")
        @NotNull
        private String ip;
        @ApiModelProperty("前三段是否能模糊匹配上 1-能对上，0-不能对上, 默认为0")
        private int exist = 0;
    }


}
