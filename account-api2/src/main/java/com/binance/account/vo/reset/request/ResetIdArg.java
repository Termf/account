package com.binance.account.vo.reset.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-01-14 18:33
 */
@ApiModel("重置流程的ID请求参数")
@Setter
@Getter
public class ResetIdArg extends ToString {

    private static final long serialVersionUID = 5321403462564094937L;

    @NotNull
    private String id;

}
