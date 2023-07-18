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

    @NotEmpty
    private List<Long> userIs = Lists.newArrayList();


    /**
     * null 或者 0返回rootUserId
     * 1返回brokerSubAccountId
     */
    private Integer resType;

    private Long parentUserId;

}
