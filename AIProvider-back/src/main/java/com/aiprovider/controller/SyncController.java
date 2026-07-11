package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.dto.SyncBatchDTO;
import com.aiprovider.model.vo.SyncResultVO;
import com.aiprovider.service.SyncService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/business-batch")
    public Result<SyncResultVO> businessBatch(@RequestBody SyncBatchDTO batch) {
        return Result.success(syncService.processBusinessBatch(
            batch.getDeviceId(), batch.getRecords()));
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        return Result.success(syncService.getStatus());
    }
}