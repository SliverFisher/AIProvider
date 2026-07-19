package com.aiprovider.model.dto;

public class ContentSourceUpdateDTO {
    private String name; private String externalUid; private String externalHandle; private Boolean enabled;
    public String getName(){return name;} public void setName(String value){name=value;}
    public String getExternalUid(){return externalUid;} public void setExternalUid(String value){externalUid=value;}
    public String getExternalHandle(){return externalHandle;} public void setExternalHandle(String value){externalHandle=value;}
    public Boolean getEnabled(){return enabled;} public void setEnabled(Boolean value){enabled=value;}
}
