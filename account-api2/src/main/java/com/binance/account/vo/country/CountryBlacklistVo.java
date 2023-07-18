package com.binance.account.vo.country;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("国家黑名单")
@Data
@NoArgsConstructor
public class CountryBlacklistVo {

    @ApiModelProperty("国家二位字母代码（ISO 3166-1 alpha-2），对应country.code字段")
    private String countryCode;

    @ApiModelProperty("true.开启")
    private Boolean isActive;

    @ApiModelProperty("国家三位字母代码（ISO 3166-1 alpha-3），对应country.code2字段")
    private String countryCode3;

    @ApiModelProperty("国家电话号段")
    private String mobileCode;

    private String countryNameCn;

    private String countryNameEn;

    @ApiModelProperty("备注")
    private String memo;

    private Date createTime;

    private Date updateTime;
}