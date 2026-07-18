package com.aiprovider.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ContentRelevanceVO {
    private final Long contentItemId; private final boolean relevant; private final BigDecimal score; private final String reason; private final String model; private final LocalDateTime checkedAt;
    public ContentRelevanceVO(Long contentItemId,boolean relevant,BigDecimal score,String reason,String model,LocalDateTime checkedAt){this.contentItemId=contentItemId;this.relevant=relevant;this.score=score;this.reason=reason;this.model=model;this.checkedAt=checkedAt;}
    public Long getContentItemId(){return contentItemId;} public boolean isRelevant(){return relevant;} public BigDecimal getScore(){return score;} public String getReason(){return reason;} public String getModel(){return model;} public LocalDateTime getCheckedAt(){return checkedAt;}
}
