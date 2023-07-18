package com.binance.account.domain.bo;

import com.binance.master.commons.ToString;
import com.binance.master.enums.BaseEnum;
import com.binance.master.enums.SysType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MsgNotification extends ToString {

    public enum OptType implements BaseEnum {
        REGISTER("register", "注册"),
        ACCOUNT_ACTIVE("account_active", "激活"),
        REGISTER_AND_ACTIVE("REGISTER_AND_ACTIVE", "注册并激活"),
        BIND_MOBILE("BIND_MOBILE", "绑定手机"),
        BIND_GOOGLE("BIND_GOOGLE", "绑定谷歌"),
        UNBIND_MOBILE("UNBIND_MOBILE", "解绑手机"),
        UNBIND_GOOGLE_VERIFY("UNBIND_GOOGLE_VERIFY", "解绑谷歌"),
        UPDATE_PWD("UPDATE_PWD", "修改密码"),
        SECURITY_LEVEL("SECURITY_LEVEL", "修改用户等级"),
        ANTI_PHISHING_CODE("ANTI_PHISHING_CODE","防钓鱼码"),
        TRADING_ACCOUNT("TRADING_ACCOUNT","用户交易账户"),
        SET_COMMISSION("SET_COMMISSION", "设置用户交易费"),
        ACCOUNT_LOCK("ACCOUNT_LOCK","锁定账号"),
        WITHDRAW_LIMIT("WITHDRAW_LIMIT", "设置用户提币额度"),
        CREATE_MARGIN("CREATE_MARGIN", "创建margin账户"),
        CREATE_FIAT("CREATE_FIAT", "创建FIAT账户"),
        CREATE_FUTURE("CREATE_FUTURE", "创建future账户"),

        MODIFY_SUBACCOUNT_EMAIL("MODIFY_SUBACCOUNT_EMAIL", "修改子账户邮箱"),
        RESET_SUBUSER_SECOND_VALIDATION("RESET_SUBUSER_SECOND_VALIDATION", "重置子账号2fa"),
        UPDATE_SUBUSER_PWD("UPDATE_SUBUSER_PWD", "修改子账号密码"),

        USER_CHARGE_CRYPTO("USER_CHARGE_CRYPTO", "用户虚拟币充值"),
        USER_PRODUCT_FEE("USER_PRODUCT_FEE", "用户市场手续费"),
        USER_PRODUCT_FEE_ONLY_PARENT_MARGIN("USER_PRODUCT_FEE_ONLY_PARENT_MARGIN", "用户市场手续费只是同步母账户的margin账号 "),


        REDBAG_ONE_BUTTON_REGISTER("REDBAG_ONE_BUTTON_REGISTER", "红包用户一键注册"),

        ;

        private String code;

        private String desc;

        OptType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getDesc() {
            return desc;
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private SysType sysType;
    private OptType optType;
    private Object data;

}
