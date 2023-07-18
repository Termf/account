package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.BaseQuery;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@DefaultDB
public interface CompanyCertificateMapper {
    int deleteByPrimaryKey(@Param("userId")Long userId,@Param("id")Long id);

    int insert(CompanyCertificate record);

    int insertIgnore(CompanyCertificate record);

    /**
     * 获取未完成记录
     *
     * @return
     */
    int getIncompleteCount(Long userId);

    int updateByPrimaryKeySelective(CompanyCertificate record);

    int updateByPrimaryKey(CompanyCertificate record);
    
    /**
     * 获取最后一条有效记录
     *
     * @return
     */
    CompanyCertificate getLast(Long userId);

    /**
     * 根据ID获取企业认证
     *
     * @return
     */
    CompanyCertificate selectByPrimaryKey(@Param("userId")Long userId, @Param("id")Long id);


    /**
     * 获取企业认证记录
     *
     * @return
     */
    List<CompanyCertificate> getList(BaseQuery query);

    /**
     * 获取企业认证记录
     *
     * @return
     */
    long getListCount(BaseQuery query);

    /**
     * 根据jumioId获取
     * @return
     */
    CompanyCertificate getByJumioId(@Param("userId")Long userId,@Param("jumioId")String id);

    /**
     * 保存JUMIO 关联ID
     * @param companyCertificate
     * @return
     */
    int saveJumioId(CompanyCertificate companyCertificate);

    /**
     * 变更人脸识别信息
     * @param companyCertificate
     * @return
     */
    int updateFaceStatus(CompanyCertificate companyCertificate);

    /**
     * 获取过期或者需要同步JUMIO数据的记录
     * @param startTime
     * @param endTime
     * @return
     */
    List<CompanyCertificate> getExpiredCheckData(@Param("startCreateTime") Date startTime,
                                                 @Param("endUpdateTime") Date endTime);
}
