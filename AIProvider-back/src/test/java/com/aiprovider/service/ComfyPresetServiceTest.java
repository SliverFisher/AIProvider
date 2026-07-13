package com.aiprovider.service;

import com.aiprovider.mapper.ComfyPresetMapper;
import com.aiprovider.model.dto.ComfyPresetDTO;
import com.aiprovider.model.vo.ComfyPresetVO;
import com.aiprovider.repository.ComfyPresetRepository;
import com.aiprovider.repository.ComfyWorkflowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ComfyPresetServiceTest {
    private final ComfyPresetRepository presets = mock(ComfyPresetRepository.class);
    private final ComfyWorkflowRepository workflows = mock(ComfyWorkflowRepository.class);
    private final ComfyPresetService service = new ComfyPresetService(presets, workflows, new ObjectMapper());

    private ComfyPresetDTO valid() {
        ComfyPresetDTO dto = new ComfyPresetDTO();
        dto.setTitle(" Portrait "); dto.setWorkflowId("futa01"); dto.setOutputFolder(" output ");
        dto.setParameters(Collections.singletonMap("width", 1080));
        when(workflows.existsActive("futa01")).thenReturn(true);
        return dto;
    }

    @Test void createsACompleteScheme() {
        when(presets.insert(any())).thenAnswer(invocation -> {
            ComfyPresetMapper.PresetInsert value = invocation.getArgument(0); value.setId(9L); return 9L;
        });
        assertThat(service.create(valid())).isEqualTo(9L);
        verify(presets).insert(argThat(value -> value.getTitle().equals("Portrait") &&
                value.getWorkflowId().equals("futa01") && value.getOutputFolder().equals("output") &&
                value.getParametersJson().contains("1080")));
    }

    @Test void defaultsAnEmptyOutputFolder() {
        ComfyPresetDTO dto = valid(); dto.setOutputFolder("  ");
        service.create(dto);
        dto = valid(); dto.setOutputFolder(null);
        service.create(dto);
        verify(presets, times(2)).insert(argThat(value -> value.getOutputFolder().equals("aimaid")));
    }

    @Test void listsTypedSchemesForNumericAndTextIds() {
        Map<String, Object> first = new HashMap<>();
        first.put("id", 7L); first.put("title", "A"); first.put("workflowId", "futa01"); first.put("outputFolder", "a"); first.put("parametersJson", "{\"steps\":30}");
        Map<String, Object> second = new HashMap<>(first); second.put("id", "8"); second.put("title", null);
        when(presets.findAll()).thenReturn(Arrays.asList(first, second));
        List<ComfyPresetVO> result = service.list();
        assertThat(result).extracting(ComfyPresetVO::getId).containsExactly(7L, 8L);
        assertThat(result.get(0).getParameters()).containsEntry("steps", 30);
        assertThat(result.get(1).getTitle()).isNull();
    }

    @Test void rejectsCorruptStoredJson() {
        Map<String, Object> row = new HashMap<>(); row.put("id", 1); row.put("parametersJson", "bad");
        when(presets.findAll()).thenReturn(Collections.singletonList(row));
        assertThatThrownBy(service::list).isInstanceOf(IllegalStateException.class).hasMessageContaining("参数方案 JSON");
    }

    @Test void deletesExistingSchemeAndRejectsMissingOne() {
        when(presets.delete(3)).thenReturn(true); when(presets.delete(4)).thenReturn(false);
        service.delete(3);
        assertThatThrownBy(() -> service.delete(4)).isInstanceOf(IllegalArgumentException.class).hasMessage("参数方案不存在");
    }

    @Test void updatesAndMarksAnExistingSchemeAsDefault() {
        when(presets.update(any())).thenReturn(true);
        service.update(3, valid());
        verify(presets).update(argThat(value -> value.getId() == 3 && value.getTitle().equals("Portrait")));
        when(presets.setDefault(3)).thenReturn(true);
        service.setDefault(3);
        verify(presets).clearDefault();
        verify(presets).setDefault(3);
    }

    @Test void acceptsLocalWorkflowIdentifiers() {
        ComfyPresetDTO dto = valid(); dto.setWorkflowId("local-1234abcd");
        service.create(dto);
        verify(presets).insert(argThat(value -> value.getWorkflowId().equals("local-1234abcd")));
    }

    @Test void validatesAllRequiredFieldsAndFolderBoundaries() {
        assertInvalid(null, "参数方案不能为空");
        ComfyPresetDTO dto = valid(); dto.setTitle(null); assertInvalid(dto, "标题长度");
        dto = valid(); dto.setTitle(" "); assertInvalid(dto, "标题长度");
        dto = valid(); dto.setTitle(String.join("", Collections.nCopies(101, "x"))); assertInvalid(dto, "标题长度");
        dto = valid(); dto.setWorkflowId(null); assertInvalid(dto, "工作流不存在");
        dto = valid(); when(workflows.existsActive("missing")).thenReturn(false); dto.setWorkflowId("missing"); assertInvalid(dto, "工作流不存在");
        dto = valid(); dto.setParameters(null); assertInvalid(dto, "参数不能为空");
        dto = valid(); dto.setParameters(Collections.emptyMap()); assertInvalid(dto, "参数不能为空");
        for (String folder : Arrays.asList("../x", "/root", "\\root", String.join("", Collections.nCopies(241, "x")))) {
            dto = valid(); dto.setOutputFolder(folder); assertInvalid(dto, "输出文件夹不合法");
        }
    }

    @Test void rejectsParametersThatCannotBeSerialized() {
        ComfyPresetDTO dto = valid(); Map<String, Object> invalid = new HashMap<>(); invalid.put("empty", new EmptyValue()); dto.setParameters(invalid);
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("有效 JSON");
    }

    private static class EmptyValue { }

    private void assertInvalid(ComfyPresetDTO dto, String message) {
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(message);
    }
}
