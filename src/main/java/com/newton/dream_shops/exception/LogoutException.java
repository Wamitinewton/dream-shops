package com.newton.dream_shops.exception;

// Logout related exceptions
public class LogoutException extends AuthenticationServiceException {
    public LogoutException(String message) {
        super(message);
    }

    public LogoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
