package com.aiprovider.service;

public class AsrProviderException extends RuntimeException {
    private final String code;
    public AsrProviderException(String code,String message,Throwable cause){super(message,cause);this.code=code;}public String getCode(){return code;}
}
