package com.binance.account.data.mapper.tag;

import com.binance.account.data.entity.tag.TagDetailDefine;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

/**
 * @author lufei
 * @date 2018/9/28
 */
@DefaultDB
public interface TagDetailDefineMapper {

    int insert(TagDetailDefine model);

    int deleteByTagId(@Param("tagId") Long tagId);

    TagDetailDefine selectByTagId(@Param("tagId")Long tagId);

}
