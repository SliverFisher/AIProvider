package com.aiprovider.model.vo;

import java.util.Map;

public class ComfyWorkflowVO {
    private String id;
    private String name;
    private String description;
    private Map<String, Object> definition;
    private Map<String, Object> binding;
    private Map<String, Object> defaults;

    public ComfyWorkflowVO(String id, String name, String description, Map<String, Object> definition,
                           Map<String, Object> binding, Map<String, Object> defaults) {
        this.id = id; this.name = name; this.description = description; this.definition = definition;
        this.binding = binding; this.defaults = defaults;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getDefinition() { return definition; }
    public Map<String, Object> getBinding() { return binding; }
    public Map<String, Object> getDefaults() { return defaults; }
}
