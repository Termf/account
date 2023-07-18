package com.binance.account.data.mapper.tag;

import com.binance.account.data.entity.tag.TagIndicatorDetail;
import org.apache.ibatis.annotations.Param;

import com.binance.master.annotations.DefaultDB;

import java.util.List;

/**
 * @author lufei
 * @date 2018/9/28
 */
@DefaultDB
public interface TagIndicatorDetailMapper {

    int insert(TagIndicatorDetail model);

    int deleteByIndicatorId(@Param("indicatorId") Long indicatorId);

    List<TagIndicatorDetail> selectByIndicatorId(@Param("indicatorId") Long indicatorId);

}
