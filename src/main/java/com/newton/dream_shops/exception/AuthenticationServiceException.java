package com.newton.dream_shops.exception;

// Custom Authentication Exceptions

// Base exception for authentication-related errors
public class AuthenticationServiceException extends RuntimeException {
    public AuthenticationServiceException(String message) {
        super(message);
    }

    public AuthenticationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

