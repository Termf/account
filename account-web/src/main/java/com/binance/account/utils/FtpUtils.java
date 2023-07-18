package com.binance.account.utils;

import com.alibaba.fastjson.JSON;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.InputStreamCallback;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Log4j2
@Service
public class FtpUtils {

    private static final Logger logger = LogManager.getLogger(FtpUtils.class);

    private static String FTP_HOSTNAME;

    private static String FTP_USERNAME;

    private static String FTP_PASSWORD;

    private static Integer FTP_PORT;

    private FTPClient ftpClient;

    @Autowired
    protected ISysConfig iSysConfig;

    @Autowired
    private FtpRemoteFileTemplate ftpTemplate;

    private void initConfig() {
        if (StringUtils.isBlank(FTP_HOSTNAME)) {
            SysConfig host = this.iSysConfig.selectByDisplayName("exchange_ftp_hostname");
            FTP_HOSTNAME = host == null ? null : host.getCode();
        }

        if (StringUtils.isBlank(FTP_USERNAME)) {
            SysConfig username = this.iSysConfig.selectByDisplayName("exchange_ftp_username");
            FTP_USERNAME = username == null ? null : username.getCode();
        }

        if (StringUtils.isBlank(FTP_PASSWORD)) {
            SysConfig password = this.iSysConfig.selectByDisplayName("exchange_ftp_password");
            FTP_PASSWORD = password == null ? null : password.getCode();
        }

        if (FTP_PORT == null) {
            SysConfig port = this.iSysConfig.selectByDisplayName("exchange_ftp_port");
            FTP_PORT = port == null ? null : Integer.parseInt(port.getCode());
        }
    }

    /**
     * Discription: Send file to ftp.
     *
     * @param filePath    the file path
     * @param bytes the input byte array
     * @return the boolean
     * @author hongchaoMao - Date 2018-7-6 17:50:03
     */
    public boolean sendToFtp(String filePath, byte[] bytes){
        if (StringUtils.isBlank(filePath)){
            log.error("------>sendToFtp() error: 上传的指定文件路径为空!");
            return false;
        }
        log.info("Sending file to {}, size: {}", filePath, bytes.length);
        // last index of / or \
        int index = FilenameUtils.indexOfLastSeparator(filePath);

        String fileName = filePath;
        String subDirectory = null;
        if (index != -1){
            fileName = filePath.substring(index + 1,filePath.length());
            subDirectory = filePath.substring(0,index);
        }
        try {
            Map<String,Object> header = new HashMap<>(2);
            // 指定上传到远程服务器上的文件名字
            header.put("file-name", fileName);
            GenericMessage<Object> message = new GenericMessage<>(bytes, header);
            String resultPath = ftpTemplate.send(message, subDirectory, FileExistsMode.REPLACE);
            return StringUtils.isNotBlank(resultPath);
        }catch (Exception e){
            logger.error("------>sendToFtp() error: {}", e);
        }
        return false;
    }

    /**
     * Discription: Download file from ftp.
     *
     * @param fileAddress the file address
     * @return the byte [ ]
     * @author hongchaoMao - Date 2018-7-11 14:40:47
     */
    public byte[] downloadFromFtp(String fileAddress) throws FileNotFoundException {
        if (StringUtils.isBlank(fileAddress)){
            log.error("------>downloadFromFtp() error: 下载的指定文件路径为空!");
            return null;
        }

        if (!ftpTemplate.exists(fileAddress)) {
            throw new FileNotFoundException(fileAddress);
        }
        final ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
        InputStreamCallback inputStreamCallback = stream -> {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = stream.read(bytes)) > 0){
                byteArrayInputStream.write(bytes,0, len);
            }
        };
        try{
            if (ftpTemplate.get(fileAddress, inputStreamCallback)){
                return byteArrayInputStream.toByteArray();
            }
        } catch (Exception e){
            logger.error("------>downloadFromFtp() 文件下载异常: {}", e);
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                logger.error("------>downloadFromFtp() 下载流关闭异常: {}", e);
            }
        }
        return null;
     }

     private static final Set<String> IGNORED = Collections.unmodifiableSet(Sets.newHashSet(".", ".."));

     public void consumeAllRecursively(String path, BiConsumer<String, FTPFile> consumer) {
         if (!path.endsWith("/")) {
             path = path + "/";
         }

         //注意，当path下的文件过多时，list方法可能返回空数组。
         FTPFile[] files = ftpTemplate.list(path);
         logger.info("List files in path: {}, got {} files", path, files.length);
         for (FTPFile file : files) {
            logger.info("Got ftp file: {}", path + file.getName());
             if (IGNORED.contains(file.getName())) {
                 continue;
             } else if (file.isDirectory()) {
                 consumeAllRecursively(path + file.getName() + "/", consumer);
             } else if (file.isFile()) {
                 consumer.accept(path, file);
             } else {
                log.warn("Not a valid file: {}", JSON.toJSONString(file));
             }
         }

     }

    /**
     * @deprecated 被ftpTemplate替代
     * @return FTPClient
     */
    @Deprecated
    public FTPClient getFtpClient() {
        initConfig();
        try {
            ftpClient = new FTPClient();

            ftpClient.enterLocalPassiveMode();
            ftpClient.connect(FTP_HOSTNAME, FTP_PORT);//连接FTP服务器
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.login(FTP_USERNAME, FTP_PASSWORD);//登录
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                log.info("未连接到FTP，用户名或密码错误。");
                ftpClient.disconnect();
            } else {
                log.info("FTP连接成功。");
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.sendNoOp();
        } catch (SocketException e) {
            log.info("FTP的IP地址可能错误，请正确配置。");
        } catch (IOException e) {
            log.info("FTP的端口错误,请正确配置。");
        } catch (Exception e) {
            log.info("unknown exception", e);
        }
        return ftpClient;
    }

}
