package com.aiprovider.model.vo;

import java.util.List;
import java.util.Map;

public class PageResultVO {

    private String table;
    private long total;
    private int page;
    private int size;
    private long pages;
    private List<Map<String, Object>> columns;
    private List<Map<String, Object>> rows;

    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getPages() { return pages; }
    public void setPages(long pages) { this.pages = pages; }
    public List<Map<String, Object>> getColumns() { return columns; }
    public void setColumns(List<Map<String, Object>> columns) { this.columns = columns; }
    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
}