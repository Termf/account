package com.binance.account.vo.country;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

@ApiModel("国家黑名单")
@Data
@NoArgsConstructor
public class CountryBlacklistRequest {

    @ApiModelProperty("国家二位字母代码（ISO 3166-1 alpha-2），对应country.code字段")
    @NotEmpty
    @Length(min = 2, max = 2, message = "countryCode必须是2位国家字母代码（ISO 3166-1 alpha-2）")
    private String countryCode;

    @ApiModelProperty("true.开启")
    private Boolean isActive;

    @ApiModelProperty("备注")
    private String memo;

}