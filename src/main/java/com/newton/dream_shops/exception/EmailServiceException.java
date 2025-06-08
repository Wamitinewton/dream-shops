package com.newton.dream_shops.exception;

public class EmailServiceException extends RuntimeException {
    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}