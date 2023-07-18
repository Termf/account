package com.binance.account.service.user;

import com.binance.account.vo.tag.response.EmailAndUserIdVo;
import com.binance.account.vo.tag.response.StatusAndUserIdVo;
import com.binance.account.vo.user.request.GetUserListRequest;

import java.util.List;

public interface IBigCustomer {

    List<EmailAndUserIdVo> emailAndTag(GetUserListRequest request);

    List<StatusAndUserIdVo> findUserStatus(GetUserListRequest request);

}
