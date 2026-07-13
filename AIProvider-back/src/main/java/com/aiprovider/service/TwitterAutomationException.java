package com.aiprovider.service;

public class TwitterAutomationException extends RuntimeException {
    private final boolean sessionExpired;

    public TwitterAutomationException(String message) { this(message, false, null); }
    public TwitterAutomationException(String message, Throwable cause) { this(message, false, cause); }
    public TwitterAutomationException(String message, boolean sessionExpired) { this(message, sessionExpired, null); }
    private TwitterAutomationException(String message, boolean sessionExpired, Throwable cause) {
        super(message, cause);
        this.sessionExpired = sessionExpired;
    }
    public boolean isSessionExpired() { return sessionExpired; }
}
