package com.newton.dream_shops.controller.auth;

import com.newton.dream_shops.dto.auth.*;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.auth.customAuth.IAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            UserInfo userInfo = authService.signUp(signUpRequest);
            return ResponseEntity.ok(new ApiResponse("Successfully signed up", userInfo));
        } catch (CustomException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred. Please try again.", null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.login(loginRequest);
            return ResponseEntity.ok(new ApiResponse("Successfully logged in", jwtResponse));
        } catch (CustomException e) {
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            JwtResponse jwtResponse = authService.refreshToken(refreshTokenRequest);
            return ResponseEntity.ok(new ApiResponse("Successfully refreshed token", jwtResponse));
        } catch (CustomException e) {
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));
        }

    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        try {
            authService.verifyEmailOtp(verifyOtpRequest);
            return ResponseEntity.ok(new ApiResponse("Email verified successfully! Your account is now active", null));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendEmailVerification(@RequestParam String email) {
        try {
            authService.resendEmailVerificationOtp(email);
            return ResponseEntity
                    .ok(new ApiResponse("Verification email sent successfully! Please check your inbox.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

}
