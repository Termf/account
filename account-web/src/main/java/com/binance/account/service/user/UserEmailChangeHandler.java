package com.binance.account.service.user;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.util.Md5Utils;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.user.UserEmailChange;
import com.binance.account.data.mapper.user.UserEmailChangeMapper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.user.UserChangeEmailEnum;
import com.binance.master.utils.Md5Tools;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Log4j2
@Service
public class UserEmailChangeHandler {

    @Resource
    private UserEmailChangeMapper userEmailChangeMapper;

    @Autowired
    private UserCommonBusiness userCommonBusiness;

    @Resource
    private ApolloCommonConfig apolloCommonConfig;

    private static final String signWorld = "sharingordlw";
    public void updateStatus(String flowId) throws Exception {

        log.info("UserEmailChangeBusiness updateStatus flowId is {}", flowId);
        //根据flowId 查询
        UserEmailChange emailChange = userEmailChangeMapper.findByFlowId(flowId);

        log.info("UserEmailChangeBusiness updateStatus read db is {}", JSONObject.toJSONString(emailChange));
        //更新状态
        UserEmailChange userEmailChange = new UserEmailChange();
        userEmailChange.setFlowId(flowId);
        userEmailChange.setStatus(Byte.parseByte(UserChangeEmailEnum.FACE_VALID.getStatus() + ""));
        userEmailChange.setUpdatedAt(new Date());
        userEmailChangeMapper.updateUserEmailChangeByFlowId(userEmailChange);

        if (emailChange != null && emailChange.getUserId() != null && StringUtils.isNotBlank(emailChange.getOldEmail())) {
            //记录老邮箱link的时间
            RedisCacheUtils.set(flowId, emailChange.getUserId() + "", apolloCommonConfig.getChangeEmailLinkHour() * 3600);
            //发送邮件,只有老邮箱可用才会发送邮件
            if (emailChange.getAvailableType()==0) {
                String sign = Md5Tools.MD5(Md5Tools.MD5(emailChange.getId()+signWorld)+flowId);
                userCommonBusiness.sendUserEmailChangeLinkEmail("user_email_change_old_link", emailChange.getUserId(), emailChange.getOldEmail(), "/v1/public/account/user/email/old/link?requestId=" + flowId+"&sign="+sign, null);
            }
        }
    }

}
