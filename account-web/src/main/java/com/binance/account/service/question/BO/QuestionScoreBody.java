package com.binance.account.service.question.BO;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class QuestionScoreBody {

    private List<QuestionScore> questionScoreList;

    private Double weightedPointTotal;//总分
    private Double passThresholdValue;//多少分及格
    private Boolean result;//是否过

    @Getter
    @Setter
    @ToString
    public static class QuestionScore {
        private String question;//1,2,3
        private Double point;//题目分数
        private Double weightedPoint; //加权之后分数
    }

}