package com.aiprovider.model.vo;

import java.util.List;

public class AssetBatchResultVO {
    private final int saved;
    private final List<AssetVO> items;

    public AssetBatchResultVO(int saved, List<AssetVO> items) {
        this.saved = saved;
        this.items = items;
    }

    public int getSaved() { return saved; }
    public List<AssetVO> getItems() { return items; }
}
