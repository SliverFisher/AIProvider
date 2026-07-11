package com.aiprovider.repository;

import com.aiprovider.mapper.TableDataMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TableDataRepository {

    private final TableDataMapper tableDataMapper;

    public TableDataRepository(TableDataMapper tableDataMapper) {
        this.tableDataMapper = tableDataMapper;
    }

    public boolean isValidTable(String tableName) {
        return tableName != null && tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    public long countByTable(String tableName) {
        return Optional.ofNullable(tableDataMapper.count(tableName)).orElse(0L);
    }

    public List<Map<String, Object>> findPage(String tableName, int page, int size) {
        return tableDataMapper.findPage(tableName, size, page * size);
    }
}