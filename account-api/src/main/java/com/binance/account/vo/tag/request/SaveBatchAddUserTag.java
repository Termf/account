package com.binance.account.vo.tag.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveBatchAddUserTag {
    private String userId;
    private Long tagId;
    private String categoryName;
    private String tagName;
    private String value;
    private String remark;
    private String lastUpdatedBy;
}
