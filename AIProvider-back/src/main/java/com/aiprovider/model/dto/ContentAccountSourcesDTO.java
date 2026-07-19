package com.aiprovider.model.dto;

import java.util.List;

public class ContentAccountSourcesDTO {
    private List<Long> sourceIds;
    private List<ContentAccountSourceRuleDTO> rules;
    public List<Long> getSourceIds(){return sourceIds;}
    public void setSourceIds(List<Long> value){sourceIds=value;}
    public List<ContentAccountSourceRuleDTO> getRules(){return rules;}
    public void setRules(List<ContentAccountSourceRuleDTO> value){rules=value;}
}
