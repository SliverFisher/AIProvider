package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.dto.AssetBatchDTO;
import com.aiprovider.model.dto.AssetDeleteDTO;
import com.aiprovider.model.vo.AssetPageVO;
import com.aiprovider.service.AssetService;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService service;
    public AssetController(AssetService service) { this.service = service; }
    @GetMapping public Result<AssetPageVO> page(@RequestParam String platform, @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "100") int pageSize) {
        return Result.success(service.page(platform, page, pageSize));
    }
    @PostMapping("/batch") public Result<Map<String,Integer>> save(@RequestBody AssetBatchDTO dto) {
        return Result.success(Collections.singletonMap("saved", service.saveBatch(dto)));
    }
    @PostMapping("/delete") public Result<Map<String,Integer>> delete(@RequestBody AssetDeleteDTO dto) {
        return Result.success(Collections.singletonMap("deleted", service.delete(dto)));
    }
}
