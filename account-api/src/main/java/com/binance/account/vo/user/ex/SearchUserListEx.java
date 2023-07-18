package com.binance.account.vo.user.ex;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SearchUserListEx extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -1105268372897277985L;

    @ApiModelProperty(readOnly = true, notes = "id")
    private Long userId;
    @ApiModelProperty(readOnly = true, notes = "账号")
    private String email;
    @ApiModelProperty(readOnly = true, notes = "手机国家")
    private String mobileCountry;
    @ApiModelProperty(readOnly = true, notes = "手机国家编码")
    private String mobileCode;
    @ApiModelProperty(readOnly = true, notes = "手机")
    private String mobile;
    @ApiModelProperty(readOnly = true,
            notes = "用户状态-使用long类型的2进制占位可以有64个状态统一0:否1:是,1:激活状态,2:禁用状态,3:锁定状态,4:特殊用户,5:种子用户,6:协议是否确认,7:手机验证,8:强制修改密码,9:是否有子账号,10:是否可以申购,11:是否可以交易,12:微信绑定,13:APP是否可以交易,14:api是否可以交易,15:BNB手续费开关")
    private Long status;
    @ApiModelProperty(readOnly = true, notes = "插入时间")
    private Date insertTime;
    @ApiModelProperty(readOnly = true, notes = "备注")
    private String remark;
    @ApiModelProperty(readOnly = true, notes = "当日密码错误次数")
    private Integer loginFailedNum;
    @ApiModelProperty(readOnly = true, notes = "登录失败日期")
    private Date loginFailedTime;
    @ApiModelProperty(readOnly = true, notes = "状态扩展字段")
    private UserStatusEx userStatusEx;
    @ApiModelProperty(readOnly = true, notes = "绑定标签数量")
    private Long bindTagCount;
    @ApiModelProperty(readOnly = true, notes = "交易账户id")
    private Long tradingAccount;
    @ApiModelProperty(readOnly = true, notes = "提币人脸识别是否激活")
    private Integer withdrawFaceStatus;

    public void setStatus(Long status) {
        this.status = status;
        userStatusEx = new UserStatusEx(status);
    }

}
