package com.aiprovider.model.vo;

import java.util.List;
import java.util.Map;

public class LocalGeneratedImageBatchResultVO {
    private final int saved;
    private final List<Map<String, Object>> items;

    public LocalGeneratedImageBatchResultVO(int saved, List<Map<String, Object>> items) {
        this.saved = saved;
        this.items = items;
    }

    public int getSaved() { return saved; }
    public List<Map<String, Object>> getItems() { return items; }
}
