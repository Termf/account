package com.binance.account.data.entity.tag;

import lombok.Data;

/**
 * @author lufei
 * @date 2018/6/22
 */
@Data
public class FullTagInfo extends TagInfo {

    private static final long serialVersionUID = -5809068518879044033L;

    private String pName;

    private String categoryName;

    private Long pCategoryId;

    private String pCategoryName;
}
