package com.aiprovider.service;

public class PromptTranslationException extends RuntimeException {
    public PromptTranslationException(String message) { super(message); }
    public PromptTranslationException(String message, Throwable cause) { super(message, cause); }
}
