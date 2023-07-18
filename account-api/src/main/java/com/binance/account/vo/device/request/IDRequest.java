package com.binance.account.vo.device.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author: caixinning
 * @date: 2018/05/08 18:24
 **/

@ApiModel(description = "数据库主键ID", value = "数据库主键ID")
@Getter
@Setter
public class IDRequest extends ToString {

    private static final long serialVersionUID = 6177284945429888216L;

    @NotNull
    private Long id;

}
