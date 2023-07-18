package com.binance.account.data.mapper.certificate;

import com.binance.account.common.enums.JumioType;
import com.binance.account.common.query.BaseQuery;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@DefaultDB
public interface JumioMapper {
    int deleteByPrimaryKey(@Param("userId")Long userId,@Param("id")String id);

    int insert(Jumio record);

    int updateByPrimaryKeySelective(Jumio record);

    Jumio selectByPrimaryKey(@Param("userId")Long userId, @Param("id")String id);

    /**
     * 获取所有Jumio列表
     *
     * @return
     */
    List<Jumio> getList(BaseQuery query);

    /**
     * 获取所有Jumio列表
     *
     * @return
     */
    long getListCount(BaseQuery query);

    /**
     * 获取所有超时的记录
     *
     * @return
     */
    List<Jumio> getExpiredList();

    /**
     * 获取一天提交总数
     *
     * @return
     */
    int getDailySubmitCount(@Param("userId")Long userId, @Param("type")Integer type);

    /**
     * 用户查询某段时间内有问题需要修复数据的JUMIO信息
     * @param startTime
     * @param endTime
     * @return
     */
    List<Jumio> getExpiredDateErrorData(@Param("startTime") Date startTime,
                                        @Param("endTime") Date endTime);

    /**
     * 修复JUMIO的证件过期时间
     * @param jumio
     * @return
     */
    int updateExpireDateError(Jumio jumio);
    
    /**
     * 修改jumio ocr 结果
     * @param jumio
     * @return
     */
    int updateOcrResult(Jumio jumio);    
}
