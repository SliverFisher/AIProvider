package com.aiprovider.model.dto;

public class ContentAccountUpdateDTO {
    private String displayName; private String accountHandle; private String publishMode; private Boolean enabled;
    public String getDisplayName(){return displayName;} public void setDisplayName(String value){displayName=value;}
    public String getAccountHandle(){return accountHandle;} public void setAccountHandle(String value){accountHandle=value;}
    public String getPublishMode(){return publishMode;} public void setPublishMode(String value){publishMode=value;}
    public Boolean getEnabled(){return enabled;} public void setEnabled(Boolean value){enabled=value;}
}
