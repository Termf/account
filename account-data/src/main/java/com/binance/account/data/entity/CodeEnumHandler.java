package com.binance.account.data.entity;

import com.binance.account.common.enums.core.BaseCodeEnum;
import com.binance.master.utils.Assert;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * Created by Shining.Cai on 2018/09/10.
 **/
public class CodeEnumHandler<E extends BaseCodeEnum> extends BaseTypeHandler<BaseCodeEnum> {

    private Class<E> type;

    public CodeEnumHandler() {}

    public CodeEnumHandler(Class<E> type){
        Assert.notNull(type, "Type argument cannot be null");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BaseCodeEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());

    }

    @Override
    public BaseCodeEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int val = rs.getInt(columnName);
        return rs.wasNull() ? null :convert(val);
    }

    @Override
    public BaseCodeEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int val = rs.getInt(columnIndex);
        return rs.wasNull() ? null : convert(val);
    }

    @Override
    public BaseCodeEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int val = cs.getInt(columnIndex);
        return cs.wasNull() ? null : convert(val);
    }

    public BaseCodeEnum convert(int code){
        E[] es = type.getEnumConstants();
        return es[0].getEnumByCode(code);
    }
}
