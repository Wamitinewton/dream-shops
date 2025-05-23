package com.newton.dream_shops.exception;

public class RefreshTokenExpiredException extends AuthenticationServiceException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
