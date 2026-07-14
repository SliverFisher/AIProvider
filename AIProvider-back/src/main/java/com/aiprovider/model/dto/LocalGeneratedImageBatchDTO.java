package com.aiprovider.model.dto;

import java.util.ArrayList;
import java.util.List;

public class LocalGeneratedImageBatchDTO {
    private String platform;
    private List<LocalGeneratedImageItemDTO> items = new ArrayList<>();
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public List<LocalGeneratedImageItemDTO> getItems() { return items; }
    public void setItems(List<LocalGeneratedImageItemDTO> items) { this.items = items; }
}
