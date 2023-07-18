package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-04-25 10:46
 */
@ApiModel("强制使通过的KYC变更到过期状态")
@Setter
@Getter
public class KycForceToExpiredRequest extends ToString {

    private static final long serialVersionUID = 2140385139195479256L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("KYC ID")
    @NotNull
    private Long kycId;

    /**
     * KYC类型 user:个人认证 company:企业认证
     */
    @ApiModelProperty("KYC类型：user/company")
    @NotNull
    private String type;

    /**
     * 拒绝描述信息
     */
    @ApiModelProperty("错误描述")
    @NotNull
    private String failReason;

}
