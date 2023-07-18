package com.binance.account.vo.reset.response;

import com.binance.account.common.enums.ResetNextStep;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("重置流程的下一步操作")
@Setter
@Getter
public class Reset2faNextStepResponse implements Serializable {

    private static final long serialVersionUID = 5267484316056547748L;

    @ApiModelProperty("下一步")
    private ResetNextStep nextStep;

    @ApiModelProperty("业务流水号")
    private String transId;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("requestId")
    private String requestId;

    @ApiModelProperty("如果下一步是UPLOAD, 当前值存在的话可以直接去上传页，否则提示用户通过邮件点击进入下一步")
    private String uploadUrl;

    @ApiModelProperty("答题环节下剩余的答题次数")
    private Integer answerCount;

    /**
     * 是否还有下一笔流程（主要用在review状态下判断是否还有后续流程，还是只需要等待最后的审核）
     */
    private boolean haveNext;

    public Reset2faNextStepResponse() {
        super();
        this.requestId = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        this.haveNext = true;
    }

    public Reset2faNextStepResponse(ResetNextStep nextStep, String transId, String type) {
        super();
        this.requestId = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        this.nextStep = nextStep;
        this.transId = transId;
        this.type = type;
        this.haveNext = true;
    }


    public static class Builder {

        public static Reset2faNextStepResponse buildFaceStep(String transId, String faceType) {
            return new Reset2faNextStepResponse(ResetNextStep.FACE, transId, faceType);
        }

        public static Reset2faNextStepResponse buildQuestionStep(String transId, String type, Integer answerCount) {
            Reset2faNextStepResponse response = new Reset2faNextStepResponse(ResetNextStep.QUESTION, transId, type);
            response.setAnswerCount(answerCount);
            return response;
        }

        public static Reset2faNextStepResponse buildUploadStep(String transId, String type, String uploadUrl) {
            Reset2faNextStepResponse response = new Reset2faNextStepResponse(ResetNextStep.UPLOAD, transId, type);
            response.setUploadUrl(uploadUrl);
            return response;
        }

        public static Reset2faNextStepResponse buildReviewStep(boolean haveNext, String type) {
            Reset2faNextStepResponse response = new Reset2faNextStepResponse();
            response.setNextStep(ResetNextStep.REVIEW);
            response.setType(type);
            response.setHaveNext(haveNext);
            return response;
        }
    }

}
