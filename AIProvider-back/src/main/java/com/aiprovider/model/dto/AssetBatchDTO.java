package com.aiprovider.model.dto;

import java.util.ArrayList;
import java.util.List;

public class AssetBatchDTO {
    private String platform;
    private List<AssetItemDTO> items = new ArrayList<>();
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public List<AssetItemDTO> getItems() { return items; }
    public void setItems(List<AssetItemDTO> items) { this.items = items; }
}
