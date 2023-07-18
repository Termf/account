package com.binance.account.service.operationlog.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.query.es.ESQueryBuilder;
import com.binance.account.common.query.es.ESQueryCondition;
import com.binance.account.common.query.es.ESResultSet;
import com.binance.account.common.query.es.ESSortCondition;
import com.binance.account.data.entity.log.UserOperationLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.domain.bo.UserOperationLogQueryObject;
import com.binance.account.service.es.ElasticService;
import com.binance.account.service.operationlog.IUserOperationLog;
import com.binance.account.utils.MessageUtils;
import com.binance.account.vo.operationlog.UserOperationLogVo;
import com.binance.account.vo.security.request.CountLoginRequest;
import com.binance.account.vo.security.request.CountTodaysUserOperationLogsRequest;
import com.binance.account.vo.security.request.FindTodaysUserOperationLogsRequest;
import com.binance.account.vo.security.request.QueryDetailWithUuidRequest;
import com.binance.account.vo.security.request.UserIdAndIdRequest;
import com.binance.account.vo.security.request.UserOperationLogRequest;
import com.binance.account.vo.security.request.UserOperationLogUserViewRequest;
import com.binance.account.vo.security.response.UserOperationLogResultResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ip2location.IPResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
public class UserOperationLogBusiness implements IUserOperationLog {


    private static final String INDEX_NAME = "user_operation_log";

    @Resource
    private UserOperationLogMapper userOperationLogMapper;

    @Resource
    private UserIndexMapper userIndexMapper;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private ElasticService elasticService;

    @Resource
    private MessageUtils messageUtils;

    private static final Set<String> operationsInEs = ImmutableSet.of("下单", "撤单", "期货下单");

    @Value("#{'${user.visible.operations:}'.split(',')}")
    private List<String> userVisibleOperations;

    @Value("${website.isfiat:false}")
    private Boolean websiteIsFiat;


    @Override
    public Long countTodaysUserOperationLogs(CountTodaysUserOperationLogsRequest request) {
        Date todayBegin = DateUtils.truncate(new Date(), Calendar.DATE);
        Date todayEnd = new Date(todayBegin.getTime() + TimeUnit.DAYS.toMillis(1) - 1);

        return userOperationLogMapper.queryUserOperationLogPageCount(
                request.getUserId(), Arrays.asList(request.getOperation()), todayBegin, todayEnd, request.getIp(),
                null, null, null, null, null, false, false);
    }

    @Override
    public UserOperationLogResultResponse queryUserOperationLogPage(UserOperationLogRequest request) throws Exception {
        UserOperationLogResultResponse resultResponse = new UserOperationLogResultResponse();
        if (operationsInEs.contains(request.getOperation()) || (request.getUserId() == null && StringUtils.isBlank(request.getEmail()))) {
            if(websiteIsFiat){
                UserOperationLogQueryObject queryObject = new UserOperationLogQueryObject();
                BeanUtils.copyProperties(request, queryObject);
                if(org.apache.commons.lang3.StringUtils.isNotBlank(request.getOperation())){
                    queryObject.setOperations(Arrays.asList(request.getOperation()));
                }
                return queryUserOperationLogPageFromDbView(queryObject);
            }
            resultResponse = queryUserOperationLogFromEs(request);
        } else if (request.getUserId() != null) {
            UserOperationLogQueryObject queryObject = new UserOperationLogQueryObject();
            BeanUtils.copyProperties(request, queryObject);
            if(org.apache.commons.lang3.StringUtils.isNotBlank(request.getOperation())) {
                queryObject.setOperations(Arrays.asList(request.getOperation()));
            }
            resultResponse = queryUserOperationLogPageFromDb(queryObject, "");
        } else {
            User user = userMapper.queryByEmail(request.getEmail());
            if (user != null) {
                request.setUserId(user.getUserId());
                UserOperationLogQueryObject queryObject = new UserOperationLogQueryObject();
                BeanUtils.copyProperties(request, queryObject);
                if(org.apache.commons.lang3.StringUtils.isNotBlank(request.getOperation())) {
                    queryObject.setOperations(Arrays.asList(request.getOperation()));
                }
                resultResponse = queryUserOperationLogPageFromDb(queryObject, request.getEmail());
            }
        }

        return resultResponse;
    }

