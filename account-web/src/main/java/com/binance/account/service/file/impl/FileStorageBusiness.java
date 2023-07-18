package com.binance.account.service.file.impl;

import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.util.IOUtils;
import com.binance.account.service.file.IFileStorage;
import com.binance.platform.amazon.s3.service.S3ObjectService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 *  从Ftp迁移到S3，刚上线时采取Ftp和S3两边同时写，从Ftp读的策略。
 *  等历史数据都从Ftp迁移到S3后，停止写Ftp，从S3读
 *
 */
@Log4j2
@Service
public class FileStorageBusiness implements IFileStorage {

    @Resource(name = "S3ObjectWithSSEService")
    private S3ObjectService s3ObjectService;

    /**
     *
     *
     * @param content
     * @param objKey
     * @throws Exception
     */
    @Override
    public void save(byte[] content, String objKey) throws Exception {
        s3ObjectService.putObject(objKey, content, Mimetypes.getInstance().getMimetype(objKey));
    }

    @Override
    public byte[] load(String objKey) throws Exception {
        return IOUtils.toByteArray(s3ObjectService.getObject(objKey));
    }
}
