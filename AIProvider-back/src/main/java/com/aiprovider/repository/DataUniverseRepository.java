package com.aiprovider.repository;

import com.aiprovider.mapper.DataUniverseMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DataUniverseRepository {

    private final DataUniverseMapper dataUniverseMapper;

    public DataUniverseRepository(DataUniverseMapper dataUniverseMapper) {
        this.dataUniverseMapper = dataUniverseMapper;
    }

    public List<Map<String, Object>> getAllColumns() {
        return dataUniverseMapper.getAllColumns();
    }

    public long countByTable(String table) {
        return Optional.ofNullable(dataUniverseMapper.count(table)).orElse(0L);
    }

    public List<Map<String, Object>> getTableColumns(String table) {
        return dataUniverseMapper.getTableColumns(table);
    }

    public List<Map<String, Object>> findRows(String table, String select, String orderBy,
                                               int size, int offset) {
        return dataUniverseMapper.findRows(table, select, orderBy, size, offset);
    }

    public long countTable(String table) {
        return Optional.ofNullable(dataUniverseMapper.count(table)).orElse(0L);
    }

    public String safeColumnExpression(Map<String, Object> column) {
        String name = String.valueOf(column.get("COLUMN_NAME"));
        String type = String.valueOf(column.get("DATA_TYPE")).toLowerCase(Locale.ROOT);
        if (type.contains("blob") || type.contains("binary"))
            return "CASE WHEN `" + name + "` IS NULL THEN NULL ELSE CONCAT('[binary ', OCTET_LENGTH(`" + name + "`), ' bytes]') END AS `" + name + "`";
        if (type.contains("text") || type.equals("json"))
            return "LEFT(`" + name + "`, 1200) AS `" + name + "`";
        return "`" + name + "`";
    }
}