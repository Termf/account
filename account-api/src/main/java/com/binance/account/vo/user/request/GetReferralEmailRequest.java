package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by yangyang on 2019/7/11.
 */
@ApiModel("获取被推荐者email")
@Getter
@Setter
public class GetReferralEmailRequest implements Serializable {

    @NotEmpty
    private String agentCode;

    @NotNull
    private Long userId;

    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Max(100)
    private Integer rows;
}
