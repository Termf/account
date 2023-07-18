package com.binance.account.data.configs;

import com.binance.master.old.config.OldDB;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.binance.account.data.mapper", annotationClass = OldDB.class,
        sqlSessionFactoryRef = "oldSqlSessionFactory")
public class AccountOldDBConfig {
}
