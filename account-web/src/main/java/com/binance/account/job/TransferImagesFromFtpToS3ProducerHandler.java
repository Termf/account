package com.binance.account.job;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.utils.FtpUtils;
import com.binance.master.constant.CacheKeys;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;


/**
 *
 * 递归遍历FTP所有文件，把符合条件的文件名加入redis队列
 *
 */
@Log4j2
@JobHandler(value = "transferImagesFromFtpToS3ProducerHandler")
@Component
public class TransferImagesFromFtpToS3ProducerHandler extends IJobHandler {


    @Resource
    private FtpUtils ftpUtils;


    public TransferImagesFromFtpToS3ProducerHandler() {
        super();
    }



    @Override
    public ReturnT<String> execute(String arg) throws Exception {
        XxlJobLogger.log("TransferImagesFromFtpToS3ProducerHandler Start");
        log.info("START-TransferImagesFromFtpToS3ProducerHandler");
        try {
            produce(arg);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("TransferImagesFromFtpToS3ProducerHandler error-->{0}", e);
            log.error("TransferImagesFromFtpToS3ProducerHandler error-->{}", e);
            return FAIL;
        } finally {
            XxlJobLogger.log("END-TransferImagesFromFtpToS3ProducerHandler");
            log.info("END-TransferImagesFromFtpToS3ProducerHandler");
        }

    }


    private void produce(String arg) {
        if (StringUtils.isBlank(arg)) {
            arg = "{}";
        }
        JSONObject argJson = JSON.parseObject(arg);

        JSONArray files = argJson.getJSONArray("paths");
        Long lastTimestamp = argJson.getLong("lastTimestamp");
        String root = argJson.getString("root");
        if (lastTimestamp == null) {
            lastTimestamp = RedisCacheUtils.get(CacheKeys.FTP_TO_S3_TRANSFER_LAST_TIMESTAMP, Long.class);
        }

        final long finalLastTimestamp = ObjectUtils.defaultIfNull(lastTimestamp, 0L);
        final long now = System.currentTimeMillis();
        XxlJobLogger.log("handle images whose timestamp: {0} ~ {1}", finalLastTimestamp, now);
        log.info("handle images whose timestamp: {} ~ {}", lastTimestamp, now);

        String clearQueue = argJson.getString("clearQueue");
        if (StringUtils.isNotBlank(clearQueue) && BooleanUtils.toBoolean(clearQueue)) {
            RedisCacheUtils.del(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE);
        }

        //加入消费队列 Function
        BiConsumer<String, FTPFile> fileConsumer = (p, f) -> {
            if (f.getTimestamp().getTimeInMillis() > finalLastTimestamp && f.getTimestamp().getTimeInMillis() < now) {
                Long result = RedisCacheUtils.setLeftPush(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE, p + f.getName());
                XxlJobLogger.log("enqueue result: {0}, ", result);
                log.info("enqueue result: {}, ", result);
            }
        };

        //可以通过Handler参数设置finalLastTimestamp > now 来跳过这一步
        if (finalLastTimestamp < now) {
            //遍历ftp里的文件，把符合条件的文件名加入队列
            ftpUtils.consumeAllRecursively(StringUtils.isBlank(root) ? "/" : root, fileConsumer);
        }

        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                String file = files.getString(i);
                XxlJobLogger.log("add {0} to transfer queue, ", file);
                log.info("add {} to transfer queue, ", file);
                RedisCacheUtils.setLeftPush(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE, file);
            }
        }

        produceFromFile(argJson);

        //把失败队列里的重新加入消费队列
        Object failedFile = null;
        int failedCnt = 0;
        while ((failedFile = RedisCacheUtils.getRightPop(CacheKeys.FTP_TO_S3_TRANSFER_FAILED_QUEUE)) != null) {
            RedisCacheUtils.setLeftPush(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE, failedFile.toString());
            failedCnt++;
        }
        XxlJobLogger.log("move {0} failed to transfer queue, ", failedCnt);
        log.info("move {} failed to transfer queue, ", failedCnt);

        //更新处理时间，下次从此时间开始
        RedisCacheUtils.set(CacheKeys.FTP_TO_S3_TRANSFER_LAST_TIMESTAMP, now, 0 /*0 -> won't expire*/);
    }

    //打开resource里的路径列表，批量把路径加到队列中。
    private void produceFromFile(JSONObject argJson) {
        String srcFileName = argJson.getString("srcFileName");
        if (StringUtils.isBlank(srcFileName)) {
            log.info("empty srcFileName, exit..");
            return;
        }
        ClassPathResource resource = new ClassPathResource(srcFileName);

        InputStream input = null;
        BufferedReader reader = null;
        int count = 0;
        try {
            log.info("loading:{}", resource.getFilename());
            XxlJobLogger.log("loading:{0}", resource.getFilename());
            input = resource.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            while (reader.ready()) {
                String path = reader.readLine();
                if (StringUtils.isBlank(path) || StringUtils.isBlank(path.trim())) {
                    continue;
                }
                path = path.trim();
                if (path.indexOf("/") == -1) {
                    continue;
                }
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                RedisCacheUtils.setLeftPush(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE, path);

                //防止log太多打不开日志
                if (count++ % 100 == 0) {
                    XxlJobLogger.log("enqueue path:{0}, {1}", path, count);
                    log.info("enqueue path:{}, {}", path, count);
                }
            }
            if (reader != null) {
                reader.close();
            }
            if (input != null) {
                input.close();
            }

        } catch (IOException e) {
            log.error("read file error.", e);
            XxlJobLogger.log("read file error. {0}", e);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    log.error("close reader error.", ioe);
                    XxlJobLogger.log("close reader error.{0}", ioe);
                }

            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioe) {
                    log.error("close input error.", ioe);
                    XxlJobLogger.log("close input error.{0}", ioe);
                }
            }
        } finally {

        }
    }



}
