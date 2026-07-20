package com.aiprovider.model.vo;

public class PlatformLoginSessionVO {
    private final String sessionId;private final String status;private final String qrImageDataUrl;private final String message;
    public PlatformLoginSessionVO(String sessionId,String status,String qrImageDataUrl,String message){this.sessionId=sessionId;this.status=status;this.qrImageDataUrl=qrImageDataUrl;this.message=message;}
    public String getSessionId(){return sessionId;}public String getStatus(){return status;}public String getQrImageDataUrl(){return qrImageDataUrl;}public String getMessage(){return message;}
}
