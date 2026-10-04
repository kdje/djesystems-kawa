package com.djesystems.kawa.retailer.application;

import org.springframework.http.HttpStatus;

public class RetailerPortalException extends RuntimeException {

    private final HttpStatus status;

    public RetailerPortalException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
