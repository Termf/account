package com.binance.account.vo.certificate.response;

import com.binance.account.common.enums.WckChannelStatus;
import lombok.Data;

/**
 * @author mikiya.chen
 * @date 2020/3/3 9:55 下午
 */
@Data
public class WorldCheckInCheckoutApplyResponse {

    /**调用是否成功*/
    private Boolean success;

    /**调用失败信息*/
    private String errorMgs;

    /**之前是否申请过*/
    private Boolean isApplyBefore;

    /**之前申请的状态(如果之前没申请过 此值为空)*/
    private WckChannelStatus wckChannelStatusApplyBefore;

}
