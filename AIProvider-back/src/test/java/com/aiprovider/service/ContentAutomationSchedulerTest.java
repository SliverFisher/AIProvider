package com.aiprovider.service;

import com.aiprovider.mapper.ContentOperationsMapper;
import com.aiprovider.model.vo.ContentPipelineTestVO;
import com.aiprovider.model.vo.ContentSourceTestVO;
import com.aiprovider.repository.ContentOperationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContentAutomationSchedulerTest {
    @Test
    void newlyCollectedContentTriggersImmediateBindings() {
        ContentOperationsRepository repository = mock(ContentOperationsRepository.class);
        ContentSourceService sources = mock(ContentSourceService.class);
        ContentPipelineService pipeline = mock(ContentPipelineService.class);
        when(repository.findDueSources()).thenReturn(List.of(Map.of("sourceId", 2L)));
        when(repository.findImmediateBindings(2L)).thenReturn(List.of(Map.of("accountId", 1L)));
        when(repository.insertOperationRun(any(ContentOperationsMapper.OperationRunRecord.class))).thenReturn(7L, 8L);
        when(sources.scheduledFetch(2L)).thenReturn(new ContentSourceTestVO(2L, 1, 1, null, List.of()));
        when(pipeline.processStoredSource(1L, 2L)).thenReturn(new ContentPipelineTestVO(1L, 2L, 3L, "PUBLISHED", "ok", null, 4L));

        new ContentAutomationScheduler(repository, sources, pipeline, new ObjectMapper()).runDue();

        verify(repository).finishOperationRun(eq(7L), contains("newCount"));
        verify(repository).finishOperationRun(eq(8L), contains("IMMEDIATE"));
        verify(pipeline).processStoredSource(1L, 2L);
        verify(repository).markBindingDispatched(1L, 2L);
    }

    @Test
    void intervalBindingProcessesStoredContentWithoutRefetching() {
        ContentOperationsRepository repository = mock(ContentOperationsRepository.class);
        ContentSourceService sources = mock(ContentSourceService.class);
        ContentPipelineService pipeline = mock(ContentPipelineService.class);
        when(repository.findDueSources()).thenReturn(List.of());
        when(repository.findDueIntervalBindings()).thenReturn(List.of(Map.of("accountId", 3L, "sourceId", 4L)));
        when(repository.insertOperationRun(any(ContentOperationsMapper.OperationRunRecord.class))).thenReturn(9L);
        when(pipeline.processStoredSource(3L, 4L)).thenReturn(new ContentPipelineTestVO(3L, 4L, 5L, "PUBLISHED", "ok", null, 6L));

        new ContentAutomationScheduler(repository, sources, pipeline, new ObjectMapper()).runDue();

        verifyNoInteractions(sources);
        verify(pipeline).processStoredSource(3L, 4L);
        verify(repository).finishOperationRun(eq(9L), contains("INTERVAL"));
        verify(repository).markBindingDispatched(3L, 4L);
    }

    @Test
    void collectionWithNoNewContentDoesNotTriggerImmediatePublishing() {
        ContentOperationsRepository repository = mock(ContentOperationsRepository.class);
        ContentSourceService sources = mock(ContentSourceService.class);
        ContentPipelineService pipeline = mock(ContentPipelineService.class);
        when(repository.findDueSources()).thenReturn(List.of(Map.of("sourceId", 2L)));
        when(repository.insertOperationRun(any(ContentOperationsMapper.OperationRunRecord.class))).thenReturn(7L);
        when(sources.scheduledFetch(2L)).thenReturn(new ContentSourceTestVO(2L, 1, 0, null, List.of()));

        new ContentAutomationScheduler(repository, sources, pipeline, new ObjectMapper()).runDue();

        verify(repository, never()).findImmediateBindings(anyLong());
        verifyNoInteractions(pipeline);
    }
}
