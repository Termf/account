package com.binance.account.data.entity.tag;

import com.binance.master.commons.ToString;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lufei
 * @date 2018/10/11
 */
@Data
@NoArgsConstructor
public class TagPermission extends ToString {
    private static final long serialVersionUID = 5546914723264725648L;

    private Long id;
    /**
     * 标签类ID
     */
    private Long categoryId;
    /**
     * 角色ID
     */
    private String roleId;
}
