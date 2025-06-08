package com.newton.dream_shops.services.auth.customAuth;

import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.newton.dream_shops.dto.auth.JwtResponse;
import com.newton.dream_shops.dto.auth.LoginRequest;
import com.newton.dream_shops.dto.auth.RefreshTokenRequest;
import com.newton.dream_shops.dto.auth.SignUpRequest;
import com.newton.dream_shops.dto.auth.UserInfo;
import com.newton.dream_shops.dto.auth.VerifyOtpRequest;
import com.newton.dream_shops.exception.AlreadyExistsException;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.auth.RefreshToken;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.repository.auth.RefreshTokenRepository;
import com.newton.dream_shops.repository.auth.UserRepository;
import com.newton.dream_shops.services.auth.otp.IOtpService;
import com.newton.dream_shops.services.email.IEmailService;
import com.newton.dream_shops.util.jwt.JwtHelperService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final JwtHelperService jwtHelperService;
    private IOtpService otpService;
    private final IEmailService emailService;

    @Override
    @Transactional
    public UserInfo signUp(SignUpRequest signUpRequest) {
        validateSignUpRequest(signUpRequest);

        return Optional.of(signUpRequest)
                .filter(request -> !userRepository.existsByUsername(request.getUsername()))
                .filter(request -> !userRepository.existsByEmail(request.getEmail()))
                .map(this::createUser)
                .map(userRepository::save)
                .map(user -> {
                    try {
                        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
                    } catch (Exception e) {
                        log.warn("Failed to send welcome email to user");
                    }

                    try {
                        otpService.generateAndSendEmailVerificationOtp(user);
                    } catch (Exception e) {
                        log.error("Failed to send email verification otp to user", e);
                    }

                    return user;
                })
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
                    if (!user.isEnabled()) {
                        throw new CustomException("Account is disabled. Please verify your email first");
                    }

                    if (!user.getEmailVerified()) {
                        throw new CustomException("Email not verified. Please check your email for verification");
                    }

                    jwtHelperService.cleanUpExpiredTokens();
                    jwtHelperService.limitActiveTokensPerUser(user.getId());
                    return createJwtResponse(user);
                })
                .orElseThrow(() -> new CustomException("Authentication failed"));
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        validateRefreshTokenRequest(refreshTokenRequest);

        return jwtHelperService.findValidRefreshToken(refreshTokenRequest.getRefreshToken())
                .map(this::rotateAndCreateResponse)
                .orElseThrow(() -> new CustomException("Invalid or expired refresh token"));
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
    public void validateVerifyOtpRequest(VerifyOtpRequest request) {
        Optional.ofNullable(request)
                .filter(r -> StringUtils.hasText(r.getEmail()))
                .filter(r -> StringUtils.hasText(r.getOtp()))
                .orElseThrow(() -> new IllegalArgumentException("Email and OTP are required"));
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
            user.setEnabled(false);
            user.setEmailVerified(false);
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
                            request.getPassword()));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public JwtResponse createJwtResponse(User user) {
        String[] tokens = jwtHelperService.generateTokenPair(user);
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

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

        // Use JwtHelperService to refresh token pair with rotation
        String[] tokens = jwtHelperService.refreshTokenPair(refreshToken.getToken());
        String accessToken = tokens[0];
        String newRefreshToken = tokens[1];

        UserInfo userInfo = mapToUserInfo(user);

        return new JwtResponse(accessToken, newRefreshToken, userInfo);
    }

    @Override
    public void revokeToken(RefreshToken refreshToken) {
        jwtHelperService.revokeRefreshToken(refreshToken);
    }

    @Override
    public UserInfo mapToUserInfo(User user) {
        UserInfo userInfo = modelMapper.map(user, UserInfo.class);

        return userInfo;
    }

    @Override
    public String generateRefreshToken(User user) {
        return jwtHelperService.generateRefreshToken(user);
    }

    @Override
    public String rotateRefreshToken(RefreshToken oldRefreshToken) {
        return jwtHelperService.rotateRefreshToken(oldRefreshToken);
    }

    @Override
    public void cleanUpExpiredTokens() {
        jwtHelperService.cleanUpExpiredTokens();
    }

    public void limitActiveTokensPerUser(Long userId) {
        jwtHelperService.limitActiveTokensPerUser(userId);
    }

    @Override
    @Transactional
    public void limitActiveTokensPerUser(HttpServletRequest request) {
        jwtHelperService.limitActiveTokensForCurrentUser(request);
    }

    @Override
    @Transactional
    public void verifyEmailOtp(VerifyOtpRequest verifyOtpRequest) {
        validateVerifyOtpRequest(verifyOtpRequest);

        try {
            otpService.activateUserAccount(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
            log.info("Email verification successful for user: {}", verifyOtpRequest.getEmail());
        } catch (Exception e) {
            log.error("Email verification failed for user: {}", verifyOtpRequest.getEmail(), e);
            throw new CustomException("Email verification failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resendEmailVerificationOtp(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }

        try {
            otpService.resendEmailVerificationOtp(email);
            log.info("Email verification OTP resent to user: {}", email);
        } catch (Exception e) {
            log.error("Failed to resend email verification OTP to user: {}", email, e);
            throw new CustomException("Failed to resend verification email: " + e.getMessage());
        }
    }

}