    @Override
    public UserOperationLogResultResponse queryUserOperationLogPageUserView(UserOperationLogUserViewRequest request) throws Exception {
        UserOperationLogQueryObject queryObject = new UserOperationLogQueryObject();
        queryObject.setUserId(request.getUserId());
        queryObject.setRequestTimeFrom(request.getRequestTimeFrom());
        queryObject.setRequestTimeTo(request.getRequestTimeTo());
        queryObject.setOffset(request.getOffset());
        queryObject.setLimit(request.getLimit());

        if (request.getStatus() == null || Integer.valueOf(0).equals(request.getStatus())) {
            queryObject.setSuccessOrHavingFailReason(true);
        } else if (Integer.valueOf(1).equals(request.getStatus())) {
            queryObject.setResponseStatus("true");
        } else if (Integer.valueOf(2).equals(request.getStatus())) {
            queryObject.setHavingFailReason(true);
        }

        if (CollectionUtils.isEmpty(request.getOperations())) {
            queryObject.setOperations(userVisibleOperations);
        } else {
            //取交集
            queryObject.setOperations(Sets.intersection(ImmutableSet.copyOf(userVisibleOperations), ImmutableSet.copyOf(request.getOperations())));
        }

        UserOperationLogResultResponse resultResponse  = queryUserOperationLogPageFromDb(queryObject, "");
        handleUserViewLogs(resultResponse.getRows());
        return resultResponse;
    }

    @Override
    public UserOperationLogResultResponse queryUserLoginLogs(UserOperationLogUserViewRequest request) throws Exception {
        UserOperationLogQueryObject queryObject = new UserOperationLogQueryObject();
        queryObject.setUserId(request.getUserId());
        queryObject.setRequestTimeFrom(request.getRequestTimeFrom());
        queryObject.setRequestTimeTo(request.getRequestTimeTo());
        queryObject.setOffset(request.getOffset());
        queryObject.setLimit(request.getLimit());
        queryObject.setSuccessOrHavingFailReason(true);
        queryObject.setOperations(Collections.singletonList("用户登陆"));

        UserOperationLogResultResponse resultResponse = queryUserOperationLogPageFromDb(queryObject, "");
        handleUserViewLogs(resultResponse.getRows());
        return resultResponse;
    }


    private void handleUserViewLogs(List<UserOperationLogVo> logs) {
        log.info("lang:{}", messageUtils.getLanguage());
        if (CollectionUtils.isNotEmpty(logs)) {
            //大概率多个operation是同一个ip，使用临时缓存提高查询效率
            Map<String, IPResult> ipLocationCache = Maps.newHashMap();
            for (UserOperationLogVo log : logs) {
                IPResult ipResult = ipLocationCache.get(log.getRealIp());
                if (ipResult == null) {
                    ipLocationCache.put(log.getRealIp(), ipResult = IP2LocationUtils.getDetail(log.getRealIp()));
                }
                if (ipResult != null) {
                    log.setLocation(ipResult.getCity() + ", " + ipResult.getCountryShort());
                }

                String operationKey = "user.operation." + log.getOperation();
                String translatedOperation = messageUtils.getI18nMessage(operationKey);
                if (!operationKey.equals(translatedOperation)) {
                    log.setOperation(translatedOperation);
                }

                if (StringUtils.isNotBlank(log.getResponseStatus())
                        && !StringUtils.equalsAnyIgnoreCase(log.getResponseStatus(), Boolean.TRUE.toString(), Boolean.FALSE.toString())) {
                    String translatedFailReason = messageUtils.getI18nMessage("user.operation.failReason." + log.getResponseStatus());
                    log.setResponseStatus(Boolean.FALSE.toString());
                    if (!StringUtils.equals(log.getResponseStatus(), translatedFailReason)) {
                        log.setFailReason(translatedFailReason);
                    } else {
                        log.setFailReason("");
                    }
                }
                if (StringUtils.isBlank(log.getClientType())) {
                    log.setClientType("web");
                }
                log.setRequest("");
                log.setUserAgent("");
                log.setResponse("");
                log.setApikey("");
                log.setUserId(null);
            }
        }
    }

