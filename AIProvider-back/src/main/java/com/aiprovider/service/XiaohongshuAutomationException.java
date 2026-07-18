package com.aiprovider.service;

public class XiaohongshuAutomationException extends RuntimeException {
    private final boolean uncertain;
    public XiaohongshuAutomationException(String message){this(message,false);}public XiaohongshuAutomationException(String message,boolean uncertain){super(message);this.uncertain=uncertain;}public XiaohongshuAutomationException(String message,Throwable cause){super(message,cause);this.uncertain=false;}public boolean isUncertain(){return uncertain;}
}
