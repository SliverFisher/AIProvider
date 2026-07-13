package com.aiprovider.model.dto;

public class TwitterClientResultDTO {
    private boolean success;
    private String tweetUrl;
    private String errorMessage;
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getTweetUrl() { return tweetUrl; }
    public void setTweetUrl(String tweetUrl) { this.tweetUrl = tweetUrl; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
