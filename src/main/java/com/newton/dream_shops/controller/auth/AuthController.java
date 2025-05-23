package com.newton.dream_shops.controller.auth;

import com.newton.dream_shops.dto.auth.*;
import com.newton.dream_shops.exception.*;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            UserInfo userInfo = authService.signUp(signUpRequest);
            return ResponseEntity.ok(new ApiResponse("Successfully signed up", userInfo));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (UserRegistrationException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
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
        } catch (LoginFailedException e) {
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            JwtResponse jwtResponse = authService.refreshToken(refreshTokenRequest);
            return ResponseEntity.ok(new ApiResponse("Successfully refreshed token", jwtResponse));
        } catch (InvalidRefreshTokenException | RefreshTokenExpiredException e) {
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok(new ApiResponse("Logout successful", null));
        } catch (LogoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}
