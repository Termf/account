package com.binance.account.vo.user.request;

import com.binance.account.vo.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

@ApiModel(description = "搜索用户Request", value = "搜索用户Request")
@Getter
@Setter
public class SearchUserListRequest extends Page {

    /**
     *
     */
    private static final long serialVersionUID = 1764186881752785825L;

    @ApiModelProperty(required = false, notes = "用戶Id")
    private Long userId;

    @ApiModelProperty(required = false, notes = "邮箱")
    private String email;//like模糊匹配查

    @ApiModelProperty(required = false, notes = "邮箱是否可以模糊查询")
    private Boolean canEmailLike;

    @ApiModelProperty(required = false, notes = "手机号")
    private String mobile;

    @ApiModelProperty(required = false, notes = "手机编码")
    private String mobileCode;

    @ApiModelProperty(required = false, notes = "开始插入时间")
    private Date startInsertTime;

    @ApiModelProperty(required = false, notes = "结束插入时间")
    private Date endInsertTime;

    @ApiModelProperty(required = false, notes = "备注")
    private String remark;

    @ApiModelProperty(required = false, notes = "状态")
    private Long status;

    @ApiModelProperty(required = false, notes = "查询位")
    private Long mask;

    @ApiModelProperty(required = false, notes = "标签ID列表")
    private List<Long> tagIds;

    @ApiModelProperty(required = false, notes = "批量邮箱")
    private List<String> emails;//in精确匹配查

    public void setEmail(String email) {
        this.email = StringUtils.trimToNull(email);
    }

    public void setMobile(String mobile) {
        this.mobile = StringUtils.trimToNull(mobile);
    }

    public void setMobileCode(String mobileCode) {
        this.mobileCode = StringUtils.trimToNull(mobileCode);
    }

}
