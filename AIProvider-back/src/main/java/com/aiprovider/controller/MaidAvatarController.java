package com.aiprovider.controller;

import com.aiprovider.service.MaidAvatarService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

@RestController
@RequestMapping("/api/maid/avatars")
public class MaidAvatarController {

    private final MaidAvatarService avatarService;

    public MaidAvatarController(MaidAvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<Resource> avatar(@PathVariable String roleId) throws IOException {
        Resource resource = avatarService.find(roleId);
        if (resource == null || !resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色头像不存在");
        }
        String detected = Files.probeContentType(resource.getFile().toPath());
        MediaType mediaType = detected == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(detected);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .contentType(mediaType)
                .body(resource);
    }
}
