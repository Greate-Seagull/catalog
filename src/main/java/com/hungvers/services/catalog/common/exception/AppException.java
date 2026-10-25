package com.hungvers.services.catalog.common.exception;

public class AppException extends RuntimeException {
    public AppException(Throwable e) {
        super(e);
    }

    public AppException() {
    }
}
