package com.aiprovider.model.vo;

public class ContentPipelineTestVO {
    private final Long accountId; private final Long sourceId; private final Long contentItemId; private final String result; private final String message; private final ContentDraftVO draft; private final Long publicationId;
    public ContentPipelineTestVO(Long accountId,Long sourceId,Long contentItemId,String result,String message,ContentDraftVO draft,Long publicationId){this.accountId=accountId;this.sourceId=sourceId;this.contentItemId=contentItemId;this.result=result;this.message=message;this.draft=draft;this.publicationId=publicationId;}
    public Long getAccountId(){return accountId;} public Long getSourceId(){return sourceId;} public Long getContentItemId(){return contentItemId;} public String getResult(){return result;} public String getMessage(){return message;} public ContentDraftVO getDraft(){return draft;} public Long getPublicationId(){return publicationId;}
}