    @Override
    public UserOperationLogResultResponse findTodaysUserOperationLogs(FindTodaysUserOperationLogsRequest request) {
        SimpleDateFormat indexFormat = new SimpleDateFormat("yyyyMMdd");
        String suffix = indexFormat.format(new Date());
        //查今天的index
        String index = INDEX_NAME + suffix;

        Map<String, Object> params = buildQuery(request).build();
        ESResultSet resultSet = null;
        UserOperationLogResultResponse result = new UserOperationLogResultResponse();
        try {
            resultSet = elasticService.search(String.format("/%s/_search", index), params);
        } catch (Exception e) {
            result.setTotal(0);
            return result;
        }
        List<UserOperationLogVo> logs = parseResults(resultSet);
        result.setRows(logs);
        result.setTotal(resultSet.getTotal());
        return result;
    }

    @Override
    public Long countDistinctLogin(CountLoginRequest request) {
        String key = "stat.pa."+request.getStartTime().getTime()/1000+"."+request.getEndTime().getTime()/1000;
        String val = RedisCacheUtils.get(key);
        if(val == null){
            Long count = userOperationLogMapper.countDistinctLogin(request.getStartTime(), request.getEndTime());
            val = count + "";
            RedisCacheUtils.set(key, val, 24 * 3600);
        }
        return Long.parseLong(val);
    }

    @Override
    public UserOperationLogVo queryDetailWithUuid(QueryDetailWithUuidRequest request) {
        log.info("queryDetailWithUuid request:{}", request);
        UserOperationLog uol = userOperationLogMapper.queryDetailWithUuid(request.getUserId(), request.getUuid());
        UserOperationLogVo vo = new UserOperationLogVo();
        if(uol != null){
            BeanUtils.copyProperties(uol, vo);
        }
        log.info("queryDetailWithUuid response:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(vo)), "email", "request", "apikey");
        return vo;
    }

    public List<UserOperationLogVo> parseResults(ESResultSet resultSet){
        Map<Long, String> userIdEmail = new HashMap<>();
        List<UserOperationLogVo> logs = new ArrayList<>(resultSet.getHits().size());
        resultSet.getHits().toJavaList(JSONObject.class).forEach(hit->{
            // 获取原始数据
            //JSONObject source = hit.getJSONObject("_source");
            //将json中的下划线数据转换为对象的值
            UserOperationLogVo log = JSON.parseObject(hit.getString("_source"), UserOperationLogVo.class);
            logs.add(log);
        });

        if (CollectionUtils.isEmpty(logs))  {
            return logs;
        }

        Set<Long> userIds = logs.stream().map(log -> log.getUserId()).filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(userIds)) {
            Iterable<List<Long>> userIdPartitions = Iterables.partition(userIds, 100);
            for (List<Long> part : userIdPartitions) {
                userIndexMapper.selectByUserIds(part).forEach(
                        ui -> userIdEmail.put(ui.getUserId(), ui.getEmail())
                );
            }

            logs.forEach(l -> l.setEmail(userIdEmail.get(l.getUserId())));
        }
        return logs;
    }


    private UserOperationLogResultResponse queryUserOperationLogFromEs(UserOperationLogRequest request) {
        UserOperationLogResultResponse userOperationLogResultResponse = new UserOperationLogResultResponse();
        String indices = INDEX_NAME + "*";
        Map<String, Object> params = buildQuery(request).build();
        ESResultSet resultSet = elasticService.search(String.format("/%s/_search", indices), params);
        List<UserOperationLogVo> logs = parseResults(resultSet);
        userOperationLogResultResponse.setRows(logs);
        userOperationLogResultResponse.setTotal(resultSet.getTotal());

        return userOperationLogResultResponse;
    }

