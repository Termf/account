package com.binance.account.vo.subuser.request;

import com.binance.account.vo.user.request.RegisterUserRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("注册资管子账户Request")
@Getter
@Setter
public class CreateAssetManagerSubUserReq extends RegisterUserRequest {

	@ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "资管子账户")
    @NotNull
    private Long assetSubUserId;

}