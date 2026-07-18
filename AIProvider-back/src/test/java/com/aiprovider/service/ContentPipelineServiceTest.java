package com.aiprovider.service;

import com.aiprovider.mapper.ContentOperationsMapper;
import com.aiprovider.model.vo.*;
import com.aiprovider.repository.ContentOperationsRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContentPipelineServiceTest {
    @Test void filtersIrrelevantLatestItemWithoutCreatingPublication(){
        ContentOperationsRepository repository=mock(ContentOperationsRepository.class);ContentSourceService sources=mock(ContentSourceService.class);ContentRelevanceService relevance=mock(ContentRelevanceService.class);ContentDraftService drafts=mock(ContentDraftService.class);Map<String,Object> account=new HashMap<>();account.put("publishMode","AUTO");when(repository.findAccount(1)).thenReturn(account);when(repository.findAccountSourceIds(1)).thenReturn(Collections.singletonList(2L));ContentItemVO item=item("PENDING");when(sources.testFetch(2)).thenReturn(new ContentSourceTestVO(2L,1,1,LocalDateTime.now(),Collections.singletonList(item)));when(relevance.classify(3)).thenReturn(new ContentRelevanceVO(3L,false,null,"不相关","gemini",LocalDateTime.now()));
        ContentPipelineTestVO result=new ContentPipelineService(repository,sources,relevance,drafts,mock(XiaohongshuPublicationService.class)).testAccount(1).get(0);assertEquals("FILTERED",result.getResult());verify(drafts,never()).createXiaohongshuDraft(anyLong());verify(repository,never()).insertPublication(any());
    }
    @Test void deduplicatesExistingPublication(){
        ContentOperationsRepository repository=mock(ContentOperationsRepository.class);ContentSourceService sources=mock(ContentSourceService.class);ContentDraftService drafts=mock(ContentDraftService.class);Map<String,Object> account=new HashMap<>();account.put("publishMode","AUTO");when(repository.findAccount(1)).thenReturn(account);when(repository.findAccountSourceIds(1)).thenReturn(Collections.singletonList(2L));when(sources.testFetch(2)).thenReturn(new ContentSourceTestVO(2L,1,0,LocalDateTime.now(),Collections.singletonList(item("RELEVANT"))));ContentDraftVO draft=new ContentDraftVO(8L,3L,"XIAOHONGSHU","标题","正文",Collections.emptyList(),"gemini","READY");when(drafts.createXiaohongshuDraft(3)).thenReturn(draft);when(repository.findPublicationId(8,1)).thenReturn(9L);Map<String,Object> published=new HashMap<>();published.put("status","PUBLISHED");when(repository.findPublicationDetails(9)).thenReturn(published);
        ContentPipelineTestVO result=new ContentPipelineService(repository,sources,mock(ContentRelevanceService.class),drafts,mock(XiaohongshuPublicationService.class)).testAccount(1).get(0);assertEquals("ALREADY_EXISTS",result.getResult());verify(repository,never()).insertPublication(any());
    }
    private static ContentItemVO item(String relevance){return new ContentItemVO(3L,2L,"post","url","author","text",LocalDateTime.now(),"COLLECTED",relevance,null,null,null,LocalDateTime.now());}
}
