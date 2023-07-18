package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.data.entity.log.UserOperationLog;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.google.common.collect.ImmutableMap;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 *
 * 只能单线程跑
 *
 */
@Log4j2
@JobHandler(value = "UserOperationLogTransferHandler")
@Component
public class UserOperationLogTransferHandler extends IJobHandler {

    public UserOperationLogTransferHandler() {
        super();
    }

    private JdbcTemplate jdbcTemplate;

    @Value("${pnk.jdbc.url:null}")
    private String pnkJdbcUrl;

    @Value("${pnk.jdbc.username:null}")
    private String pnkJdbcUserName;

    @Value("${pnk.jdbc.password:null}")
    private String pnkJdbcPassword;

    @Resource
    private UserOperationLogMapper userOperationLogMapper;


    @Override
    public void init() {
        super.init();
        XxlJobLogger.log("create datasource using {0}, {1}...", pnkJdbcUrl, pnkJdbcUserName);
        log.info("create datasource using {}, {}...", pnkJdbcUrl, pnkJdbcUserName);
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(pnkJdbcUrl);
        config.setUsername(pnkJdbcUserName);
        config.setPassword(pnkJdbcPassword);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "10");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("maximumPoolSize", "1");
        HikariDataSource ds = new HikariDataSource(config);
        XxlJobLogger.log("create datasource complete...");
        log.info("create datasource complete...");

        jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public ReturnT<String> execute(String arg) throws Exception {
        if (jdbcTemplate == null) {
            init();
        }

        XxlJobLogger.log("START-UserOperationLogTransferHandler");
        log.info("START-UserOperationLogTransferHandler");
        try {
            transfer(arg);
            return SUCCESS;
        } catch (InterruptedException e) {
            //throw InterruptedException，响应调度中心“终止任务”
            XxlJobLogger.log("UserOperationLogTransferHandler 任务中断-->{0}");
            log.info("UserOperationLogTransferHandler 任务中断-->{}");
            throw e;
        } catch (Exception e) {
            XxlJobLogger.log("UserOperationLogTransferHandler error-->{0}", e);
            log.error("UserOperationLogTransferHandler error-->{}", e);
            return FAIL;
        } finally {
            XxlJobLogger.log("END-UserOperationLogTransferHandler");
            log.info("END-UserOperationLogTransferHandler");

        }
    }

    private static final String QUERY_SQL_TEMPLATE = "select * from %s where id > ? order by id limit ?";

    private static final String REDIS_KEY_TRANSFER_UOL_ERROR_BATCHES = "transfer_uol_error_batches";

    private static final String REDIS_KEY_TRANSFER_UOL_PROCESS = "transfer_uol_process";

    private static final int BATCH_SIZE = 1000;

    private void transfer(String arg) throws InterruptedException {
        for (String tableName : tableNames(arg)) {
            XxlJobLogger.log("START -- transfer table: {0}", tableName);
            log.info("START -- transfer table: {}", tableName);
            String sql = String.format(QUERY_SQL_TEMPLATE, tableName);
            Long id = loadProcess(tableName);
            int batchNo = 0;
            while (true) {
                //允许中断
                Thread.sleep(1);
                batchNo++;

                StopWatch stopWatch = new StopWatch();
                stopWatch.start("query");
                List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(sql, new Object[] {id, BATCH_SIZE});
                stopWatch.stop();
                if (CollectionUtils.isNotEmpty(rawRows)) {
                    List<UserOperationLog> newUserOperationLogs =
                            rawRows.stream().map(this::convert).filter(u -> u != null).collect(Collectors.toList());
                    //insert batch
                    try {
                        stopWatch.start("insert");
                        int result = userOperationLogMapper.batchInsert(newUserOperationLogs);
                        stopWatch.stop();
                    } catch (Throwable e) {
                        RedisCacheUtils.setLeftPush(REDIS_KEY_TRANSFER_UOL_ERROR_BATCHES,
                                JSON.toJSONString(ImmutableMap.of("id", id, "size", rawRows.size())));
                        XxlJobLogger.log("ERROR -- transfer table: {0}, id: {1}, size: {2}", tableName, id, rawRows.size());
                        log.error("ERROR -- transfer table: {}, id: {}, size: {}", tableName, id, rawRows.size());
                    }
                    XxlJobLogger.log("batch no:{0}, from id: {1}, batch size: {2}, " + stopWatch.toString(), batchNo, id, rawRows.size());
                    log.info("batch no:{}, from id: {}, , batch size: {}, " + stopWatch.toString(), batchNo, id, rawRows.size());
                    id = Long.valueOf(String.valueOf(rawRows.get(rawRows.size() - 1).get("id")));
                    //保存当前进度，可以中断后继续。
                    saveProcess(tableName, id);
                } else {
                    XxlJobLogger.log("END -- transfer table: {0}", tableName);
                    log.info("END -- transfer table: {}", tableName);
                    break;
                }
            }
        }
    }

    private Long loadProcess(String tableName) {
        String process = RedisCacheUtils.get(REDIS_KEY_TRANSFER_UOL_PROCESS);
        if (StringUtils.isBlank(process)) {
            return 0L;
        }
        JSONObject processJson = JSON.parseObject(process);
        Long id = processJson.getLong(tableName);
        return id == null ? 0L : id;
    }

    private void saveProcess(String tableName, Long id) {
        String process = RedisCacheUtils.get(REDIS_KEY_TRANSFER_UOL_PROCESS);
        JSONObject processJson = null;
        if (StringUtils.isBlank(process)) {
            processJson = new JSONObject();
        } else {
            processJson = JSON.parseObject(process);
        }
        processJson.put(tableName, id);
        RedisCacheUtils.set(REDIS_KEY_TRANSFER_UOL_PROCESS, JSON.toJSONString(processJson), 0);
    }

    private List<String> tableNames(String arg) {
        if (StringUtils.isBlank(arg)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(arg.split(","));
        }
    }

    private UserOperationLog convert(Map<String, Object> opLog) {
        try {
            UserOperationLog userOperationLog = new UserOperationLog();
            userOperationLog.setOperation((String) opLog.get("operation"));
            userOperationLog.setUuid(String.valueOf(opLog.get("id")));
            userOperationLog.setUserId(Long.valueOf((String) opLog.get("user_id")));
            userOperationLog.setFullIp((String) opLog.get("ip"));
            userOperationLog.setRealIp((String) opLog.get("real_ip"));
            userOperationLog.setClientType((String) opLog.get("client_type"));
            userOperationLog.setVersionCode((String) opLog.get("version_code"));
            userOperationLog.setApikey((String) opLog.get("apikey"));
            userOperationLog.setRequest((String) opLog.get("request"));
            userOperationLog.setResponseStatus((String) opLog.get("response"));
            userOperationLog.setUserAgent((String) opLog.get("browser"));
            userOperationLog.setRequestTime((Date) opLog.get("time"));
            return userOperationLog;
        } catch (RuntimeException e) {
            XxlJobLogger.log("Skip transferring operation log(id:{0}). Caused by: {1}", opLog.get("id"), e);
            log.info("Skip transferring operation log(id:{}). Caused by: {}", opLog.get("id"), e);
            return null;
        }
    }
}