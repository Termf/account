package com.binance.account.vo.tag.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lufei
 * @date 2018/10/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagCheckedCategoryResponse extends ToString {

    private static final long serialVersionUID = -5892629588188767532L;

    @ApiModelProperty(required = true, notes = "标签组ID")
    private List<Long> categoryIds;
}
