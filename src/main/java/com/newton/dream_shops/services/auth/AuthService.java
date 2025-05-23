package com.newton.dream_shops.services.auth;

import com.newton.dream_shops.dto.auth.*;
import com.newton.dream_shops.exception.*;
import com.newton.dream_shops.models.auth.RefreshToken;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.repository.auth.RefreshTokenRepository;
import com.newton.dream_shops.repository.auth.UserRepository;
import com.newton.dream_shops.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private static final int MAX_REFRESH_TOKEN_COUNT = 5;

    @Transactional
    public UserInfo signUp(SignUpRequest signUpRequest) {
        try {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                throw new UserAlreadyExistsException("Username already exists");
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists");
            }

            User user = new User();
            user.setFirstName(signUpRequest.getFirstName());
            user.setLastName(signUpRequest.getLastName());
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

            User savedUser = userRepository.save(user);
            return new UserInfo(
                    savedUser.getId(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getUsername(),
                    savedUser.getEmail()
            );
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation during signup: {}", e.getMessage());
            throw new UserAlreadyExistsException("Username or email already exists!");
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage());
            throw new UserRegistrationException("Registration failed. Please try again.", e);
        }
    }

    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );
            User user = (User) authentication.getPrincipal();

            cleanUpExpiredTokens();
            limitActiveTokensPerUser(user.getId());

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);

            UserInfo userInfo = new UserInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUsername(),
                    user.getEmail());

            return new JwtResponse(accessToken, refreshToken, jwtUtil.getJwtExpirationMs(), userInfo);
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new LoginFailedException("Invalid username/email or password", e);
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage());
            throw new LoginFailedException("Login failed. Please try again.", e);
        }
    }

    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshTokenValue = refreshTokenRequest.getRefreshToken();
            RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                    .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

            if (refreshToken.isExpired()) {
                refreshTokenRepository.delete(refreshToken);
                throw new RefreshTokenExpiredException("Refresh token expired");
            }

            User user = refreshToken.getUser();
            String accessToken = jwtUtil.generateAccessToken(user);
            String newRefreshToken = rotateRefreshToken(refreshToken);

            UserInfo userInfo = new UserInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUsername(),
                    user.getEmail()
            );
            return new JwtResponse(accessToken, newRefreshToken, jwtUtil.getJwtExpirationMs(), userInfo);
        } catch (InvalidRefreshTokenException | RefreshTokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage());
            throw new InvalidRefreshTokenException("Token refresh failed. Please login again.");
        }
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        try {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                    .orElse(null);

            if (refreshToken != null) {
                refreshToken.setRevoked(true);
                refreshTokenRepository.save(refreshToken);
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw new LogoutException("Logout failed. Please try again.", e);
        }
    }

    private String generateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getJwtRefreshExpirationMs())));
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private String rotateRefreshToken(RefreshToken oldRefreshToken) {
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);
        return generateRefreshToken(oldRefreshToken.getUser());
    }

    private void cleanUpExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private void limitActiveTokensPerUser(Long userId) {
        int activeTokenCount = refreshTokenRepository.countActiveTokensByUser(userId, LocalDateTime.now());

        if (activeTokenCount >= MAX_REFRESH_TOKEN_COUNT) {
            refreshTokenRepository.revokeAllTokensByUser(userId);
        }
    }
}