package com.aiprovider.model.vo;

public class XhsPublicationResultVO {private final Long publicationId;private final String status;private final String externalPostUrl;public XhsPublicationResultVO(Long publicationId,String status,String externalPostUrl){this.publicationId=publicationId;this.status=status;this.externalPostUrl=externalPostUrl;}public Long getPublicationId(){return publicationId;}public String getStatus(){return status;}public String getExternalPostUrl(){return externalPostUrl;}}
