package com.aiprovider.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ComfyWorkflowMapper {
    @Select("SELECT Id id, Name name, Description description, DefinitionJson definitionJson, BindingJson bindingJson, DefaultParametersJson defaultsJson FROM ComfyWorkflows WHERE Active=TRUE ORDER BY CreatedAt")
    List<Map<String, Object>> findActive();

    @Select("SELECT COUNT(*) FROM ComfyWorkflows WHERE Id=#{id} AND Active=TRUE")
    int countActive(String id);
}
