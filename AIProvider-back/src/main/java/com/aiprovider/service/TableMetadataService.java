package com.aiprovider.service;

import com.aiprovider.repository.TableMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TableMetadataService {

    private final TableMetadataRepository tableMetadataRepo;

    public TableMetadataService(TableMetadataRepository tableMetadataRepo) {
        this.tableMetadataRepo = tableMetadataRepo;
    }

    public List<Map<String, Object>> listTables() {
        return tableMetadataRepo.listTables();
    }

    public List<Map<String, Object>> getColumns(String tableName) {
        return tableMetadataRepo.getColumns(tableName);
    }
}