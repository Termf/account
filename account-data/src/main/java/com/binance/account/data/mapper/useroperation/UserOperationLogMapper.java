package com.binance.account.data.mapper.useroperation;

import com.binance.account.data.entity.log.UserOperationLog;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;
import org.javasimon.aop.Monitored;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@DefaultDB
@Monitored
public interface UserOperationLogMapper {
    /**
     * 这个方法是处理用户行为日志历史数据专用的 不具有通用性 不要使用！！！
     * 分页查询
     *
     * @param skip
     * @param size
     * @param start
     * @param end
     * @param userId
     * @return
     */
    List<UserOperationLog> page(@Param("skip") int skip, @Param("size") int size, @Param("start") Date start
            , @Param("end") Date end, @Param("userId") Long userId);

    int insert(UserOperationLog record);

    int batchInsert(List<UserOperationLog> records);

    List<UserOperationLog> queryUserOperationLogPage(@Param("userId") Long userId, @Param("operationList") Collection<String> operationList,
                                                     @Param("requestTimeFrom") Date requestTimeFrom, @Param("requestTimeTo") Date requestTimeTo,
                                                     @Param("ip") String ip, @Param("clientType") String clientType, @Param("apikey") String apikey,
                                                     @Param("request") String request, @Param("response") String response,
                                                     @Param("responseStatus") String responseStatus,
                                                     @Param("successOrHavingFailReason") boolean successOrHavingFailReason,
                                                     @Param("havingFailReason") boolean havingFailReason,
                                                     @Param("limit") int limit, @Param("offset") int offset);

    Long queryUserOperationLogPageCount(@Param("userId") Long userId, @Param("operationList") Collection<String> operationList,
                                        @Param("requestTimeFrom") Date requestTimeFrom, @Param("requestTimeTo") Date requestTimeTo,
                                        @Param("ip") String ip, @Param("clientType") String clientType, @Param("apikey") String apikey,
                                        @Param("request") String request, @Param("response") String response,
                                        @Param("responseStatus") String responseStatus,
                                        @Param("successOrHavingFailReason") boolean successOrHavingFailReason,
                                        @Param("havingFailReason") boolean havingFailReason);

    UserOperationLog queryDetail(@Param("userId") Long userId, @Param("id") Long id);

    UserOperationLog queryDetailWithUuid(@Param("userId") Long userId, @Param("uuid") String uuid);

    Long countDistinctLogin(@Param("requestTimeFrom") Date requestTimeFrom, @Param("requestTimeTo") Date requestTimeTo);

    /**
     * 查询条件在无法得知userId的情况下，通过视图来查询用户操作日志列表
     *
     * @param operationList
     * @param requestTimeFrom
     * @param requestTimeTo
     * @param realIpList
     * @param ip
     * @param clientType
     * @param apikey
     * @param request
     * @param responseStatus
     * @param limit
     * @param offset
     * @return
     */
    List<UserOperationLog> queryUserOperationLogListByView(@Param("operationList") Collection<String> operationList,
                                                           @Param("requestTimeFrom") Date requestTimeFrom, @Param("requestTimeTo") Date requestTimeTo,
                                                           @Param("realIpList") List<String> realIpList, @Param("ip") String ip, @Param("clientType") String clientType,
                                                           @Param("apikey") String apikey, @Param("request") String request,
                                                           @Param("responseStatus") String responseStatus,
                                                           @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 查询条件在无法得知userId的情况下，通过视图来统计用户操作日志记录数
     *
     * @param operationList
     * @param requestTimeFrom
     * @param requestTimeTo
     * @param realIpList
     * @param ip
     * @param clientType
     * @param apikey
     * @param request
     * @param responseStatus
     * @return
     */
    Long countUserOperationLogByView(@Param("operationList") Collection<String> operationList,
                                     @Param("requestTimeFrom") Date requestTimeFrom, @Param("requestTimeTo") Date requestTimeTo,
                                     @Param("realIpList") List<String> realIpList, @Param("ip") String ip, @Param("clientType") String clientType,
                                     @Param("apikey") String apikey, @Param("request") String request,
                                     @Param("responseStatus") String responseStatus);

    UserOperationLog queryUserLastLog(@Param("userId") Long userId);
}
