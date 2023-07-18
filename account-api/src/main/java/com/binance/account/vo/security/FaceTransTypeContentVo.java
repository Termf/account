package com.binance.account.vo.security;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2018-12-04 14:51
 */
@ApiModel("人脸识别操作类型验证结构提示标题内容")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class FaceTransTypeContentVo {

    @ApiModelProperty("提示头")
    private String title;
    @ApiModelProperty("提示内容")
    private String content;

}
