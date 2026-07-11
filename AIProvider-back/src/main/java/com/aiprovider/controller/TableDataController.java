package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.service.TableDataService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/data")
public class TableDataController {

    private final TableDataService tableDataService;

    public TableDataController(TableDataService tableDataService) {
        this.tableDataService = tableDataService;
    }

    @GetMapping("/{tableName}")
    public Result<Map<String, Object>> getData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Result.success(tableDataService.getData(tableName, page, size));
    }

    @GetMapping("/{tableName}/count")
    public Result<Map<String, Object>> getCount(@PathVariable String tableName) {
        return Result.success(tableDataService.getCount(tableName));
    }
}