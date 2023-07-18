package com.binance.account.vo.subuser.response;

import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("CheckRelationShipAndReturnSubUserResp")
@Data
public class CheckRelationShipAndReturnSubUserResp {

    private UserVo userVo;

    private UserInfoVo userInfoVo;

}
