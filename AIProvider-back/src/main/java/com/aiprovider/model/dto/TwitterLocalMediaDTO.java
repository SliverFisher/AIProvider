package com.aiprovider.model.dto;

public class TwitterLocalMediaDTO {
    private String path;
    private String source;
    private String fileName;
    private String contentType;
    private Long fileSize;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