    private static ESQueryBuilder buildQuery(FindTodaysUserOperationLogsRequest request) {
        List<ESQueryCondition> mustConditions = new ArrayList<>();
        List<ESQueryCondition> mustNotConditions = new ArrayList<>();
        mustNotConditions.add(ESQueryCondition.term("userId", String.valueOf(request.getExcludeUserId())));
        mustConditions.add(ESQueryCondition.term("realIp", request.getIp()));
        mustConditions.add(ESQueryCondition.term("operation", request.getOperation()));

        ESQueryBuilder builder = ESQueryBuilder.instance().must(mustConditions)
                .mustNot(mustNotConditions)
                .limit(0, 100);
        builder.setSortCondition(Arrays.asList(ESSortCondition.desc("requestTime")));
        return builder;
    }

    private static ESQueryBuilder buildQuery(UserOperationLogRequest request) {
        List<ESQueryCondition> mustConditions = new ArrayList<>();
        List<ESQueryCondition> shouldConditions = new ArrayList<>();
        if (request.getUserId() != null) {
            //queryBuilder.must(QueryBuilders.termQuery("userId", request.getUserId()));
            mustConditions.add(ESQueryCondition.term("userId", String.valueOf(request.getUserId())));
        }
        if (StringUtils.isNotBlank(request.getApikey())) {
            //queryBuilder.must(QueryBuilders.termQuery("apikey", request.getApikey()));
            mustConditions.add(ESQueryCondition.term("apikey", request.getApikey()));
        }
        if (StringUtils.isNotBlank(request.getClientType())) {
            //queryBuilder.must(QueryBuilders.termQuery("clientType", request.getClientType()));
            mustConditions.add(ESQueryCondition.term("clientType", request.getClientType()));
        }
        if (StringUtils.isNotBlank(request.getIp())) {
            //queryBuilder.must(QueryBuilders.fuzzyQuery("fullIp", request.getIp()));
            mustConditions.add(ESQueryCondition.wildcard("fullIp", request.getIp() + "*"));
        }
        if (StringUtils.isNotBlank(request.getOperation())) {
            //queryBuilder.must(QueryBuilders.termQuery("operation", request.getOperation()));
            mustConditions.add(ESQueryCondition.term("operation", request.getOperation()));
        }
        if (StringUtils.isNotBlank(request.getRequest())) {
            //queryBuilder.must(QueryBuilders.termQuery("request", request.getRequest()));
            mustConditions.add(ESQueryCondition.wildcard("request", request.getRequest() + "*"));
        }
        if (StringUtils.isNotBlank(request.getResponse())) {
            //queryBuilder.must(QueryBuilders.fuzzyQuery("response", request.getResponse()));
            mustConditions.add(ESQueryCondition.wildcard("response", "{*" + request.getResponse() + "*"));
        }
        if (request.getRequestTimeFrom() != null || request.getRequestTimeTo() != null) {
            Long from = request.getRequestTimeFrom() == null ? null : request.getRequestTimeFrom().getTime();
            Long to = request.getRequestTimeTo() == null ? null : request.getRequestTimeTo().getTime();
            mustConditions.add(ESQueryCondition.range("requestTime", from, to));
        }
        if (CollectionUtils.isNotEmpty(request.getRealIpList())) {
            if (Integer.valueOf(1).equals(request.getLikeSearch())) {
                if (request.getRealIpList().size() == 1) {
                    mustConditions.add(ESQueryCondition.wildcard("realIp", request.getRealIpList().get(0) + "*"));
                } else {
                    request.getRealIpList().forEach(
                            realIp -> shouldConditions.add(ESQueryCondition.wildcard("realIp", realIp + "*"))
                    );
                }
            } else {
                if (request.getRealIpList().size() == 1) {
                    mustConditions.add(ESQueryCondition.term("realIp", request.getRealIpList().get(0)));
                } else {
                    request.getRealIpList().forEach(
                            realIp -> shouldConditions.add(ESQueryCondition.term("realIp", realIp))
                    );
                }
            }
        }

        ESQueryBuilder builder = ESQueryBuilder.instance().must(mustConditions)
                                                            .should(shouldConditions)
                                                            .limit(request.getOffset(), request.getLimit());
        builder.setSortCondition(Arrays.asList(ESSortCondition.desc("requestTime")));
        if (CollectionUtils.isNotEmpty(shouldConditions)) {
            builder.setMinimumShouldMatch(1);
        }
        return builder;

    }


