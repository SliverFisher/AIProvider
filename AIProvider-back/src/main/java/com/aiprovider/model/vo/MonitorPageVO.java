package com.aiprovider.model.vo;

import java.util.List;

public class MonitorPageVO<T> {
    private final List<T> items; private final long total; private final int page; private final int pageSize;
    public MonitorPageVO(List<T> items,long total,int page,int pageSize){this.items=items;this.total=total;this.page=page;this.pageSize=pageSize;}
    public List<T> getItems(){return items;} public long getTotal(){return total;} public int getPage(){return page;} public int getPageSize(){return pageSize;}
}
