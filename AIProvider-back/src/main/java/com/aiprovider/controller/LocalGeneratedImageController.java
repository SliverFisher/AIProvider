package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.dto.LocalGeneratedImageBatchDTO;
import com.aiprovider.model.dto.LocalGeneratedImageIdsDTO;
import com.aiprovider.model.vo.GalleryRecordPageVO;
import com.aiprovider.model.vo.LocalGeneratedImageBatchResultVO;
import com.aiprovider.service.LocalGeneratedImageService;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/local-generated-images")
public class LocalGeneratedImageController {
    private final LocalGeneratedImageService service;
    public LocalGeneratedImageController(LocalGeneratedImageService service) { this.service = service; }
    @GetMapping public Result<GalleryRecordPageVO> page(@RequestParam String platform,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "100") int pageSize,
                                                       @RequestParam(defaultValue = "ACTIVE") String status) {
        return Result.success(service.page(platform, page, pageSize, status));
    }
    @PostMapping("/batch") public Result<LocalGeneratedImageBatchResultVO> save(@RequestBody LocalGeneratedImageBatchDTO dto) {
        return Result.success(service.saveBatch(dto));
    }
    @PostMapping("/trash") public Result<Map<String,Integer>> trash(@RequestBody LocalGeneratedImageIdsDTO dto) {
        return Result.success(Collections.singletonMap("trashed", service.trash(dto)));
    }
    @PostMapping("/restore") public Result<Map<String,Integer>> restore(@RequestBody LocalGeneratedImageIdsDTO dto) {
        return Result.success(Collections.singletonMap("restored", service.restore(dto)));
    }
    @PostMapping("/delete") public Result<Map<String,Integer>> delete(@RequestBody LocalGeneratedImageIdsDTO dto) {
        return Result.success(Collections.singletonMap("deleted", service.delete(dto)));
    }
}
