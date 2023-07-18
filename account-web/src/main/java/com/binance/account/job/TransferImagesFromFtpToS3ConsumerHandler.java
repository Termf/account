package com.binance.account.job;


import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.PutObjectResult;
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
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

/**
 *
 * 消费TransferImagesFromFtpToS3ProducerHandler生产的数据—— 把文件从FTP搬到S3
 * 可以多个节点同时消费
 *
 */
@Log4j2
@JobHandler(value = "transferImagesFromFtpToS3ConsumerHandler")
@Component
public class TransferImagesFromFtpToS3ConsumerHandler extends IJobHandler {

    @Resource
    private FtpUtils ftpUtils;

    @Resource(name = "S3ObjectWithSSEService")
    private S3ObjectService s3ObjectService;


    public TransferImagesFromFtpToS3ConsumerHandler() {
        super();
    }



    @Override
    public ReturnT<String> execute(String arg) throws Exception {
        XxlJobLogger.log("START-TransferImagesFromFtpToS3ConsumerHandler");
        log.info("START-TransferImagesFromFtpToS3ConsumerHandler");
        try {
            transfer(arg);
            return SUCCESS;
        } catch (InterruptedException e) {
            //throw InterruptedException，响应调度中心“终止任务”
            XxlJobLogger.log("TransferImagesFromFtpToS3ConsumerHandler 任务中断-->{0}");
            log.info("TransferImagesFromFtpToS3ConsumerHandler 任务中断-->{}");
            throw e;
        } catch (Exception e) {
            XxlJobLogger.log("TransferImagesFromFtpToS3ConsumerHandler error-->{0}", e);
            log.error("TransferImagesFromFtpToS3ConsumerHandler error-->{}", e);
            return FAIL;
        } finally {
            XxlJobLogger.log("END-TransferImagesFromFtpToS3ConsumerHandler");
            log.info("END-TransferImagesFromFtpToS3ConsumerHandler");

        }
    }


    private void transfer(String arg) throws InterruptedException {
        if (StringUtils.isBlank(arg)) {
            arg = Boolean.TRUE.toString();
        }
        boolean overwrite = BooleanUtils.toBoolean(arg);
        while (true) {
            //throw InterruptedException，响应调度中心“终止任务”
            Thread.sleep(1);

            String fileFullName = (String) RedisCacheUtils.getRightPop(CacheKeys.FTP_TO_S3_TRANSFER_QUEUE);
            XxlJobLogger.log("pop file name from queue: {0}", fileFullName);
            log.info("pop file name from queue: {}", fileFullName);

            if (StringUtils.isBlank(fileFullName)) {
                XxlJobLogger.log("nothing popped from queue, exit...");
                log.info("nothing popped from queue, exit...");
                break;
            }
            try {
                //如果overwrite == false， 先检查是否已存在
                if (!overwrite && s3ObjectService.existsObject(fileFullName)) {
                    XxlJobLogger.log("file exists: {0}", fileFullName);
                    log.info("file exists: {}", fileFullName);
                    continue;
                }

                byte[] content = ftpUtils.downloadFromFtp(fileFullName);
                XxlJobLogger.log("downloading from ftp...done. size: {0} bytes", content.length);
                log.info("downloading from ftp...done. size: {} bytes", content.length);
                PutObjectResult result = null;

                result =
                    s3ObjectService.putObject(fileFullName, content, Mimetypes.getInstance().getMimetype(fileFullName));

                XxlJobLogger.log("put to s3 result: {0}", result.getETag());
                log.info("put to s3 result: {}", result.getETag());
            } catch (FileNotFoundException e) {
                XxlJobLogger.log("file not found: {0}", e);
                log.error("file not found: {}", e);
            } catch (Exception e) {
                XxlJobLogger.log("transfer failed: {0}", fileFullName);
                log.error("transfer failed: {}", fileFullName);
                try {
                    RedisCacheUtils.setLeftPush(CacheKeys.FTP_TO_S3_TRANSFER_FAILED_QUEUE, fileFullName);
                } catch (Exception e1) {
                    XxlJobLogger.log("push back to queue failed: {0}", fileFullName);
                    log.error("push back to queue failed: {}", fileFullName);
                }
            } finally {
                XxlJobLogger.log("");
                log.info("");
            }
        }
    }

}
