package com.verifyhub.common.web;

import com.verifyhub.common.exception.ErrorCode;
import com.verifyhub.common.exception.VerifyhubException;
import com.verifyhub.common.response.ErrorResponse;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID = "traceId";

    @ExceptionHandler(VerifyhubException.class)
    public ResponseEntity<ErrorResponse> handleVerifyhubException(VerifyhubException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, exception.getMessage(), traceId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, message, traceId(request)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessageException(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getMessage(), traceId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage(), traceId(request)));
    }

    private String traceId(HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        Object requestTraceId = request.getAttribute(TRACE_ID);
        if (requestTraceId instanceof String && !((String) requestTraceId).isBlank()) {
            return (String) requestTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
