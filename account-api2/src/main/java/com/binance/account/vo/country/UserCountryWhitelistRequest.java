package com.binance.account.vo.country;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
public class UserCountryWhitelistRequest {

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("备注，最大长度为512字符")
    @Length(max = 512)
    private String memo;

    @ApiModelProperty("过期时间")
    private Date expireTime;

}