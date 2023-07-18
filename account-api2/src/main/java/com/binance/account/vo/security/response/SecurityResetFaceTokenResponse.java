package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author liliang1
 * @date 2018-08-27 13:57
 */
@ApiModel("重置2FA 获取Face++ Token 结果")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SecurityResetFaceTokenResponse extends ToString {

    private static final long serialVersionUID = -335972279205838404L;

    @ApiModelProperty("做活体识别的页面链接")
    private String livenessUrl;

    @ApiModelProperty("本次的业务流水号")
    private String bizNo;

    @ApiModelProperty("错误信息")
    private String errorMessage;

}
