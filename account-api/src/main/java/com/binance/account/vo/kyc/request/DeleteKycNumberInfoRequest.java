package com.binance.account.vo.kyc.request;

import lombok.Data;

/**
 * @author mikiya.chen
 * @date 2020/1/17 4:41 下午
 */
@Data
public class DeleteKycNumberInfoRequest {

    private Long userId;

    private String number;

    private String type;

    private String country;

}
