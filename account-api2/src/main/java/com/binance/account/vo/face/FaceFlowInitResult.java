package com.binance.account.vo.face;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class FaceFlowInitResult implements Serializable {

    private static final long serialVersionUID = -7822098187123389492L;

    private String transId;

    private String type;

}
