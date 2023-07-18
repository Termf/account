package com.binance.account.vo.user.request;

import com.google.common.collect.Lists;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Created by yangyang on 2019/11/7.
 */
@Data
public class SelectRootUserRequest {

    /**
     * 可能是future、margin、card
     */
    @NotEmpty
    private List<Long> userIds = Lists.newArrayList();


    /**
     * null 或者 0返回rootUserId
     * 1返回brokerSubAccountId
     * 如果是1，相当于通过future、margin这些获取到用户的brokerSubaccountId
     */
    private Integer resType;


    private Long parentUserId;

}
