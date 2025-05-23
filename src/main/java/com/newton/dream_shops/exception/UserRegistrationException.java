package com.newton.dream_shops.exception;

public class UserRegistrationException extends AuthenticationServiceException {
    public UserRegistrationException(String message) {
        super(message);
    }

    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
