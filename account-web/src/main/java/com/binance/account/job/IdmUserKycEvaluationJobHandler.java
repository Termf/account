package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.UserIp;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.user.IUser;
import com.binance.account.utils.ExcelUtil;
import com.binance.account.utils.MapUtil;
import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.UserKycApproveVo;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.user.response.GetUserResponse;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.vo.idm.Level1UserData;
import com.binance.inspector.vo.idm.Level2UserData;
import com.binance.inspector.vo.idm.request.Level1UserRequest;
import com.binance.inspector.vo.idm.request.Level2UserRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.RedisCacheUtils;
import com.github.promeg.pinyinhelper.Pinyin;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于把合规团队需要的交易平台数据通过 API 传输至 IDM 平台，具体需求请查看文档.
 *
 * @see <a href="https://confluence.fdgahl.cn/pages/viewpage.action?pageId=12588272">Confluence</a>
 * @author sunzhenlei
 * @date 2019-02-22
 */
@Log4j2
@JobHandler(value = "IdmUserKycEvaluationJobHandler")
@Component
public class IdmUserKycEvaluationJobHandler extends IJobHandler {
    @Resource
    private IUser iUser;
    @Resource
    private IUserKyc userKyc;
    @Resource
    IdmApi idmApi;
    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;
    @Resource
    private UserIpMapper userIpMapper;

    private static final Integer MAX_USER_ID = 99999999;
    private static final Integer MIN_USER_ID = 10000000;
    private static final String USER_ID_PROCESSED = "USER_ID_PROCESSED";

    /**
     * 每次处理的最大用户数
     */
    private static final Integer USER_NUMBER_PER_TIME = 10000;

    @Override
    /**
     * 根据输入将一定的用户KYC数据发送至idm平台进行合规校验
     *
     * @param param
     *  1. 可以为空，表示同时更新所有用户及交易数据
     *  2. 格式 useId, true/false 表示用户及是否输出csv
     *      a. 用户格式可以为
     *         a1. * 表示任意用户
     *         a2. userId 表示指定用户
     *         a3. userId1-userId2 表示指定用户范围，若userId2为null，即 'userId-' 表示不限制最大用户id
     *         a4. redis:1000[:35000010] 表示从redis读取用户起始值(也可以强制设置起始值，比如35000010)，每次读取尝试1000个，即 userId范围为：redis - redis+1000
     */
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("START-IdmUserKycEvaluationJobHandler");
        log.info("START-IdmUserKycEvaluationJobHandler");
        final String usage = "Input params type: userId[, exportAsCsv], like * or 35000052 or 35000052-35000058,true or redis:1000 or redis:1000:35000010,true";

        // 1. 根据入参，从pnk user数据表获取所需校验的用户基本数据
        Map<String, Integer> userSelectorMap = new HashMap<>(16);
        if (StringUtils.isNotBlank(param)) {
            try {
                parseUserKycParam(param, userSelectorMap);
                log.info("Params after parse:{}", userSelectorMap);
            } catch (Exception e) {
                XxlJobLogger.log(usage);
                return FAIL;
            }
        }
        // 暂时不能为空
        else {
            XxlJobLogger.log("Input params is empty! " + usage);
            return FAIL;
        }

