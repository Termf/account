package com.binance.account.vo.apimanage.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@ApiModel("分页结果对象")
@Getter
@Setter
public class PagingResult<T> extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = 5304034131217661662L;

    @ApiModelProperty
    private long total;
    @ApiModelProperty
    private List<T> rows;

    public PagingResult() {

    }

    public PagingResult(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
    }

    public long getTotal() {
        if (total == 0 && rows != null) {
            return rows.size();
        }
        return total;
    }

}
