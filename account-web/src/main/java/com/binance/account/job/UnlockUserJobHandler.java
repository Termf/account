package com.binance.account.job;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.impl.UserBusiness;
import com.binance.master.constant.CacheKeys;
import com.binance.master.utils.RedisCacheUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 *
 * 定期解锁符合条件的用户(UserSecurity.lock_end_time >= System.currentTimeMillis())
 *
 * 用户被锁定的时候，会在redis的zset(CacheKeys.LOCKED_USER_EMAIL_ZSET)里记录
 * 一条user email(zset value)和lock_end_time(zset score)。
 *
 * 定期查询这个zset，找出lock_end_time >= System.currentTimeMillis()的user email，执行解锁
 *
 */
@Log4j2
@JobHandler(value = "unlockUserJobHandler")
@Component
public class UnlockUserJobHandler extends IJobHandler {

    public UnlockUserJobHandler() {
        super();
    }

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBusiness userBusiness;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("START-unlockUserJobHandler");
        log.info("START-unlockUserJobHandler");
        try {
            unlockUsers();
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("unlockUserJobHandler error-->{0}", e);
            log.error("unlockUserJobHandler error-->{}", e);
            return FAIL;
        } finally {
            XxlJobLogger.log("END-unlockUserJobHandler");
            log.info("END-unlockUserJobHandler");

        }
    }

    private void unlockUsers() {
        //查询 lock_end_time 属于 [0, System.currentTimeMillis()] 的 user ids,
        //如果量大的话可以改用RedisCacheUtils.zrangeByScore分页
        Set<Object> lockedUserEmails =
                RedisCacheUtils.zrangeByScore(CacheKeys.LOCKED_USER_EMAIL_ZSET, 0, System.currentTimeMillis());

        XxlJobLogger.log("got {0} locked user emails from cache: {1}", lockedUserEmails.size(), lockedUserEmails);
        log.info("got {} locked user emails from cache: {}", lockedUserEmails.size(), lockedUserEmails);

        if (CollectionUtils.isEmpty(lockedUserEmails)) {
            return;
        }

        boolean allSuccess = true;
        for (Object lockedUserEmail : lockedUserEmails) {
            User user = userMapper.queryByEmail(lockedUserEmail.toString());
            if (user == null) {
                XxlJobLogger.log("Couldn't find user: {0} from db", lockedUserEmail);
                log.info("Couldn't find user: {} from db", lockedUserEmail);
                continue;
            }
            XxlJobLogger.log("START-unlock user:{0}", user.getEmail());
            log.info("START-unlock user: {}", user.getEmail());
            boolean singleSuccess;
            try {
                singleSuccess = userBusiness.unlockUser(user);
            } catch (Exception e) {
                //抓住异常，不影响其他用户解锁
                XxlJobLogger.log("unlock user error");
                XxlJobLogger.log(e);
                log.info("unlock user error", e);
                singleSuccess = false;
            }

            if (singleSuccess) {
                Long rmCnt = RedisCacheUtils.zrem(CacheKeys.LOCKED_USER_EMAIL_ZSET, user.getEmail());
                log.info("remove cache: {}", rmCnt);
                XxlJobLogger.log("remove cache: {0}", rmCnt);
            }
            allSuccess &= singleSuccess;
            XxlJobLogger.log("END-unlock user:{0}, result: {1}\n", user.getEmail(), singleSuccess ? "success" : "failed");
            log.info("END-unlock user:{}, result: {}", user.getEmail(), singleSuccess ? "success" : "failed");
        }

        if (!allSuccess) {
            throw new RuntimeException("has error");
        }
    }


}
