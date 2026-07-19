package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.vo.FileTransferDownload;
import com.aiprovider.model.vo.FileTransferFileVO;
import com.aiprovider.service.FileTransferService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file-transfer")
public class FileTransferController {
    private final FileTransferService service;

    public FileTransferController(FileTransferService service) { this.service = service; }

    @PostMapping("/upload")
    public Result<FileTransferFileVO> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(service.upload(file));
    }

    @GetMapping("/files")
    public Result<List<FileTransferFileVO>> files() throws IOException {
        return Result.success(service.list());
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String fileName) throws IOException {
        FileTransferDownload download = service.download(fileName);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(download.getFileSize())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(download.getFileName(), StandardCharsets.UTF_8).build().toString())
            .body(download.getResource());
    }

    @DeleteMapping("/{fileName:.+}")
    public Result<Map<String, String>> delete(@PathVariable String fileName) throws IOException {
        service.delete(fileName);
        return Result.success(Collections.singletonMap("deleted", fileName));
    }
}
