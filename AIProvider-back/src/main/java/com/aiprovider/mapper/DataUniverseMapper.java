package com.aiprovider.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataUniverseMapper {

    @Select("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT " +
            "FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "ORDER BY TABLE_NAME, ORDINAL_POSITION")
    List<Map<String, Object>> getAllColumns();

    @Select("SELECT COUNT(*) FROM `${tableName}`")
    Long count(@Param("tableName") String tableName);

    @Select("SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT " +
            "FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = #{tableName} " +
            "ORDER BY ORDINAL_POSITION")
    List<Map<String, Object>> getTableColumns(@Param("tableName") String tableName);

    @Select("SELECT ${select} FROM `${table}` ORDER BY ${orderBy} LIMIT #{size} OFFSET #{offset}")
    List<Map<String, Object>> findRows(@Param("table") String table,
                                       @Param("select") String select,
                                       @Param("orderBy") String orderBy,
                                       @Param("size") int size,
                                       @Param("offset") int offset);
}