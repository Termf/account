package com.binance.account.vo.user.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@ApiModel("申请jumio验证Token返回值")
@Getter
@Setter
@NoArgsConstructor
public class JumioTokenResponse implements Serializable {

    private static final long serialVersionUID = -4483113320096853083L;
    private String authorizationToken;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
