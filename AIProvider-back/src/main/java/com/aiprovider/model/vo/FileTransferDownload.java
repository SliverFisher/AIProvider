package com.aiprovider.model.vo;

import org.springframework.core.io.Resource;

public class FileTransferDownload {
    private final String fileName;
    private final long fileSize;
    private final Resource resource;

    public FileTransferDownload(String fileName, long fileSize, Resource resource) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.resource = resource;
    }

    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public Resource getResource() { return resource; }
}
