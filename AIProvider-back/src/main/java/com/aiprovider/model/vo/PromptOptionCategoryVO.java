package com.aiprovider.model.vo;

public class PromptOptionCategoryVO {
    private final String category;
    private final String label;
    private final int sortOrder;
    private final boolean multiple;

    public PromptOptionCategoryVO(String category, String label, int sortOrder, boolean multiple) {
        this.category = category;
        this.label = label;
        this.sortOrder = sortOrder;
        this.multiple = multiple;
    }

    public String getCategory() { return category; }
    public String getKey() { return category; }
    public String getLabel() { return label; }
    public int getSortOrder() { return sortOrder; }
    public boolean isMultiple() { return multiple; }
}
