package com.aiprovider.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TableMetadataMapper {

    @Select("SELECT TABLE_NAME, TABLE_ROWS, TABLE_COMMENT " +
            "FROM information_schema.TABLES " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "ORDER BY TABLE_NAME")
    List<Map<String, Object>> listTables();

    @Select("SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT, ORDINAL_POSITION " +
            "FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = #{tableName} " +
            "ORDER BY ORDINAL_POSITION")
    List<Map<String, Object>> getColumns(@Param("tableName") String tableName);
}