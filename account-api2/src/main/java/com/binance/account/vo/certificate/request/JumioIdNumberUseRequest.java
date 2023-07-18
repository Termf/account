package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2018-11-05 17:50
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class JumioIdNumberUseRequest extends ToString {
    private static final long serialVersionUID = 5058458996483487041L;

    @ApiModelProperty(required = true, notes = "用户id")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "证件ID号码")
    @NotNull
    private String idNumber;

    @ApiModelProperty(required = true, notes = "国家码")
    @NotNull
    private String countryCode;

    @ApiModelProperty(required = true, notes = "证件类型")
    private String idType;

}
