package com.aiprovider.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TableDataMapper {

    @Select("SELECT COUNT(*) FROM `${tableName}`")
    Long count(@Param("tableName") String tableName);

    @Select("SELECT * FROM `${tableName}` LIMIT #{size} OFFSET #{offset}")
    List<Map<String, Object>> findPage(@Param("tableName") String tableName,
                                       @Param("size") int size,
                                       @Param("offset") int offset);
}