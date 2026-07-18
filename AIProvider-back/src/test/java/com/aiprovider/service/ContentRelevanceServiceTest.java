package com.aiprovider.service;

import com.aiprovider.mapper.ContentAiMapper;
import com.aiprovider.model.vo.ContentRelevanceVO;
import com.aiprovider.repository.ContentAiRepository;
import com.aiprovider.repository.ContentOperationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContentRelevanceServiceTest {
    @Test void persistsRelevantDecisionFromStrictJson(){
        Fixture f=new Fixture();when(f.client.generateJson(any(),anyString(),anyString())).thenReturn("{\"relevant\":true,\"score\":0.92,\"reason\":\"讨论大模型\"}");
        ContentRelevanceVO result=f.service.classify(9);assertTrue(result.isRelevant());assertEquals(new BigDecimal("0.92"),result.getScore());verify(f.content).updateContentRelevance(9,"RELEVANT",new BigDecimal("0.92"),"讨论大模型");verify(f.ai).markSucceeded(eq(12L),contains("relevant"),anyLong());
    }
    @Test void rejectsMalformedModelOutputAndMarksItemFailed(){
        Fixture f=new Fixture();when(f.client.generateJson(any(),anyString(),anyString())).thenReturn("yes");
        ContentAiException error=assertThrows(ContentAiException.class,()->f.service.classify(9));assertEquals("INVALID_CLASSIFICATION",error.getCode());verify(f.content).updateContentRelevance(eq(9L),eq("FAILED"),isNull(),contains("有效 JSON"));verify(f.ai).markFailed(eq(12L),eq("INVALID_CLASSIFICATION"),anyString(),anyLong());
    }
    private static class Fixture {final ContentAiConfigService config=mock(ContentAiConfigService.class);final GeminiContentClient client=mock(GeminiContentClient.class);final ContentAiRepository ai=mock(ContentAiRepository.class);final ContentOperationsRepository content=mock(ContentOperationsRepository.class);final ContentRelevanceService service;
        Fixture(){Map<String,Object> item=new HashMap<>();item.put("rawText","Gemini 发布了新模型");item.put("authorName","Google AI");item.put("sourceUrl","https://x.com/1");when(content.findContentItem(9)).thenReturn(item);when(config.runtime()).thenReturn(new GeminiRuntimeConfig(true,"https://generativelanguage.googleapis.com","gemini","key","判断 AI 相关性并给出明确理由","rewrite","reply",BigDecimal.ZERO,512));when(ai.insertGeneration(any(ContentAiMapper.GenerationRecord.class))).thenReturn(12L);service=new ContentRelevanceService(config,client,ai,content,new ObjectMapper());}
    }
}
