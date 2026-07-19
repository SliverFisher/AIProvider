package com.aiprovider.model.vo;

import java.time.Instant;

public class FileTransferFileVO {
    private final String fileName;
    private final long fileSize;
    private final Instant uploadedAt;

    public FileTransferFileVO(String fileName, long fileSize, Instant uploadedAt) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public Instant getUploadedAt() { return uploadedAt; }
}
