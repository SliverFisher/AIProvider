package com.aiprovider.repository;

import com.aiprovider.mapper.TableMetadataMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TableMetadataRepository {

    private final TableMetadataMapper tableMetadataMapper;

    public TableMetadataRepository(TableMetadataMapper tableMetadataMapper) {
        this.tableMetadataMapper = tableMetadataMapper;
    }

    public List<Map<String, Object>> listTables() {
        return tableMetadataMapper.listTables();
    }

    public List<Map<String, Object>> getColumns(String tableName) {
        return tableMetadataMapper.getColumns(tableName);
    }
}