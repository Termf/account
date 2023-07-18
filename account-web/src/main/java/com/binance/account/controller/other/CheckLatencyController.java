package com.binance.account.controller.other;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserMapper;

import lombok.extern.log4j.Log4j2;

/**
 * Created by Fei.Huang on 2018/7/6.
 */
@RestController
@Log4j2
public class CheckLatencyController {

    @Autowired
    private UserMapper userMapper;

    private static final int maxLoop = 3;
    private static final String testEmail = "79527759@qq.com";

    @GetMapping("/check/latency")
    public Map<String, String> checkLatency() {

        Map<String, String> resultMap = new HashMap<>();
        final User originalUser = userMapper.queryByEmail(testEmail);

        if (null != originalUser) {

            int goodCount = 0;
            int badCount = 0;
            double latencyCount = 0;
            double maxLatency = 0;

            StopWatch stopWatchMain = new StopWatch();
            stopWatchMain.start();

            for (int i = 0; i < maxLoop; i++) {

                // Write
                User user = new User();
                user.setStatus((long) i);
                user.setEmail(testEmail);
                userMapper.updateUserStatusByEmail(user);

                // Read
                StopWatch stopWatchRead = new StopWatch();
                stopWatchRead.start();

                User userRead;
                int tryCount = 0;
                do {
                    ++tryCount;
                    userRead = userMapper.queryByEmail(testEmail);
                } while (i != userRead.getStatus());

                stopWatchRead.stop();

                if (tryCount == 1) {
                    ++goodCount;
                    log.info("CheckLatency all goodCount, maxLoop:{}", i);
                } else {
                    ++badCount;
                    if (maxLatency < stopWatchRead.getTotalTimeSeconds()) {
                        maxLatency = stopWatchRead.getTotalTimeSeconds();
                    }
                    latencyCount += stopWatchRead.getTotalTimeSeconds();
                    log.info("CheckLatency latency happened, maxLoop:{}, tryCount={}, latency:{}sec", i, tryCount,
                            stopWatchRead.getTotalTimeSeconds());
                }
            }

            stopWatchMain.stop();
            String result = String.format(
                    "CheckLatency maxLoop:%s, goodCount:%s, badCount:%s, maxLatency:%ssec, averageLatency:%ssec, totalConsumeTime:%ssec",
                    maxLoop, goodCount, badCount, maxLatency, 0 != badCount ? latencyCount / badCount : 0,
                    stopWatchMain.getTotalTimeSeconds());
            log.info(result);

            // Revert
            userMapper.updateUserStatusByEmail(originalUser);

            resultMap.put("result", result);
        }
        return resultMap;
    }

}
