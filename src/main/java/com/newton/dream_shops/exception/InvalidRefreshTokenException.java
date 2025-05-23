package com.newton.dream_shops.exception;

// Token related exceptions
public class InvalidRefreshTokenException extends AuthenticationServiceException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
