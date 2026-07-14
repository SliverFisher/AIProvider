package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.dto.LocalGeneratedImageBatchDTO;
import com.aiprovider.service.LocalGeneratedImageService;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/local-generated-images")
public class LocalGeneratedImageController {
    private final LocalGeneratedImageService service;
    public LocalGeneratedImageController(LocalGeneratedImageService service) { this.service = service; }
    @PostMapping("/batch") public Result<Map<String,Integer>> save(@RequestBody LocalGeneratedImageBatchDTO dto) {
        return Result.success(Collections.singletonMap("saved", service.saveBatch(dto)));
    }
}