    private UserOperationLogResultResponse queryUserOperationLogPageFromDb(UserOperationLogQueryObject request, String email) {
        if (request.getLimit() <= 0 || request.getLimit() > 100000) {
            request.setLimit(20);
        }

        if (StringUtils.isEmpty(email)) {
            List<User> users = userMapper.selectByUserIds(Arrays.asList(request.getUserId()));
            email = CollectionUtils.isEmpty(users) ? "" : users.get(0).getEmail();
        }

        UserOperationLogResultResponse userOperationLogResultResponse = new UserOperationLogResultResponse();
        Long count = userOperationLogMapper.queryUserOperationLogPageCount(
                request.getUserId(), request.getOperations(), request.getRequestTimeFrom(), request.getRequestTimeTo(),
                request.getIp(), request.getClientType(), request.getApikey(), request.getRequest(), request.getResponse(),
                request.getResponseStatus(), request.isSuccessOrHavingFailReason(), request.isHavingFailReason());

        userOperationLogResultResponse.setTotal(count);
        if (count == 0) {
            return userOperationLogResultResponse;
        }

        List<UserOperationLog> userOperationLogs = userOperationLogMapper.queryUserOperationLogPage(
                request.getUserId(), request.getOperations(), request.getRequestTimeFrom(), request.getRequestTimeTo(),
                request.getIp(), request.getClientType(), request.getApikey(), request.getRequest(), request.getResponse(),
                request.getResponseStatus(), request.isSuccessOrHavingFailReason(), request.isHavingFailReason(),
                request.getLimit(), request.getOffset());

        List<UserOperationLogVo> result = new ArrayList<>(userOperationLogs.size());
        if (CollectionUtils.isNotEmpty(userOperationLogs)) {
            for (UserOperationLog log : userOperationLogs) {
                UserOperationLogVo logVo = new UserOperationLogVo();
                BeanUtils.copyProperties(log, logVo);
                logVo.setId(log.getId() + "");
                logVo.setEmail(email);
                result.add(logVo);
            }
        }
        userOperationLogResultResponse.setRows(result);
        return userOperationLogResultResponse;
    }

    private UserOperationLogResultResponse queryUserOperationLogPageFromDbView(UserOperationLogQueryObject request){
        if (request.getLimit() <= 0 || request.getLimit() > 100000) {
            request.setLimit(20);
        }

        UserOperationLogResultResponse userOperationLogResultResponse = new UserOperationLogResultResponse();
        Long count = userOperationLogMapper.countUserOperationLogByView(
                request.getOperations(), request.getRequestTimeFrom(), request.getRequestTimeTo(),request.getRealIpList(),
                request.getIp(), request.getClientType(), request.getApikey(), request.getRequest(), request.getResponseStatus());

        userOperationLogResultResponse.setTotal(count);
        if (count == 0) {
            return userOperationLogResultResponse;
        }

        List<UserOperationLog> userOperationLogs = userOperationLogMapper.queryUserOperationLogListByView(
                request.getOperations(), request.getRequestTimeFrom(), request.getRequestTimeTo(),request.getRealIpList(),
                request.getIp(), request.getClientType(), request.getApikey(), request.getRequest(), request.getResponseStatus(),
                request.getLimit(), request.getOffset());

        List<UserOperationLogVo> result = new ArrayList<>(userOperationLogs.size());
        if (CollectionUtils.isNotEmpty(userOperationLogs)) {
            for (UserOperationLog log : userOperationLogs) {
                UserOperationLogVo logVo = new UserOperationLogVo();
                BeanUtils.copyProperties(log, logVo);
                logVo.setId(log.getId() + "");
                result.add(logVo);
            }
        }
        userOperationLogResultResponse.setRows(result);
        return userOperationLogResultResponse;
    }

	@Override
	public UserOperationLogVo queryDetail(UserIdAndIdRequest request) {
        log.info("queryDetail request:{}", request);
		UserOperationLog uol = userOperationLogMapper.queryDetail(request.getUserId(),request.getId());
        UserOperationLogVo vo = new UserOperationLogVo();
        if(uol != null){
            BeanUtils.copyProperties(uol, vo);
        }
        log.info("queryDetail response:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(vo)), "email", "request", "apikey");
		return vo;
	}



}
