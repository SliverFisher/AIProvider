package com.aiprovider.controller;

import com.aiprovider.model.vo.AsrApiResponse;
import com.aiprovider.service.AsrTranscriptionException;
import org.springframework.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes=AsrTranscriptionController.class)
public class AsrApiExceptionHandler {
    @ExceptionHandler(AsrTranscriptionException.class) public ResponseEntity<AsrApiResponse> transcription(AsrTranscriptionException e){HttpStatus status="ASR_REQUEST_IN_PROGRESS".equals(e.getCode())?HttpStatus.CONFLICT:HttpStatus.BAD_GATEWAY;return ResponseEntity.status(status).body(AsrApiResponse.failure(e.getCode(),e.getMessage(),e.getRequestId()));}
    @ExceptionHandler({IllegalArgumentException.class,SecurityException.class}) public ResponseEntity<AsrApiResponse> badRequest(RuntimeException e){return ResponseEntity.badRequest().body(AsrApiResponse.failure("ASR_INVALID_REQUEST",e.getMessage(),null));}
}
