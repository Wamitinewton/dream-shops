package com.newton.dream_shops.exception;

// User registration related exceptions
public class UserAlreadyExistsException extends AuthenticationServiceException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
