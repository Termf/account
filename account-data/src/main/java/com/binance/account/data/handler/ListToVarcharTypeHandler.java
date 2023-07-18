package com.binance.account.data.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * List<String>  varchar
 *
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListToVarcharTypeHandler extends BaseTypeHandler<List<String>> {

    /**
     * 讲"1,2"转化为数组对象
     * 
     * @param columnValue
     * @return
     */
    private List<String> getStringArray(String columnValue) {
        if (!StringUtils.hasText(columnValue)) {
            return new ArrayList<>(0);
        }
        String[] tmp = columnValue.split(",");
        List<String> lst = new ArrayList<>(tmp.length);
        for (String string : tmp) {
            lst.add(string);
        }
        return lst;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType)
            throws SQLException {
        if (CollectionUtils.isEmpty(parameter)) {
            ps.setString(i, "");
            return;
        }
        StringBuffer result = new StringBuffer(parameter.size() * 2);
        for (String value : parameter) {
            result.append(value).append(",");
        }
        result.deleteCharAt(result.length() - 1);
        ps.setString(i, result.toString());
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return this.getStringArray(rs.getString(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return this.getStringArray(rs.getString(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return this.getStringArray(cs.getString(columnIndex));
    }

}
