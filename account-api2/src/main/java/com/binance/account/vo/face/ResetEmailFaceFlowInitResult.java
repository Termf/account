package com.binance.account.vo.face;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetEmailFaceFlowInitResult extends FaceFlowInitResult {

    private static final long serialVersionUID = -4885764011493078987L;

    public enum NextStep {

        KYC,

        FACE;
    }

    private NextStep nextStep;

}
