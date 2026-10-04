package com.djesystems.kawa.retailer.api;

import com.djesystems.kawa.retailer.application.RetailerPortalException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RetailerPortalController.class)
public class RetailerPortalExceptionHandler {

    @ExceptionHandler(RetailerPortalException.class)
    public ResponseEntity<PortalErrorResponse> handlePortalException(
            RetailerPortalException exception) {
        String code = exception.getStatus().is4xxClientError()
            ? exception.getStatus().name().toLowerCase()
            : "portal_error";
        return ResponseEntity.status(exception.getStatus())
            .body(new PortalErrorResponse(code, exception.getMessage()));
    }
}
