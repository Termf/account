package com.binance.account.vo.user.response;

import com.binance.account.vo.user.ex.SearchUserListEx;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@ApiModel(description = "搜索用户Response", value = "搜索用户Response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchUserListResponse extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 6848790067218398411L;

    private List<SearchUserListEx> searchUserList;

    private Long count;

}
