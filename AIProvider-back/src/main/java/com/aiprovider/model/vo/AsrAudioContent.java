package com.aiprovider.model.vo;

import org.springframework.core.io.Resource;

public class AsrAudioContent {
    private final Resource resource;private final String contentType;private final long fileSize;private final String fileName;
    public AsrAudioContent(Resource resource,String contentType,long fileSize,String fileName){this.resource=resource;this.contentType=contentType;this.fileSize=fileSize;this.fileName=fileName;}public Resource getResource(){return resource;}public String getContentType(){return contentType;}public long getFileSize(){return fileSize;}public String getFileName(){return fileName;}
}
