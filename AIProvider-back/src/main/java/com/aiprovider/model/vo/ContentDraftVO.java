package com.aiprovider.model.vo;

import java.util.List;

public class ContentDraftVO {
    private final Long id; private final Long contentItemId; private final String platform; private final String title; private final String body; private final List<String> tags; private final String modelName; private final String reviewStatus;
    public ContentDraftVO(Long id,Long contentItemId,String platform,String title,String body,List<String> tags,String modelName,String reviewStatus){this.id=id;this.contentItemId=contentItemId;this.platform=platform;this.title=title;this.body=body;this.tags=tags;this.modelName=modelName;this.reviewStatus=reviewStatus;}
    public Long getId(){return id;} public Long getContentItemId(){return contentItemId;} public String getPlatform(){return platform;} public String getTitle(){return title;} public String getBody(){return body;} public List<String> getTags(){return tags;} public String getModelName(){return modelName;} public String getReviewStatus(){return reviewStatus;}
}
