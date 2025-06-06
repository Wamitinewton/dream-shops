package com.newton.dream_shops.util.jwt;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.auth.RefreshToken;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.repository.auth.RefreshTokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtHelperService {

    private static final int MAX_REFRESH_TOKEN_COUNT = 5;

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

   
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new CustomException("Authorization token is required");
    }

   
    public Long getCurrentUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return getCurrentUserIdFromToken(token);
    }

  
    public Long getCurrentUserIdFromToken(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new CustomException("Invalid token or expired");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new CustomException("User ID not found in token");
            }
            return userId;
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new CustomException("Failed to extract user information from token");
        }
    }

    public String getCurrentUsernameFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return jwtUtil.getUsernameFromToken(token);
    }

    /**
     * Get current user email from JWT token in request
     */
    public String getCurrentUserEmailFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return jwtUtil.getEmailFromToken(token);
    }

    /**
     * Validate that the token belongs to the specified user ID
     * This can be used for additional security checks
     */
    public boolean validateTokenOwnership(String token, Long expectedUserId) {
        try {
            Long tokenUserId = getCurrentUserIdFromToken(token);
            return tokenUserId.equals(expectedUserId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate a new refresh token for the user
     */
    public String generateRefreshToken(User user) {
        try {
            String tokenValue = jwtUtil.generateRefreshToken(user.getUsername());
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(tokenValue);
            refreshToken.setUser(user);
            refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getJwtRefreshExpirationMs())));
            refreshTokenRepository.save(refreshToken);
            return tokenValue;
        } catch (Exception e) {
            log.error("Error generating refresh token for user {}: {}", user.getId(), e.getMessage());
            throw new CustomException("Failed to generate refresh token");
        }
    }

    /**
     * Rotate (replace) an existing refresh token with a new one
     */
    public String rotateRefreshToken(RefreshToken oldRefreshToken) {
        try {
            // Revoke the old token
            oldRefreshToken.setRevoked(true);
            refreshTokenRepository.save(oldRefreshToken);

            // Generate new token
            return generateRefreshToken(oldRefreshToken.getUser());
        } catch (Exception e) {
            log.error("Error rotating refresh token: {}", e.getMessage());
            throw new CustomException("Failed to rotate refresh token");
        }
    }

    /**
     * Find a valid (non-revoked, non-expired) refresh token
     */
    public Optional<RefreshToken> findValidRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }

        try {
            return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                    .filter(refreshToken -> !refreshToken.isExpired());
        } catch (Exception e) {
            log.error("Error finding refresh token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Revoke a specific refresh token
     */
    public void revokeRefreshToken(RefreshToken refreshToken) {
        try {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        } catch (Exception e) {
            log.error("Error revoking refresh token: {}", e.getMessage());
            throw new CustomException("Failed to revoke refresh token");
        }
    }

    /**
     * Revoke a refresh token by token string
     */
    public void revokeRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }

        refreshTokenRepository.findByToken(token)
                .ifPresent(this::revokeRefreshToken);
    }

    /**
     * Revoke all refresh tokens for a specific user
     */
    public void revokeAllRefreshTokensByUser(Long userId) {
        try {
            refreshTokenRepository.revokeAllTokensByUser(userId);
        } catch (Exception e) {
            log.error("Error revoking all tokens for user {}: {}", userId, e.getMessage());
            throw new CustomException("Failed to revoke user tokens");
        }
    }

    /**
     * Revoke all refresh tokens for current user from request
     */
    public void revokeAllRefreshTokensForCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserIdFromRequest(request);
        revokeAllRefreshTokensByUser(userId);
    }

    /**
     * Clean up expired refresh tokens from database
     */
    public void cleanUpExpiredTokens() {
        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.debug("Cleaned up expired refresh tokens");
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens: {}", e.getMessage());
        }
    }


    public void limitActiveTokensPerUser(Long userId) {
        try {
            int activeTokenCount = refreshTokenRepository.countActiveTokensByUser(userId, LocalDateTime.now());
            if (activeTokenCount >= MAX_REFRESH_TOKEN_COUNT) {
                log.warn("User {} has {} active tokens, revoking all", userId, activeTokenCount);
                refreshTokenRepository.revokeAllTokensByUser(userId);
            }
        } catch (Exception e) {
            log.error("Error limiting active tokens for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Limit active tokens for current user from request
     */
    public void limitActiveTokensForCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserIdFromRequest(request);
        limitActiveTokensPerUser(userId);
    }

    /**
     * Generate both access and refresh tokens for a user
     * Returns array: [accessToken, refreshToken]
     */
    public String[] generateTokenPair(User user) {
        try {
            // Clean up and limit tokens first
            cleanUpExpiredTokens();
            limitActiveTokensPerUser(user.getId());

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);

            return new String[] { accessToken, refreshToken };
        } catch (Exception e) {
            log.error("Error generating token pair for user {}: {}", user.getId(), e.getMessage());
            throw new CustomException("Failed to generate authentication tokens");
        }
    }

    /**
     * Refresh access token using refresh token
     * Returns new access token
     */
    public String refreshAccessToken(String refreshTokenValue) {
        return findValidRefreshToken(refreshTokenValue)
                .map(RefreshToken::getUser)
                .map(jwtUtil::generateAccessToken)
                .orElseThrow(() -> new CustomException("Invalid or expired refresh token"));
    }

    /**
     * Refresh both access and refresh tokens (token rotation)
     * Returns array: [newAccessToken, newRefreshToken]
     */
    public String[] refreshTokenPair(String refreshTokenValue) {
        return findValidRefreshToken(refreshTokenValue)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();
                    String newAccessToken = jwtUtil.generateAccessToken(user);
                    String newRefreshToken = rotateRefreshToken(refreshToken);
                    return new String[] { newAccessToken, newRefreshToken };
                })
                .orElseThrow(() -> new CustomException("Invalid or expired refresh token"));
    }

    /**
     * Perform complete logout - revoke refresh token and clean up
     */
    public void performLogout(String refreshToken) {
        revokeRefreshToken(refreshToken);
        cleanUpExpiredTokens();
    }

    /**
     * Perform logout for all devices - revoke all tokens for user
     */
    public void performLogoutAllDevices(HttpServletRequest request) {
        Long userId = getCurrentUserIdFromRequest(request);
        revokeAllRefreshTokensByUser(userId);
        cleanUpExpiredTokens();
    }
}