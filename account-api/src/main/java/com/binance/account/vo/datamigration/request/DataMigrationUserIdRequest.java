package com.binance.account.vo.datamigration.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "迁移指定userId数据Request", value = "迁移指定userId数据Request")
@Getter
@Setter
public class DataMigrationUserIdRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 5246165150611125138L;

    @ApiModelProperty(required = true, notes = "用户id")
    @NotNull
    private Long userId;

}
