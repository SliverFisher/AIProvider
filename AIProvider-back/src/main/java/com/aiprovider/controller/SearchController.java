package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(searchService.search(q, limit));
    }
}