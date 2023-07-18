package com.binance.account.data.mapper.certificate;


import com.binance.account.data.entity.certificate.MessageMap;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface MessageMapMapper {

    int insert(MessageMap messageMap);

    int update(MessageMap messageMap);

    List<MessageMap> getAll();

    MessageMap getByCodeAndLang(@Param("code") String code, @Param("lang") String lang);

    List<MessageMap> getByCode(String code);
    
    int batchInsert(List<MessageMap> messageMaps);
    
    int batchDelete(List<MessageMap> messageMaps);
    
    List<MessageMap> batchSelectByPk(List<MessageMap> messageMaps);
    
    List<MessageMap> fuzzySeach(@Param("message")String message);
    
}
