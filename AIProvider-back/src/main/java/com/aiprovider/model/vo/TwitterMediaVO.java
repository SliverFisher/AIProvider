package com.aiprovider.model.vo;

public class TwitterMediaVO {
    private final Long id;
    private final Long assetId;
    private final String originalFileName;
    private final String contentType;
    private final Long fileSize;
    private final Integer sortOrder;
    private final String localPath;
    private final String localSource;

    public TwitterMediaVO(Long id, Long assetId, String originalFileName, String contentType, Long fileSize, Integer sortOrder,
                          String localPath, String localSource) {
        this.id = id;
        this.assetId = assetId;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.sortOrder = sortOrder;
        this.localPath = localPath;
        this.localSource = localSource;
    }
    public Long getId() { return id; }
    public Long getAssetId() { return assetId; }
    public String getOriginalFileName() { return originalFileName; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public Integer getSortOrder() { return sortOrder; }
    public String getLocalPath() { return localPath; }
    public String getLocalSource() { return localSource; }
}
