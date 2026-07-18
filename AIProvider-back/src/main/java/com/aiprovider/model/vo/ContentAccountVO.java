package com.aiprovider.model.vo;

import java.time.LocalDateTime;

public class ContentAccountVO {
    private final Long id; private final String platform; private final String displayName; private final String accountHandle;
    private final String publishMode; private final String adapterType; private final boolean enabled; private final String connectionStatus; private final String adapterStatus; private final boolean sessionConfigured; private final String sessionHint;
    private final String lastError; private final LocalDateTime lastConnectedAt; private final LocalDateTime lastPublishedAt;
    public ContentAccountVO(Long id, String platform, String displayName, String accountHandle, String publishMode, boolean enabled,
                            String adapterType,String connectionStatus, String adapterStatus,boolean sessionConfigured,String sessionHint,String lastError,LocalDateTime lastConnectedAt, LocalDateTime lastPublishedAt) {
        this.id=id; this.platform=platform; this.displayName=displayName; this.accountHandle=accountHandle; this.publishMode=publishMode;
        this.adapterType=adapterType;this.enabled=enabled; this.connectionStatus=connectionStatus; this.adapterStatus=adapterStatus;this.sessionConfigured=sessionConfigured;this.sessionHint=sessionHint;this.lastError=lastError;this.lastConnectedAt=lastConnectedAt; this.lastPublishedAt=lastPublishedAt;
    }
    public Long getId(){return id;} public String getPlatform(){return platform;} public String getDisplayName(){return displayName;}
    public String getAccountHandle(){return accountHandle;} public String getPublishMode(){return publishMode;} public boolean isEnabled(){return enabled;}
    public String getConnectionStatus(){return connectionStatus;} public String getAdapterStatus(){return adapterStatus;}
    public String getAdapterType(){return adapterType;}public boolean isSessionConfigured(){return sessionConfigured;}public String getSessionHint(){return sessionHint;}public String getLastError(){return lastError;}public LocalDateTime getLastConnectedAt(){return lastConnectedAt;} public LocalDateTime getLastPublishedAt(){return lastPublishedAt;}
}
