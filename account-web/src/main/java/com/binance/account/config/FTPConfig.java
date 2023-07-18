package com.binance.account.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

/**
 * Description: Spring integrate ftp configuration
 *
 * @author hongchaoMao - Date 2018/7/6
 */
@Configuration
public class FTPConfig implements EnvironmentAware {

    private static final Logger logger = LogManager.getLogger(FTPConfig.class);

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SessionFactory<FTPFile> ftpSessionFactory() {
        String hostName = environment.getProperty("account.ftp.hostname");
        String port = environment.getProperty("account.ftp.port");
        String userName = environment.getProperty("account.ftp.username");
        String pasword = environment.getProperty("account.ftp.password");
        if (StringUtils.isBlank(hostName) || StringUtils.isBlank(port)){
            throw new RuntimeException("FTP SessionFactory 未配置host或者port,初始化失败!");
        }
        DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
        sf.setHost(hostName);
        sf.setPort(Integer.parseInt(port));
        sf.setUsername(userName);
        sf.setPassword(pasword);
        sf.setDataTimeout(30000); // 数据传输超时时间30秒
        sf.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
        return sf;
    }

    @Bean
    public CachingSessionFactory<FTPFile> cachingSessionFactory(){
        // session缓存池最大数为20,避免每次从factory重新创建会话
        CachingSessionFactory<FTPFile> cachingSessionFactory = new CachingSessionFactory<>(ftpSessionFactory(), 20);
        // 设置session缓存池中的session超时时间 30 seconds
        cachingSessionFactory.setSessionWaitTimeout(30000);
        return cachingSessionFactory;
    }

    @Bean
    public FtpRemoteFileTemplate template() {
        FtpRemoteFileTemplate remoteFileTemplate =  new FtpRemoteFileTemplate(cachingSessionFactory());
        // 如果远程目录不存在则会自动创建
        remoteFileTemplate.setAutoCreateDirectory(true);
        remoteFileTemplate.setCharset("UTF-8");
        // 取Message中的header的file-name属性值作为文件名
        remoteFileTemplate.setFileNameGenerator(message -> message.getHeaders().get("file-name").toString());
        // 必须设置远程目录,这里简单的设置成根目录
        remoteFileTemplate.setRemoteDirectoryExpression(new ValueExpression<>("/"));
        logger.info("FtpRemoteFileTemplate 对象成功初始化 !");
        return remoteFileTemplate;
    }


}
