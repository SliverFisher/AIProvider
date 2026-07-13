package com.aiprovider.repository;

import com.aiprovider.mapper.ComfyWorkflowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class ComfyWorkflowRepository {
    private final ComfyWorkflowMapper mapper;
    public ComfyWorkflowRepository(ComfyWorkflowMapper mapper) { this.mapper = mapper; }
    public List<Map<String, Object>> findActive() { return mapper.findActive(); }
    public boolean existsActive(String id) { return mapper.countActive(id) > 0; }
}
