package com.binance.account.vo.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class BaseModel{

    // 开始页码
    protected int page = 1;
    // 每页显示的条数,默认10条
    protected int rows;
    // 排序字段
    protected String sort;
    // 正序:"asc" 倒序："desc"
    protected String order;

    protected long startPage;

    protected long endPage;
    protected String defaultSort;// 默认的排序
    protected Map<String, Object> paramMap = new HashMap<>();
    private Date startDate;
    private Date endDate;

    public void setPage(int page) {
        if (page == 0) {
            this.page = 1;
        } else {
            this.page = page;
        }
    }
    
    public long getStartPage() {
        return (this.page - 1) * rows;
    }
    
    public long getEndPage() {
        return this.rows;
    }

    public Map<String, Object> getParamMap() {
        this.paramMap.put("sort", this.sort);
        this.paramMap.put("order", this.order);
        this.paramMap.put("startPage", this.getStartPage());
        this.paramMap.put("endPage", this.getEndPage());
        return this.paramMap;
    }

}
