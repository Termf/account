package com.binance.account.vo.device.request;

import com.binance.account.common.constant.UserDeviceConst;
import com.binance.account.common.validator.ValidateResult;
import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel(description = "设备指纹Request", value = "设备指纹Request")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDeviceRequest extends ToString {

    private static final long serialVersionUID = 1L;
    /**长度上限，数据库长度为4096，这里设置为4000，是因为有可能动态的添加device_id（约50个字符长度）*/
    private static final int CONTENT_MAX_LENGTH = 4000;

    @ApiModelProperty(required = true, notes = "用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "客户端类型：web,ios,android,pc,mac")
    @NotNull
    private String agentType;

    @ApiModelProperty(required = false, notes = "状态：0->授权设备，1->非授权设备")
    private Integer status = 0;

    @ApiModelProperty("操作来源 login、reigst等")
    @NotEmpty
    private String source;

    @ApiModelProperty(required = true, notes = "设备指纹信息（json格式）")
    @NotNull
    private Map<String, String> content;

    @ApiModelProperty("授权成功后的跳转地址")
    private String callback;


    /**
     * 校验公共信息
     * @return
     */
    public ValidateResult validate(){
        if (content == null){
            return ValidateResult.pass();
        }
        String ip = content.get(UserDeviceConst.LOGIN_IP);
        if (StringUtils.isEmpty(ip)){
            return ValidateResult.reject("IP不可为空");
        }
        //
        if (content.toString().length() > CONTENT_MAX_LENGTH){
            return ValidateResult.reject("content长度溢出");
        }
        if (StringUtils.length(source) > 16){
            return ValidateResult.reject("source长度溢出");
        }
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)  && status != null) {
            return ValidateResult.reject("status必须属于{null, 0, 1}");
        }

        return ValidateResult.pass();
    }
}
