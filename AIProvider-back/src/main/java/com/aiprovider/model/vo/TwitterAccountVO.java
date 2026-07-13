package com.aiprovider.model.vo;

import java.time.LocalDateTime;

public class TwitterAccountVO {
    private final Long id;
    private final String username;
    private final String sessionStatus;
    private final LocalDateTime lastLoginAt;
    private final String lastError;

    public TwitterAccountVO(Long id, String username, String sessionStatus, LocalDateTime lastLoginAt, String lastError) {
        this.id = id;
        this.username = username;
        this.sessionStatus = sessionStatus;
        this.lastLoginAt = lastLoginAt;
        this.lastError = lastError;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getSessionStatus() { return sessionStatus; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public String getLastError() { return lastError; }
}
