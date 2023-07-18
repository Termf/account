package com.binance.account.vo.user.response;

import com.binance.account.vo.user.BaseUserEmailChangeVo;
import lombok.Data;
import java.util.List;

@Data
public class UserEmailChangeResponse {

    private int totalCount;

    private List<BaseUserEmailChangeVo> datas;
}