        // 根据入参查询用户ids，然后根据ids，查询需要的所有信息
        Integer userId =  userSelectorMap.getOrDefault("userId", MIN_USER_ID);
        Integer endUserId =  userSelectorMap.getOrDefault("endUserId", MAX_USER_ID);
        Integer exportCsv =  userSelectorMap.getOrDefault("export", 0);
        XxlJobLogger.log("Processing user range:{0}-{1}", userId, endUserId);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            evaluateUserKycData(userId, endUserId, exportCsv==1?true:false);
            // 成功后将最终处理的用户Id写入redis
            RedisCacheUtils.set(USER_ID_PROCESSED, endUserId+1, 0);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("IdmUserKycEvaluationJobHandler error-->{0}", e);
            log.error("IdmUserKycEvaluationJobHandler error-->{}", e);
            return FAIL;
        } finally {
            stopWatch.stop();
            XxlJobLogger.log("END-IdmUserKycEvaluationJobHandler use {0}", stopWatch.getTotalTimeSeconds());
            log.info("END-IdmUserKycEvaluationJobHandler use {}s", stopWatch.getTotalTimeSeconds());
        }
    }

    public void evaluateUserKycData(final int userId, final int endUserId, boolean exportCsv) throws Exception {
        int totalTrans = (endUserId-userId)/USER_NUMBER_PER_TIME+1;
        // 分批次拉取用户数据
        for (int trans = 0; trans<totalTrans; trans++) {
            int start = userId + trans*USER_NUMBER_PER_TIME;
            int end = (start + USER_NUMBER_PER_TIME);
            end = (end>endUserId)?endUserId:end;
            List<IdmUserInfo> idmUserInfos = new ArrayList<>(USER_NUMBER_PER_TIME);
            for (int id=start; id<=end; id++) {
                UserIdRequest userIdRequest = new UserIdRequest();
                userIdRequest.setUserId(Long.valueOf(id));
                try {
                    APIResponse<GetUserResponse> userResponse = iUser.getUserById(APIRequest.instance(userIdRequest));
                    if (userResponse == null || userResponse.getData() == null) {
                        log.error("Cannot find user info for user:{}", id);
                        continue;
                    }

                    List<UserSecurityLog> userSecurityLogs = userSecurityLogMapper.getUserSecurityListByUserIdAndOperateType(
                            Long.valueOf(id),
                            "regist",
                            0,
                            10);
                    // 尝试查找用户注册ip地址，找不到尝试查找用户曾经使用的ip
                    UserSecurityLog registerLog = null;
                    String usedIpAddress = null;
                    if (CollectionUtils.isEmpty(userSecurityLogs)) {
                        List<UserIp> userIp = userIpMapper.getIpByUser(Long.valueOf(userId));
                        usedIpAddress = CollectionUtils.isEmpty(userIp) ? null:userIp.get(0).getIp();
                        log.error("Cannot find register info for user:{}", id);
                        continue;
                    }
                    else if(userSecurityLogs.size()>1) {
                        log.warn("Found multiple register log for user:{}", id);
                        registerLog = userSecurityLogs.get(0);
                    }
                    else {
                        registerLog = userSecurityLogs.get(0);
                    }

                    idmUserInfos.add(new IdmUserInfo(userResponse.getData(), registerLog, usedIpAddress));
                } catch (Exception e) {
                    // 假如是找不到用户信息，继续查询
                    if (e instanceof BusinessException && ((BusinessException) e).getErrorCode()== GeneralCode.USER_NOT_EXIST) {
                        log.info("userId {} not exist!", id);
                        continue;
                    }
                    log.error("Error occurred while fetching user {}, please check!", id);
                    throw e;
                }
            }

            // 发送校验信息
            evaluateUserKycData(idmUserInfos, start, end, exportCsv);
            XxlJobLogger.log("Evaluated data, user {0}-{1}, valid {2}", start, end, idmUserInfos.size());
            log.info("Evaluated data, user {}-{}, valid {}", start, end, idmUserInfos.size());
        }
    }

    private void evaluateUserKycData(List<IdmUserInfo> idmUserInfos, int start, int end, boolean exportCsv) throws Exception {
        // 分批次发送请求，每次请求最多1000条用户数据
        List<Level1UserData>[] level1UserList = new List[idmUserInfos.size() / 1000 + 1];
        List<Level2UserData>[] level2UserList = new List[idmUserInfos.size() / 1000 + 1];
        int level1UserCounter = 0;
        int level2UserCounter = 0;

        for (IdmUserInfo idmUserInfo:idmUserInfos) {
            GetUserResponse userResponse = idmUserInfo.getUserResponse();
            Long userId = Long.valueOf(userResponse.getUser().getUserId());
            try {
                UserSecurityVo userSecurityVo = userResponse.getUserSecurity();
                Integer level = userSecurityVo==null? 1:userSecurityVo.getSecurityLevel();
                UserVo userVo = userResponse.getUser();
                UserSecurityLog userSecurityLog = idmUserInfo.getUserSecurityLog();
                String ipAddress = null;

                // 按用户等级划分，不同等级需要提交不同的数据
                if (1 == level) {
                    Level1UserData level1UserData = new Level1UserData();
                    if (userSecurityLog!=null
                            && userSecurityLog.getOperateTime() != null) {
                        Long unixTime = userSecurityLog.getOperateTime().getTime() / 1000;
                        level1UserData.setTti(unixTime.toString());
                    }
                    else {
                        XxlJobLogger.log("Cannot get register time for userId: " + userId);
                        log.warn("Cannot get register time for userId: " + userId);
                    }
                    // 无注册IP地址，用曾经使用过的ip地址
                    if (userSecurityLog == null || StringUtils.isBlank(userSecurityLog.getIp())){
                        ipAddress = idmUserInfo.getUsedIpAddress();
                        if (ipAddress == null) {
                            XxlJobLogger.log("Cannot get register/used ip for userId: " + userId);
                            log.warn("Cannot get register/used ip for userId: " + userId);
                            continue;
                        }
                    }
                    else {
                        ipAddress = userSecurityLog.getIp();
                    }

                    level1UserData.setIp(ipAddress);
                    level1UserData.setTea(userVo.getEmail());
                    level1UserData.setMan(String.valueOf(userId));

                    int index = level1UserCounter / 1000;
                    List<Level1UserData> list = level1UserList[index];
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(level1UserData);
                    level1UserList[index] = list;
                    level1UserCounter ++;
                }
                else {
                    Level2UserData level2UserData = new Level2UserData();
                    if (userSecurityLog!=null
                            && userSecurityLog.getOperateTime() != null) {
                        Long unixTime = userSecurityLog.getOperateTime().getTime() / 1000;
                        level2UserData.setTti(unixTime.toString());
                    }
                    else {
                        XxlJobLogger.log("Cannot get register time for userId: " + userId);
                        log.warn("Cannot get register time for userId: " + userId);
                    }
                    // 无注册IP地址，跳过此用户
                    if (userSecurityLog == null || StringUtils.isBlank(userSecurityLog.getIp())){
                        ipAddress = idmUserInfo.getUsedIpAddress();
                        if (ipAddress == null) {
                            XxlJobLogger.log("Cannot get register ip for userId: " + userId);
                            log.warn("Cannot get register ip for userId: " + userId);
                            continue;
                        }
                    }
                    else {
                        ipAddress = userSecurityLog.getIp();
                    }
                    level2UserData.setIp(ipAddress);
                    level2UserData.setTea(userVo.getEmail());
                    level2UserData.setMan(String.valueOf(userId));

                    // 查询用户KYC相关信息
                    UserIdRequest userIdRequest = new UserIdRequest();
                    userIdRequest.setUserId(Long.valueOf(userId));
                    APIResponse<UserKycApproveVo> userKycApproveVoResp = userKyc.getApproveUser(APIRequest.instance(userIdRequest));
                    if (userKycApproveVoResp == null || userKycApproveVoResp.getData() == null) {
                        log.error("Cannot find user kyc info for level2+ user:{}", userId);
                        continue;
                    }
                    UserKycApproveVo userKycApproveVo = userKycApproveVoResp.getData();
                    UserKycApproveVo.CheckInfo checkInfo = userKycApproveVo.getCheckInfo();

                    if (checkInfo != null) {
                        level2UserData.setBfn(Pinyin.toPinyin(checkInfo.getFirstName(), ""));
                        level2UserData.setBln(Pinyin.toPinyin(checkInfo.getLastName(),""));
                        level2UserData.setBco(checkInfo.getIssuingCountry());
                        level2UserData.setDob(checkInfo.getDob());
                        level2UserData.setBz(checkInfo.getPostalCode());
                        String memo = checkInfo.getIssuingCountry() + ":" + checkInfo.getNumber();
                        level2UserData.setMemo(memo);
                    }
                    else {
                        XxlJobLogger.log("Cannot get checkInfo for userId: " + userId);
                        log.error("Cannot get checkInfo for userId: " + userId);
                        continue;
                    }

                    int index = level2UserCounter / 1000;
                    List<Level2UserData> list = level2UserList[index];
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(level2UserData);
                    level2UserList[index] = list;
                    level2UserCounter ++;
                }
            }
            catch(Exception e) {
                XxlJobLogger.log("Last userId:" + userId, e);
                log.error("Last userId:" + userId, e);
                throw e;
            }
        }

        final List<Map<String, Object>> csvMapList = new ArrayList<>(idmUserInfos.size());
        // 分别发送校验信息
        for (List<Level1UserData> level1User : level1UserList) {
            if (level1User == null || level1User.isEmpty()) {
                continue;
            }
            Level1UserRequest level1UserRequest = new Level1UserRequest(level1User);
            APIResponse apiResponse = idmApi.consumerKycLevel1Evaluation(APIRequest.instance(level1UserRequest));
            if (APIResponse.Status.OK != apiResponse.getStatus()) {
                List<Level1UserData> errorData = (List)apiResponse.getErrorData();
                log.error("Error occurred while processing:" + errorData);
                XxlJobLogger.log("Error occurred,please check logs. total:{0}, error:{1}", level1User.size(), errorData.size());
            }
            else {
                String result = JSON.toJSONString(apiResponse);
                log.info("All {} Level1Users are sent to IDM successfully!", level1User.size());
                XxlJobLogger.log("All {0} Level1Users are sent to IDM successfully!", level1User.size());
            }

            level1User.forEach(user->{
                csvMapList.add(MapUtil.beanToMap(user));
            });
        }

        final List<Map<String, Object>> level2CsvMap = new ArrayList<>(idmUserInfos.size());
        for (List<Level2UserData> level2User : level2UserList) {
            if (level2User == null || level2User.isEmpty()) {
                continue;
            }
            Level2UserRequest level2UserRequest = new Level2UserRequest(level2User);
            APIResponse apiResponse = idmApi.consumerKycLevel2Evaluation(APIRequest.instance(level2UserRequest));
            if (APIResponse.Status.OK != apiResponse.getStatus()) {
                List<Level2UserData> errorData = (List)apiResponse.getErrorData();
                log.error("Error occurred while processing:" + errorData);
                XxlJobLogger.log("Error occurred,please check logs. total:{0}, error:{1}", level2User.size(), errorData.size());
            }
            else {
                log.info("All {} Level2+Users are sent to IDM successfully!", level2User.size());
                XxlJobLogger.log("All {0} Level2+Users are sent to IDM successfully!", level2User.size());
            }

            level2User.forEach(user->{
                level2CsvMap.add(MapUtil.beanToMap(user));
            });
        }

        if (exportCsv) {
            exportToExcel(start, end, csvMapList, level2CsvMap);
        }
    }


    private void exportToExcel(int start, int end, List<Map<String, Object>> csvMapList,
            List<Map<String, Object>> level2CsvMap) throws IOException {
        // csv 格式输出, level1 users
        String[] keys, columnNames = null;
        keys = new String[] {"tti", "man", "tea", "ip"};
        // 列名
        columnNames = new String[] {"register time", "user id", "email", "ip address"};
        Workbook userBook = ExcelUtil.createWorkBook("level1User", csvMapList, keys, keys);

        // csv 格式输出, level2+ users
        keys = new String[] {"tti", "man", "tea", "ip","bfn", "bln", "bco"};
        // 列名
        columnNames = new String[] {"register time", "user id", "email", "ip address", "first name", "last name", "country code"};
        ExcelUtil.createWorkBook(userBook,"level2User", level2CsvMap, keys, keys);

        String timeStamp = String.valueOf(System.currentTimeMillis());
        FileOutputStream fileOutputStream = new FileOutputStream("userData."+timeStamp+"."+start +"-"+end+ ".xlsx");
        userBook.write(fileOutputStream);
    }

    private void parseUserKycParam(String param, Map<String, Integer> paramMap) throws Exception{
        if (StringUtils.isBlank(param)) {
            log.info("Params is empty.");
            throw new IllegalArgumentException("Param is empty!");
        }

        String[] params = param.split(",");
        //是否输出csv files
        paramMap.put("export", 0);
        if (params.length == 2 && "true".equalsIgnoreCase(params[1].trim())) {
            paramMap.put("export", 1);
        }

        if ("*".equals(params[0].trim())) {
            paramMap.put("userId", MIN_USER_ID);
            paramMap.put("endUserId", MAX_USER_ID);
        }
        else if (params[0].trim().startsWith("redis:")) {
            Integer redisId = RedisCacheUtils.get(USER_ID_PROCESSED, Integer.class);
            Integer userId = redisId==null ? MIN_USER_ID : Integer.valueOf(redisId);
            String[] step = params[0].split(":");
            if (step.length >3) {
                throw new IllegalArgumentException("Param type not correct!");
            }
            int stepLen = Integer.valueOf(step[1]);

            // 如果格式为redis:step:startUser
            if (step.length == 3 && StringUtils.isNotBlank(step[2])) {
                userId = Integer.valueOf(step[2]);
            }

            paramMap.put("userId", userId);
            paramMap.put("endUserId", userId+stepLen);
        }
        else if (params[0].contains("-")) {
            String[] userIds = params[0].split("-");
            if (userIds.length < 1) {
                throw new IllegalArgumentException();
            }
            paramMap.put("userId", Integer.parseInt(userIds[0]));
            Integer endUserId = userIds.length==1 ? MAX_USER_ID :Integer.parseInt(userIds[1]);
            paramMap.put("endUserId", endUserId);
        }
        else {
            paramMap.put("userId", Integer.parseInt(params[0]));
            paramMap.put("endUserId", Integer.parseInt(params[0]));
        }
    }


    @Getter
    @Setter
    @AllArgsConstructor
    private class IdmUserInfo {
        /**
         * 用户基本信息
         */
        private GetUserResponse userResponse;

        /**
         * 用户注册日志，用于查询该用户的注册ip及注册时间
         */
        private UserSecurityLog userSecurityLog;

        /**
         * 用户曾经使用过的某个ip，很可能为空。
         * 因为此field只有在找不到注册ip的情况下才会填充
         */
        private String usedIpAddress;
    }
}
