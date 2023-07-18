package com.binance.account.service.user;

import com.binance.account.vo.user.request.SearchUserListRequest;
import com.binance.account.vo.user.response.SearchUserListResponse;
import com.binance.master.models.APIResponse;

/**
 * @author lufei
 * @date 2018/8/3
 */
public interface IUserSearch {

    APIResponse<SearchUserListResponse> searchUserList(SearchUserListRequest request);

}
