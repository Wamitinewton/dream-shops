package com.newton.dream_shops.exception;

// Login related exceptions
public class LoginFailedException extends AuthenticationServiceException {
    public LoginFailedException(String message) {
        super(message);
    }

    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
