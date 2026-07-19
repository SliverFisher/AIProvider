package com.aiprovider.model.dto;

public class ContentAccountSourceRuleDTO {
    private Long sourceId; private Boolean enabled; private String publishTiming; private Integer publishIntervalMinutes;
    public Long getSourceId(){return sourceId;} public void setSourceId(Long value){sourceId=value;}
    public Boolean getEnabled(){return enabled;} public void setEnabled(Boolean value){enabled=value;}
    public String getPublishTiming(){return publishTiming;} public void setPublishTiming(String value){publishTiming=value;}
    public Integer getPublishIntervalMinutes(){return publishIntervalMinutes;} public void setPublishIntervalMinutes(Integer value){publishIntervalMinutes=value;}
}
