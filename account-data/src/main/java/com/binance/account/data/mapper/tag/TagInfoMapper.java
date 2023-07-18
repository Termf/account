package com.binance.account.data.mapper.tag;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.tag.FullTagInfo;
import com.binance.account.data.entity.tag.TagInfo;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface TagInfoMapper {

    List<TagInfo> selectSimpleByCategoryId(@Param("categoryId") Long categoryId);

    List<TagInfo> selectByCategoryId(@Param("categoryId") String categoryId, @Param("position") Integer position,
                                     @Param("size") Integer size);

    Long countByCategoryId(@Param("categoryId") String categoryId);

    TagInfo selectByPrimaryKey(@Param("id") String id);

    FullTagInfo selectFullById(@Param("id") String id);

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(TagInfo model);

    int updateByPrimaryKey(TagInfo model);

    List<TagInfo> selectByName(@Param("name") String name);

    List<TagInfo> selectByCategoryNameAndPTagName(@Param("categoryName") String categoryName,
                                                  @Param("pTagName") String pTagName);

    List<TagInfo> selectByPid(@Param("pid") Long pid);

}
