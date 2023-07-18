package com.binance.account.vo.subuser.response;

import com.binance.account.vo.user.response.RegisterUserResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@Data
public class CreateSubUserResp extends RegisterUserResponse {

    @ApiModelProperty("母账号UserId")
    private Long parentUserId;
}