package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.service.TableMetadataService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tables")
public class TableMetadataController {

    private final TableMetadataService tableMetadataService;

    public TableMetadataController(TableMetadataService tableMetadataService) {
        this.tableMetadataService = tableMetadataService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> listTables() {
        return Result.success(tableMetadataService.listTables());
    }

    @GetMapping("/{tableName}/columns")
    public Result<List<Map<String, Object>>> getColumns(@PathVariable String tableName) {
        return Result.success(tableMetadataService.getColumns(tableName));
    }
}