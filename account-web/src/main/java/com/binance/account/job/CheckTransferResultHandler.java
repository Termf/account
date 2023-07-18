package com.binance.account.job;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.utils.FtpUtils;
import com.binance.master.constant.CacheKeys;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.platform.amazon.s3.service.S3ObjectService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;


/**
 *
 * 递归遍历FTP所有文件，把符合条件的文件名加入redis队列
 *
 */
@Log4j2
@JobHandler(value = "CheckTransferResultHandler")
@Component
public class CheckTransferResultHandler extends IJobHandler {


    @Resource
    private FtpUtils ftpUtils;

    @Resource(name = "S3ObjectWithSSEService")
    private S3ObjectService s3ObjectService;

    public CheckTransferResultHandler() {
        super();
    }



    @Override
    public ReturnT<String> execute(String arg) throws Exception {
        XxlJobLogger.log("START-CheckTransferResultHandler");
        log.info("START-CheckTransferResultHandler");
        try {
            check(arg);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("CheckTransferResultHandler error-->{0}", e);
            log.error("CheckTransferResultHandler error-->{}", e);
            return FAIL;
        } finally {
            XxlJobLogger.log("END-CheckTransferResultHandler");
            log.info("END-CheckTransferResultHandler");
        }

    }


    private void check(String arg) {
        if (StringUtils.isBlank(arg)) {
            arg = "{}";
        }
        JSONObject argJson = JSON.parseObject(arg);

        final String pathRegex = argJson.getString("pathRegex");
        XxlJobLogger.log("Use pathRegex {0}", pathRegex);
        log.info("Use pathRegex {}", pathRegex);

        final JSONArray roots = argJson.getJSONArray("roots");
        if (roots == null || roots.size() == 0) {
            XxlJobLogger.log("roots is null or empty");
            log.info("roots is null or empty");
        }

        //加入消费队列 Function
        BiConsumer<String, FTPFile> fileConsumer = (path, file) -> {
            if (Pattern.matches(pathRegex, path)) {
                boolean existing = false;
                try {
                    existing = s3ObjectService.existsObject(path + file.getName());
                } catch (Exception e) {
                    XxlJobLogger.log("existsObject() error: {0}, ", e);
                }

                if (!existing) {
                    Long result = RedisCacheUtils.setLeftPush(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE, path + file.getName());
                    XxlJobLogger.log("enqueue: {0}, {1}", path + file.getName(), result);
                    log.info("enqueue: {}, {}", path + file.getName(), result);
                } else {
                    XxlJobLogger.log("{0} exists", path + file.getName());
                    log.info("{} exists", path + file.getName());
                }
            } else {
                XxlJobLogger.log("path: {0} doesn't match pattern.", path);
                log.info("path: {} doesn't match pattern.", path);
            }
        };

        //遍历ftp里的文件，把符合条件的文件名加入队列
        for (int i = 0; i < roots.size(); i++) {
            String root = roots.getString(i);
            XxlJobLogger.log("check in {0}", root);
            log.info("check in {}", root);
            if (StringUtils.isNotBlank(root)) {
                ftpUtils.consumeAllRecursively(root, fileConsumer);
            }
        }

        //把失败队列里的重新加入消费队列
        Object failedFile = null;
        int failedCnt = 0;
        while ((failedFile = RedisCacheUtils.getRightPop(CacheKeys.FTP_TO_S3_TRANSFER_FAILED_QUEUE)) != null) {
            RedisCacheUtils.setLeftPush(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE, failedFile.toString());
            failedCnt++;
        }
        XxlJobLogger.log("move {0} failed to transfer queue, ", failedCnt);
        log.info("move {} failed to transfer queue, ", failedCnt);
    }
}
