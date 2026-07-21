package com.aiprovider.service;

public class AsrTranscriptionException extends RuntimeException {
    private final String code;private final String requestId;
    public AsrTranscriptionException(String code,String requestId){super("语音识别失败");this.code=code;this.requestId=requestId;}public String getCode(){return code;}public String getRequestId(){return requestId;}
}
