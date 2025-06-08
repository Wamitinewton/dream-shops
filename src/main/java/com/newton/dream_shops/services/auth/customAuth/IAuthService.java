package com.newton.dream_shops.services.auth.customAuth;

import com.newton.dream_shops.dto.auth.*;
import com.newton.dream_shops.models.auth.RefreshToken;
import com.newton.dream_shops.models.auth.User;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

public interface IAuthService {

    UserInfo signUp(SignUpRequest signUpRequest);

    JwtResponse login(LoginRequest loginRequest);

    JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest);


    void verifyEmailOtp(VerifyOtpRequest verifyOtpRequest);

    void resendEmailVerificationOtp(String email);

    void validateSignUpRequest(SignUpRequest request);

    void validateLoginRequest(LoginRequest request);

    void validateRefreshTokenRequest(RefreshTokenRequest request);

    void validateVerifyOtpRequest(VerifyOtpRequest request);

    User createUser(SignUpRequest request);

    RuntimeException handleSignUpException(SignUpRequest request);

    Authentication authenticateUser(LoginRequest request);

    JwtResponse createJwtResponse(User user);

    JwtResponse rotateAndCreateResponse(RefreshToken refreshToken);

    void revokeToken(RefreshToken refreshToken);

    UserInfo mapToUserInfo(User user);

    String generateRefreshToken(User user);

    String rotateRefreshToken(RefreshToken oldRefreshToken);

    void cleanUpExpiredTokens();

    void limitActiveTokensPerUser(HttpServletRequest request);
}
