package com.binance.account.job;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.user.UserEmailChange;
import com.binance.account.data.mapper.user.UserEmailChangeMapper;
import com.binance.account.vo.user.UserChangeEmailEnum;
import com.binance.account.vo.user.request.UserEmailChangeRequest;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Log4j2
@JobHandler(value = "userEmailChangeCheckHandler")
@Component
public class UserEmailChangeCheckHandler extends IJobHandler {

    @Resource
    private UserEmailChangeMapper userEmailChangeMapper;

    @Resource
    private ApolloCommonConfig apolloCommonConfig;


    @Override
    public ReturnT<String> execute(String s) throws Exception {
        userEmailChangeMapper.updateStatusCancelByHour(new Date(), apolloCommonConfig.getChangeEmailReviewHour());

        List<UserEmailChange> userEmailChangeList = userEmailChangeMapper.findUndoneWithHours(apolloCommonConfig.getChangeEmailReviewHour());
        if (userEmailChangeList != null && !userEmailChangeList.isEmpty()) {
            for (UserEmailChange userEmailChange : userEmailChangeList) {
                if (userEmailChange == null) {
                    continue;
                }

                String key = RedisCacheUtils.get(userEmailChange.getFlowId());
                if (StringUtils.isBlank(key)) {
                    //失效了，结束流程节点
                    UserEmailChange change = new UserEmailChange();
                    change.setFlowId(userEmailChange.getFlowId());
                    change.setUpdatedAt(new Date());
                    change.setStatus(Byte.parseByte(UserChangeEmailEnum.CANCEL.getStatus() + ""));
                    userEmailChangeMapper.updateUserEmailChangeByFlowId(change);
                }

            }
        }

        return SUCCESS;
    }
}
