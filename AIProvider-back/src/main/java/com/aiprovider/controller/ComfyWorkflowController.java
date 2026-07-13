package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.vo.ComfyWorkflowVO;
import com.aiprovider.service.ComfyWorkflowService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comfy-workflows")
public class ComfyWorkflowController {
    private final ComfyWorkflowService service;
    public ComfyWorkflowController(ComfyWorkflowService service) { this.service = service; }
    @GetMapping public Result<List<ComfyWorkflowVO>> list() { return Result.success(service.list()); }
}
