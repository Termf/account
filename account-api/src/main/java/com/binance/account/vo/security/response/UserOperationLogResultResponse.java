package com.binance.account.vo.security.response;

import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.operationlog.UserOperationLogVo;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("用户行为日志Response")
@Getter
@Setter
public class UserOperationLogResultResponse extends SearchResult<UserOperationLogVo> implements Serializable {

    private static final long serialVersionUID = 1577762353126572799L;

}
