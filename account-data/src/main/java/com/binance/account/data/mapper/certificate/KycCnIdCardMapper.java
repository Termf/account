package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.KycCnIdCard;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface KycCnIdCardMapper {
	int deleteByPrimaryKey(Long userId);

	int insert(KycCnIdCard record);

	KycCnIdCard selectByPrimaryKey(Long userId);

	int updateByPrimaryKeySelective(KycCnIdCard record);

	long countByStatus(@Param("userIds") List<Long> userIds);

	List<KycCnIdCard> selectPageByStatus(@Param("start") Integer start, @Param("rows") Integer rows,
			@Param("userIds") List<Long> userIds, @Param("flagUser") Integer isFlagUser);
	
	List<KycCnIdCard> selectPageByStatusError(@Param("start") Integer start, @Param("rows") Integer rows,
			@Param("userIds") List<Long> userIds,@Param("status") String status,@Param("failReason") String failReason);

	List<KycCnIdCard> selectCreateFiatAccountList(@Param("start") Integer start, @Param("rows") Integer rows,
												  @Param("flagUser") Integer flagUser);
	
	List<KycCnIdCard> selectPageResetkyc(@Param("start") Integer start, @Param("rows") Integer rows,
			@Param("userIds") List<Long> userIds, @Param("flagUser") Integer isFlagUser,@Param("status") String status);
	
}