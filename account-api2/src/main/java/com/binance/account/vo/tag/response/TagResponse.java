package com.binance.account.vo.tag.response;

import com.binance.master.commons.ToString;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lufei
 * @date 2018/6/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagResponse extends ToString {
    private static final long serialVersionUID = 7976642495075390545L;
    private boolean success;
    private String errorMsg;
    private Long id;
    private Integer total;

    public TagResponse(boolean isSuccess) {
        this.success = isSuccess;
    }

    public TagResponse(String errorMsg) {
        this.success = false;
        this.errorMsg = errorMsg;
    }

    public TagResponse(boolean isSuccess, Long id) {
        this.success = isSuccess;
        this.id = id;
    }

    public TagResponse(Integer total){
        this.success = true;
        this.total = total;
    }
}
