package com.aiprovider.model.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class TwitterPostCreateDTO {
    private Long accountId;
    private String content;
    private Integer delayMinutes;
    private List<MultipartFile> images;
    private List<Long> assetIds;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }
    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
    public List<Long> getAssetIds() { return assetIds; }
    public void setAssetIds(List<Long> assetIds) { this.assetIds = assetIds; }
}
