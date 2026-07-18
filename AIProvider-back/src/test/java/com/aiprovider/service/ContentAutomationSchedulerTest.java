package com.aiprovider.service;

import com.aiprovider.mapper.ContentOperationsMapper;
import com.aiprovider.model.vo.ContentPipelineTestVO;
import com.aiprovider.repository.ContentOperationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContentAutomationSchedulerTest {
    @Test void runsOnlyRepositorySelectedDueBindingsAndAuditsResult(){ContentOperationsRepository repository=mock(ContentOperationsRepository.class);ContentPipelineService pipeline=mock(ContentPipelineService.class);Map<String,Object> due=new HashMap<>();due.put("accountId",1L);due.put("sourceId",2L);when(repository.findDueBindings()).thenReturn(Collections.singletonList(due));when(repository.insertOperationRun(any(ContentOperationsMapper.OperationRunRecord.class))).thenReturn(7L);when(pipeline.processBoundSource(1,2)).thenReturn(new ContentPipelineTestVO(1L,2L,3L,"PUBLISHED","ok",null,4L));new ContentAutomationScheduler(repository,pipeline,new ObjectMapper()).runDue();verify(repository).finishOperationRun(eq(7L),contains("PUBLISHED"));verify(repository,never()).failOperationRun(anyLong(),anyString());}
}
