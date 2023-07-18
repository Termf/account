package com.binance.account.vo.user.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApiModel("GetUserCommissionDetailResponseResponse")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class GetUserCommissionDetailResponse implements Serializable {
    @ApiModelProperty(value = "总推荐人数")
    private Long agentCount;
    @ApiModelProperty(value = "推荐人信息")
    private List<Agent> agents;



    @Data
    public static class Agent{
        private Date time;
        private String email;
        private String ts;
    }
    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
