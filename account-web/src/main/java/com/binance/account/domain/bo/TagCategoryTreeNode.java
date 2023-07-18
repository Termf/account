package com.binance.account.domain.bo;

import java.util.List;

/**
 * @author lufei
 * @date 2018/5/16
 */
public class TagCategoryTreeNode extends TagTreeNode {
    private List<? extends TagTreeNode> children;

    public List<? extends TagTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<? extends TagTreeNode> children) {
        this.children = children;
    }

}
