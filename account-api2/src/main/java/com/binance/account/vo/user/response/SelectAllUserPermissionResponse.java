package com.binance.account.vo.user.response;

import com.binance.account.vo.user.UserCommonPermissionVo;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("SelectAllUserPermissionResponse")
@Data
public class SelectAllUserPermissionResponse {

    private List<UserCommonPermissionVo> userCommonPermissionVoList= Lists.newArrayList();
}
