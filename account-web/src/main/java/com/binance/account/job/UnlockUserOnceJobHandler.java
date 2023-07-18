package com.binance.account.job;

import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.impl.UserBusiness;
import com.binance.master.constant.Constant;
import com.binance.master.utils.DateUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * 由于UnlockUserJobHandler依赖于缓存，所以无法处理上线前就被锁定的用户。
 * 本Job只用来在上线时处理所有的历史数据，不需要定期跑。
 *
 */
@Log4j2
@JobHandler(value = "unlockUserOnceJobHandler")
@Component
public class UnlockUserOnceJobHandler extends IJobHandler {

    public UnlockUserOnceJobHandler() {
        super();
    }

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBusiness userBusiness;

    @Resource
    private UserSecurityMapper userSecurityMapper;

    //
    private static final int BATCH_SIZE = 100;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("START-unlockUserOnceJobHandler");
        log.info("START-unlockUserOnceJobHandler");
        try {
            unlockUsers();
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("unlockUserOnceJobHandler error-->{0}", e);
            log.error("unlockUserOnceJobHandler error-->{}", e);
            return FAIL;
        } finally {
            XxlJobLogger.log("END-unlockUserOnceJobHandler");
            log.info("END-unlockUserOnceJobHandler");
        }
    }

    private void unlockUsers() {
        final Long ttlCnt = userMapper.queryUserByHavingStatusCount(Constant.USER_LOCK);
        List<User> users;
        int batchNo = 0;
        int processedCnt = 0;
        do {
            users = userMapper.queryUserByHavingStatusPage(new RowBounds(0, BATCH_SIZE), Constant.USER_LOCK);

            XxlJobLogger.log("batch no: {0}, users: {1}", batchNo, users.size());
            log.info("batch no: {}， users: {}", batchNo, users.size());
            batchNo++;
            for (User user : users) {
                processedCnt++;
                XxlJobLogger.log("START-unlock user:{0}, [{1}/{2}] ", user.getEmail(), processedCnt, ttlCnt);
                log.info("START-unlock user: {}, [{}/{}]", user.getEmail(), processedCnt, ttlCnt);

                String result = unlockUser(user);

                XxlJobLogger.log("END-unlock user:{0}, result: {1}\n", user.getEmail(), result);
                log.info("END-unlock user:{}, result: {}", user.getEmail(), result);
            }
        } while (users.size() == BATCH_SIZE);

    }

    private String unlockUser(User user) {
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if (userSecurity == null) {
            //should not be here
            XxlJobLogger.log("[ERROR] UserSecurity not found by userId({0})", user.getUserId());
            log.error("UserSecurity not found by userId({}).", user.getUserId());
            return "error";
        }

        if (userSecurity.getLockEndTime() == null) {
            XxlJobLogger.log("UserSecurity.lockEndTime is null, ignore");
            log.info("UserSecurity.lockEndTime is null, ignore");
            return "ignored";
        }

        if (userSecurity.getLockEndTime().getTime() > System.currentTimeMillis()) {
            XxlJobLogger.log("UserSecurity.lockEndTime:{0} > now, ignore",
                DateUtils.formatter(userSecurity.getLockEndTime(), "yyyy-MM-dd'T'HH:mm:ss"));
            log.info("UserSecurity.lockEndTime:{} > now, ignore",
                DateUtils.formatter(userSecurity.getLockEndTime(), "yyyy-MM-dd'T'HH:mm:ss"));
            return "ignored";
        }

        boolean success = userBusiness.unlockUser(user);


        return success ? "success" : "failed";
    }


}
