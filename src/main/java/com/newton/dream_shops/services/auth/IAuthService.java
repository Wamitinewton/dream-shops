package com.newton.dream_shops.services.auth;

import com.newton.dream_shops.dto.auth.*;
import com.newton.dream_shops.models.auth.RefreshToken;
import com.newton.dream_shops.models.auth.User;
import org.springframework.security.core.Authentication;

public interface IAuthService {

    /**
     * Register a new user
     *
     * @param signUpRequest the sign-up request containing user details
     * @return UserInfo containing the registered user's information
     */
    UserInfo signUp(SignUpRequest signUpRequest);

    /**
     * Authenticate user and generate JWT tokens
     *
     * @param loginRequest the login request containing credentials
     * @return JwtResponse containing access token, refresh token and user info
     */
    JwtResponse login(LoginRequest loginRequest);

    /**
     * Refresh access token using refresh token
     *
     * @param refreshTokenRequest the refresh token request
     * @return JwtResponse containing new access token, refresh token and user info
     */
    JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    /**
     * Logout user by revoking refresh token
     *
     * @param refreshToken the refresh token to revoke
     */
    void logout(String refreshToken);

    /**
     * Logout user from all devices by revoking all refresh tokens
     *
     * @param userId the user ID
     */
    void logoutAllDevices(Long userId);

    UserInfo getUserById(Long userId);
    void deleteUser(Long userId);

    void validateSignUpRequest(SignUpRequest request);

    void validateLoginRequest(LoginRequest request);

    void validateRefreshTokenRequest(RefreshTokenRequest request);

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

    void limitActiveTokensPerUser(Long userId);
}
