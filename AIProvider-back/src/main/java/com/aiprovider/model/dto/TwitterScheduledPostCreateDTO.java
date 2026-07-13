package com.aiprovider.model.dto;

import java.util.List;

public class TwitterScheduledPostCreateDTO {
    private Long accountId;
    private String content;
    private Integer delayMinutes;
    private List<TwitterLocalMediaDTO> images;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }
    public List<TwitterLocalMediaDTO> getImages() { return images; }
    public void setImages(List<TwitterLocalMediaDTO> images) { this.images = images; }
}
