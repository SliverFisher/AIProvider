package com.aiprovider.model.vo;

import java.time.LocalDateTime;
import java.util.List;

public class ContentSourceTestVO {
    private final Long sourceId; private final int fetchedCount; private final int newCount; private final LocalDateTime testedAt; private final List<ContentItemVO> items;
    public ContentSourceTestVO(Long sourceId,int fetchedCount,int newCount,LocalDateTime testedAt,List<ContentItemVO> items){this.sourceId=sourceId;this.fetchedCount=fetchedCount;this.newCount=newCount;this.testedAt=testedAt;this.items=items;}
    public Long getSourceId(){return sourceId;} public int getFetchedCount(){return fetchedCount;} public int getNewCount(){return newCount;} public LocalDateTime getTestedAt(){return testedAt;} public List<ContentItemVO> getItems(){return items;}
}
