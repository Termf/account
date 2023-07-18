package com.binance.account.data.configs;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.data.DBConfig;
import com.binance.master.data.sharding.TableRuleSharding;


@Configuration
@MapperScan(basePackages = "com.binance.account.data", annotationClass = DefaultDB.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class DefaultDBConfig extends DBConfig {

    @Primary
    @Bean(name = "dataSource")
    @Override
    public DataSource dataSource() throws SQLException {
        JSONObject datas = JSON.parseObject(super.env.getProperty("hikaricp.data", String.class));
        return this.dataSource(datas, super.applicationContext.getBeansOfType(TableRuleSharding.class));
    }

    @Primary
    @Bean(name = DefaultDB.TRANSACTION)
    @Override
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return super.transactionManager(dataSource);
    }

    @Primary
    @Bean(name = "sqlSessionFactory")
    @Override
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        return super.sqlSessionFactory(dataSource);
    }

}
