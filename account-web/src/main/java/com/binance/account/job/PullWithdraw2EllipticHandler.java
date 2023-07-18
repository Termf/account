package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.capital.api.WithdrawApi;
import com.binance.capital.vo.withdraw.request.GetWithdrawCountRequest;
import com.binance.capital.vo.withdraw.vo.WithdrawVo;
import com.binance.inspector.api.EllipticApi;
import com.binance.inspector.vo.chainalysis.request.UserTransactionRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.amazonaws.util.StringUtils.join;
import static com.binance.master.utils.RedisCacheUtils.getRedisTemplate;


/**
 * @author user
 */
@Component
@JobHandler("pullWithdraw2EllipticHandler")
@Slf4j
public class PullWithdraw2EllipticHandler extends IJobHandler {
    /**
     * 成功的状态值
     */
    private static final int SUCCESS_STATUS = 6;
    /**
     * redis key
     */
    private static final String REDIS_KEY = "pullWithdraw2Elliptic:";
    /**
     * 调用接口成功时返回的数据key
     */
    private static final String DATA_KEY = "fullResponse";
    /**
     * elliptic接口全局最大500次每分钟
     */
    @Value("${account.elliptic.maxFrequency:450}")
    private int maxFrequency;
    /**
     * elliptic支持的币种
     */
    private final Set<String> ELLIPTIC_SUPPORT_ASSET;

    private final WithdrawApi withdrawApi;

    private final EllipticApi ellipticApi;

    public PullWithdraw2EllipticHandler(WithdrawApi withdrawApi, EllipticApi ellipticApi
            , @Value("${account.elliptic.support:BTC,ETH,LTC,BCH,XRP,ZIL}") String ellpticSupport) {
        this.withdrawApi = withdrawApi;
        this.ellipticApi = ellipticApi;
        log.info("set ELLIPTIC_SUPPORT_ASSET [{}]", ellpticSupport);
        this.ELLIPTIC_SUPPORT_ASSET = Sets.newHashSet(StringUtils.split(ellpticSupport, ","));
    }

    @Override
    public ReturnT<String> execute(String s) {
        Param param = new Gson().fromJson(s, Param.class);
        if (param == null || param.getBatchSize() == null || param.getRange() == null) {
            throw new RuntimeException("param wrong");
        }
        log.info("PullWithdraw2EllipticHandler start with param [{}]", param);
        GetWithdrawCountRequest getWithdrawCountRequest = new GetWithdrawCountRequest();
        getWithdrawCountRequest.setStatus(SUCCESS_STATUS);
        Date now = ObjectUtils.defaultIfNull(param.getBaseDate(), new Date());
        //如果是拉取全量数据 不设置开始时间
        getWithdrawCountRequest.setStartApplyTime(BooleanUtils.isTrue(param.getFull()) ? null : DateUtils.add(now, Calendar.DAY_OF_MONTH, -param.range));
        getWithdrawCountRequest.setEndApplyTime(now);
        //size
        getWithdrawCountRequest.setOffset(param.getBatchSize());
        //skip
        getWithdrawCountRequest.setPage(0);
        List<WithdrawVo> withdrawVos;
        Map<Integer, AtomicInteger> frequencyCount = Maps.newHashMapWithExpectedSize(1);
        //递归直到查光数据
        while (!(withdrawVos = getWithdrawPage(getWithdrawCountRequest)).isEmpty()) {
            withdrawVos.stream()
                    //过滤掉txId为空的、币种不支持的、已经提交过的数据
                    .filter(withdrawVo -> StringUtils.isNotBlank(withdrawVo.getTxId())
                            && ELLIPTIC_SUPPORT_ASSET.contains(withdrawVo.getCoin())
                            && BooleanUtils.isTrue(getRedisTemplate().opsForValue()
                            .setIfAbsent(getRedisKey(withdrawVo.getId()), "", param.getRange(), TimeUnit.DAYS)))
                    .forEach(withdrawVo -> {
                        UserTransactionRequest userTransactionRequest = new UserTransactionRequest();
                        userTransactionRequest.setAsset(withdrawVo.getCoin());
                        userTransactionRequest.setTxHash(withdrawVo.getTxId());
                        userTransactionRequest.setOutputAddress(withdrawVo.getAddress());
                        userTransactionRequest.setUserId(withdrawVo.getUserId() + "");
                        log.info("submit elliptic [{}]", userTransactionRequest);
                        APIResponse<?> apiResponse = null;
                        try {
                            apiResponse = ellipticApi.checkTransactionOut(APIRequest.instance(userTransactionRequest));
                            //获取当前分钟数 作为计数器的key
                            int currentMinute = LocalDateTime.now().getMinute();
                            if (!frequencyCount.containsKey(currentMinute)) {
                                //当没有当前分钟的key时  清空其他分钟的key  避免程序运行超过1小时时 读到上一小时同一分钟的计数
                                frequencyCount.clear();
                                frequencyCount.put(currentMinute, new AtomicInteger(0));
                            }
                            if (frequencyCount.get(currentMinute).addAndGet(1) >= maxFrequency) {
                                log.warn("trigger current limit [{}]", maxFrequency);
                                Thread.sleep(10 * 1000);
                            }
                            log.info("elliptic resp [{}]", apiResponse);
                        } catch (Exception e) {
                            log.error("submit elliptic err", e);
                        }
                        //调用失败或返回数据为空时删除redisKey 以便下一次拿到数据再次提交 elliptic接口有返回404等错误但是提交成功的情况 此时返回数据为空 不能确认是否真正提交成功
                        if (apiResponse == null || apiResponse.getStatus() != APIResponse.Status.OK || apiResponse.getData() == null
                                || JSON.parseObject(JSON.toJSONString(apiResponse.getData())).get(DATA_KEY) == null) {
                            RedisCacheUtils.del(getRedisKey(withdrawVo.getId()));
                        }
                    });
            getWithdrawCountRequest.setPage(getWithdrawCountRequest.getPage() + param.getBatchSize());
            //获取到的条数不足时说明到了最后一页
            if (withdrawVos.size() < param.getBatchSize()) {
                break;
            }
        }
        return IJobHandler.SUCCESS;
    }

    /**
     * 获取出金列表
     *
     * @param getWithdrawCountRequest
     * @return
     */
    private List<WithdrawVo> getWithdrawPage(GetWithdrawCountRequest getWithdrawCountRequest) {
        try {
            log.info("getWithdrawPage [{}]", getWithdrawCountRequest);
            APIResponse<List<WithdrawVo>> apiResponse = withdrawApi.getWithdrawPage(APIRequest.instance(getWithdrawCountRequest));
            if (apiResponse == null || apiResponse.getStatus() != APIResponse.Status.OK) {
                throw new RuntimeException("apiResponse empty or error");
            }
            return apiResponse.getData();
        } catch (Exception e) {
            log.error("", e);
        }
        return Collections.emptyList();
    }

    /**
     * 获取redisKey
     *
     * @param id
     * @return
     */
    private static String getRedisKey(String id) {
        return join(REDIS_KEY, id);
    }

    @Data
    public static class Param {
        /**
         * 拉多少天范围内的数据
         */
        private Integer range;
        /**
         * 每次拉多少条
         */
        private Integer batchSize;
        /**
         * 基准日期
         */
        private Date baseDate;
        /**
         * 是否拉全量数据
         */
        private Boolean full;
    }

}
