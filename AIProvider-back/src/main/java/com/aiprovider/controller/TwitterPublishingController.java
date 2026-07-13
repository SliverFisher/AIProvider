package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.dto.TwitterAccountConnectDTO;
import com.aiprovider.model.dto.TwitterPostCreateDTO;
import com.aiprovider.model.dto.TwitterClientAccountDTO;
import com.aiprovider.model.dto.TwitterClientResultDTO;
import com.aiprovider.model.vo.TwitterAccountVO;
import com.aiprovider.model.vo.TwitterPostVO;
import com.aiprovider.service.TwitterPublishingService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/twitter")
public class TwitterPublishingController {
    private final TwitterPublishingService service;

    public TwitterPublishingController(TwitterPublishingService service) { this.service = service; }

    @PostMapping("/accounts/connect")
    public Result<TwitterAccountVO> connect(@RequestBody TwitterAccountConnectDTO dto) {
        return Result.success(service.connect(dto));
    }

    @GetMapping("/accounts")
    public Result<List<TwitterAccountVO>> accounts() { return Result.success(service.listAccounts()); }

    @PostMapping("/accounts/client-status")
    public Result<TwitterAccountVO> clientAccount(@RequestBody TwitterClientAccountDTO dto) {
        return Result.success(service.registerClientAccount(dto));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Long>> create(@ModelAttribute TwitterPostCreateDTO dto) {
        return Result.success(Collections.singletonMap("id", service.createPost(dto)));
    }

    @PostMapping(value = "/posts/local-scheduled", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Long>> createLocalScheduled(@ModelAttribute TwitterPostCreateDTO dto) {
        return Result.success(Collections.singletonMap("id", service.createPost(dto)));
    }

    @GetMapping("/posts")
    public Result<List<TwitterPostVO>> posts(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(service.listPosts(limit));
    }

    @GetMapping("/posts/pending")
    public Result<List<TwitterPostVO>> pending(@RequestParam long accountId,
                                               @RequestParam(defaultValue = "10") int limit) {
        return Result.success(service.pendingPosts(accountId, limit));
    }

    @PostMapping("/posts/{id}/claim")
    public Result<TwitterPostVO> claim(@PathVariable long id) { return Result.success(service.claimForClient(id)); }

    @PostMapping("/posts/{id}/client-result")
    public Result<Void> clientResult(@PathVariable long id, @RequestBody TwitterClientResultDTO dto) {
        service.completeFromClient(id, dto); return Result.success();
    }

    @GetMapping("/posts/{id}")
    public Result<TwitterPostVO> post(@PathVariable long id) { return Result.success(service.getPost(id)); }

    @PostMapping("/posts/{id}/retry")
    public Result<Void> retry(@PathVariable long id) { service.retry(id); return Result.success(); }

    @PostMapping("/posts/{id}/cancel")
    public Result<Void> cancel(@PathVariable long id) { service.cancel(id); return Result.success(); }

    @GetMapping("/posts/{postId}/images/{imageId}")
    public ResponseEntity<Resource> image(@PathVariable long postId, @PathVariable long imageId) {
        Resource resource = service.getImage(postId, imageId);
        MediaType type;
        try { type = MediaType.parseMediaType(service.getImageContentType(postId, imageId)); }
        catch (IllegalArgumentException e) { type = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.noCache()).body(resource);
    }
}
