package com.aiprovider.model.vo;

import java.time.LocalDateTime;
import java.util.List;

public class TwitterPostVO {
    private final Long id;
    private final Long accountId;
    private final String username;
    private final String content;
    private final String status;
    private final String tweetUrl;
    private final String errorMessage;
    private final Integer attemptCount;
    private final LocalDateTime sentAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime scheduledAt;
    private final String source;
    private final List<TwitterMediaVO> images;

    public TwitterPostVO(Long id, Long accountId, String username, String content, String status,
                         String tweetUrl, String errorMessage, Integer attemptCount, LocalDateTime sentAt,
                         LocalDateTime createdAt, LocalDateTime scheduledAt, String source, List<TwitterMediaVO> images) {
        this.id = id; this.accountId = accountId; this.username = username; this.content = content;
        this.status = status; this.tweetUrl = tweetUrl; this.errorMessage = errorMessage;
        this.attemptCount = attemptCount; this.sentAt = sentAt; this.createdAt = createdAt;
        this.scheduledAt = scheduledAt; this.source = source; this.images = images;
    }
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public String getTweetUrl() { return tweetUrl; }
    public String getErrorMessage() { return errorMessage; }
    public Integer getAttemptCount() { return attemptCount; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getSource() { return source; }
    public List<TwitterMediaVO> getImages() { return images; }
}
