// Generated by the devefx compiler. DO NOT EDIT!
package com.binance.account.vo.sso;

import com.binance.master.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * User 用户登录
 *
 * @date 2018-01-09 11:49:50
 */
@Getter
@Setter
public class UserVo implements Serializable {

    private static final long serialVersionUID = -7264586240498718720L;
    private Long userId; // id
    private String email; // 账号
    private String password; // 密码
    private String salt; // 加密
    private Long status; // 用户状态：使用long类型的2进制占位可以有64个状态统一0:否1:是,1:激活状态,2:禁用状态,3:锁定状态,4:特殊用户,5:种子用户,6:协议是否确认,7:手机验证,8:强制修改密码,9:是否有子账号,10:是否可以申购,11:是否可以交易,12:微信绑定,13:APP是否可以交易,14:api是否可以交易,,15:BNB手续费开关
    private Date insertTime; // 插入时间
    private Date updateTime; // 更新时间

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
