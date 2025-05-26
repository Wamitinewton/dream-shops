package com.newton.dream_shops.services.auth;

import com.newton.dream_shops.dto.auth.*;
import com.newton.dream_shops.exception.AlreadyExistsException;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.auth.RefreshToken;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.repository.auth.RefreshTokenRepository;
import com.newton.dream_shops.repository.auth.UserRepository;
import com.newton.dream_shops.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    private static final int MAX_REFRESH_TOKEN_COUNT = 5;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserInfo signUp(SignUpRequest signUpRequest) {
        validateSignUpRequest(signUpRequest);

        return Optional.of(signUpRequest)
                .filter(request -> !userRepository.existsByUsername(request.getUsername()))
                .filter(request -> !userRepository.existsByEmail(request.getEmail()))
                .map(this::createUser)
                .map(userRepository::save)
                .map(this::mapToUserInfo)
                .orElseThrow(() -> handleSignUpException(signUpRequest));
    }

    @Override
    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        validateLoginRequest(loginRequest);

        return Optional.of(loginRequest)
                .map(this::authenticateUser)
                .map(auth -> (User) auth.getPrincipal())
                .map(user -> {
                    cleanUpExpiredTokens();
                    limitActiveTokensPerUser(user.getId());
                    return createJwtResponse(user);
                })
                .orElseThrow(() -> new CustomException("Authentication failed"));
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        validateRefreshTokenRequest(refreshTokenRequest);

        return Optional.of(refreshTokenRequest.getRefreshToken())
                .flatMap(token -> refreshTokenRepository.findByTokenAndRevokedFalse(token))
                .filter(token -> !token.isExpired())
                .map(this::rotateAndCreateResponse)
                .orElseThrow(() -> new CustomException("Invalid or expired refresh token"));
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        Optional.ofNullable(refreshToken)
                .filter(StringUtils::hasText)
                .flatMap(refreshTokenRepository::findByToken)
                .ifPresent(this::revokeToken);
    }

    @Override
    @Transactional
    public void logoutAllDevices(Long userId) {
        Optional.ofNullable(userId)
                .ifPresentOrElse(
                        refreshTokenRepository::revokeAllTokensByUser,
                        () -> {
                            throw new IllegalArgumentException("User ID cannot be null");
                        }
                );
    }

    @Override
    @Transactional
    public UserInfo getUserById(Long userId) {
        return userRepository.findById(userId)
        .map(this :: mapToUserInfo)
        .orElseThrow(() -> new CustomException("User with " + userId + " Not found"));
    }

    @Override
    public void validateSignUpRequest(SignUpRequest request) {
        Optional.ofNullable(request)
                .filter(r -> StringUtils.hasText(r.getUsername()))
                .filter(r -> StringUtils.hasText(r.getEmail()))
                .filter(r -> StringUtils.hasText(r.getPassword()))
                .filter(r -> StringUtils.hasText(r.getFirstName()))
                .filter(r -> StringUtils.hasText(r.getLastName()))
                .orElseThrow(() -> new IllegalArgumentException("All fields are required"));
    }

    @Override
    public void validateLoginRequest(LoginRequest request) {
        Optional.ofNullable(request)
                .filter(r -> StringUtils.hasText(r.getUsernameOrEmail()))
                .filter(r -> StringUtils.hasText(r.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Username/email and password are required"));
    }

    @Override
    public void validateRefreshTokenRequest(RefreshTokenRequest request) {
        Optional.ofNullable(request)
                .filter(r -> StringUtils.hasText(r.getRefreshToken()))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is required"));
    }

    @Override
    public User createUser(SignUpRequest request) {
        try {
            User user = new User();
            user.setFirstName(request.getFirstName().trim());
            user.setLastName(request.getLastName().trim());
            user.setUsername(request.getUsername().trim().toLowerCase());
            user.setEmail(request.getEmail().trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            return user;
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException("Username or email already exists");
        }
    }

    @Override
    public RuntimeException handleSignUpException(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new CustomException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return new CustomException("Email already exists");
        }
        return new CustomException("Registration failed");
    }

    @Override
    public Authentication authenticateUser(LoginRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public JwtResponse createJwtResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        UserInfo userInfo = mapToUserInfo(user);

        return new JwtResponse(accessToken, refreshToken, userInfo);
    }

    @Override
    public JwtResponse rotateAndCreateResponse(RefreshToken refreshToken) {
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = rotateRefreshToken(refreshToken);
        UserInfo userInfo = mapToUserInfo(user);

        return new JwtResponse(accessToken, newRefreshToken, userInfo);
    }

    @Override
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public UserInfo mapToUserInfo(User user) {
        return modelMapper.map(user, UserInfo.class);
    }

    @Override
    public String generateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getJwtRefreshExpirationMs())));
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    @Override
    public String rotateRefreshToken(RefreshToken oldRefreshToken) {
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);
        return generateRefreshToken(oldRefreshToken.getUser());
    }

    @Override
    public void cleanUpExpiredTokens() {
        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens: {}", e.getMessage());
        }
    }

    @Override
    public void limitActiveTokensPerUser(Long userId) {
        try {
            int activeTokenCount = refreshTokenRepository.countActiveTokensByUser(userId, LocalDateTime.now());
            if (activeTokenCount >= MAX_REFRESH_TOKEN_COUNT) {
                refreshTokenRepository.revokeAllTokensByUser(userId);
            }
        } catch (Exception e) {
            log.error("Error limiting active tokens for user {}: {}", userId, e.getMessage());
        }
    }
}