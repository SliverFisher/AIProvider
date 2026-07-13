package com.aiprovider.service;

import com.aiprovider.model.vo.ComfyWorkflowVO;
import com.aiprovider.repository.ComfyWorkflowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComfyWorkflowServiceTest {
    private final ComfyWorkflowRepository repository = mock(ComfyWorkflowRepository.class);
    private final ComfyWorkflowService service = new ComfyWorkflowService(repository, new ObjectMapper());

    @Test void mapsEveryDatabaseJsonField() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", "futa01"); row.put("name", "Futa 01"); row.put("description", null);
        row.put("definitionJson", "{\"node\":{}}"); row.put("bindingJson", "{\"fields\":{}}"); row.put("defaultsJson", "{\"width\":1080}");
        when(repository.findActive()).thenReturn(Collections.singletonList(row));
        List<ComfyWorkflowVO> result = service.list();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("futa01");
        assertThat(result.get(0).getName()).isEqualTo("Futa 01");
        assertThat(result.get(0).getDescription()).isNull();
        assertThat(result.get(0).getDefinition()).containsKey("node");
        assertThat(result.get(0).getBinding()).containsKey("fields");
        assertThat(result.get(0).getDefaults()).containsEntry("width", 1080);
    }

    @Test void rejectsCorruptWorkflowJson() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", "futa01"); row.put("definitionJson", "not-json");
        row.put("bindingJson", "{}"); row.put("defaultsJson", "{}");
        when(repository.findActive()).thenReturn(Collections.singletonList(row));
        assertThatThrownBy(service::list).isInstanceOf(IllegalStateException.class).hasMessageContaining("工作流 JSON");
    }
}
