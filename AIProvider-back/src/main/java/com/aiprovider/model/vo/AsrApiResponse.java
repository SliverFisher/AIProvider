package com.aiprovider.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsrApiResponse {
    private final boolean success;private final AsrRecordVO data;private final ErrorBody error;
    private AsrApiResponse(boolean success,AsrRecordVO data,ErrorBody error){this.success=success;this.data=data;this.error=error;}
    public static AsrApiResponse success(AsrRecordVO data){return new AsrApiResponse(true,data,null);}
    public static AsrApiResponse failure(String code,String message,String requestId){return new AsrApiResponse(false,null,new ErrorBody(code,message,requestId));}
    public boolean isSuccess(){return success;}public AsrRecordVO getData(){return data;}public ErrorBody getError(){return error;}
    public static class ErrorBody {private final String code;private final String message;private final String requestId;public ErrorBody(String code,String message,String requestId){this.code=code;this.message=message;this.requestId=requestId;}public String getCode(){return code;}public String getMessage(){return message;}public String getRequestId(){return requestId;}}
}
