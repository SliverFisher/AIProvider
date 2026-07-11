package com.aiprovider.service;

import com.aiprovider.model.vo.PageResultVO;
import com.aiprovider.repository.TableDataRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TableDataService {

    private final TableDataRepository tableDataRepo;

    public TableDataService(TableDataRepository tableDataRepo) {
        this.tableDataRepo = tableDataRepo;
    }

    public Map<String, Object> getData(String tableName, int page, int size) {
        if (!tableDataRepo.isValidTable(tableName)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }

        long total = tableDataRepo.countByTable(tableName);
        page = Math.max(0, page);
        size = Math.max(1, Math.min(200, size));

        List<Map<String, Object>> rows = tableDataRepo.findPage(tableName, page, size);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("table", tableName);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> getCount(String tableName) {
        if (!tableDataRepo.isValidTable(tableName)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        long count = tableDataRepo.countByTable(tableName);
        return new LinkedHashMap<String, Object>() {{
            put("table", tableName); put("count", count);
        }};
    }
}