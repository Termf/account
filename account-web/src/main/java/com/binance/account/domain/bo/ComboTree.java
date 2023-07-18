package com.binance.account.domain.bo;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lufei
 * @date 2018/5/14
 */
@Data
@NoArgsConstructor
public class ComboTree {
    private Long id;
    private String text;
    private String state;
    private List<ComboTree> children;

    public ComboTree(Long id, String text, String state) {
        this.id = id;
        this.text = text;
        this.state = state;
    }

}
