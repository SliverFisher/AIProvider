package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.service.DataUniverseService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/universe")
public class DataUniverseController {

    private final DataUniverseService dataUniverseService;

    public DataUniverseController(DataUniverseService dataUniverseService) {
        this.dataUniverseService = dataUniverseService;
    }

    @GetMapping
    public Result<Map<String, Object>> index() {
        return Result.success(dataUniverseService.getIndex());
    }

    @GetMapping("/{table}")
    public Result<Map<String, Object>> rows(
            @PathVariable String table,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return Result.success(dataUniverseService.getRows(table, page, size));
    }
}