package com.binance.account.vo.device.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class FindMostSimilarUserDeviceResponse implements Serializable {
	private static final long serialVersionUID = 5452319325500086679L;

	/**
     * true：表示为相同设备
     */
    private boolean same;

    private double score;

    private UserDeviceVo matched;

}
