package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.dto.PromptTranslationDTO;
import com.aiprovider.model.vo.PromptTranslationVO;
import com.aiprovider.service.LibreTranslateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prompt-translations/prose")
public class ProsePromptTranslationController {
    private final LibreTranslateService service;

    public ProsePromptTranslationController(LibreTranslateService service) { this.service = service; }

    @PostMapping
    public Result<PromptTranslationVO> translate(@RequestBody PromptTranslationDTO dto) {
        return Result.success(service.translateToChinese(dto));
    }
}
