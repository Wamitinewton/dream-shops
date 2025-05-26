package com.newton.dream_shops.controller.auth;

import com.newton.dream_shops.dto.auth.*;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.auth.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok(new ApiResponse("Logout successful", null));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<ApiResponse> logOutAllDevices(@RequestParam Long userId) {
        try {
            authService.logoutAllDevices(userId);
            return ResponseEntity.ok(new ApiResponse("Logout successful", null));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserDetailsById(@PathVariable Long userId){
        try {
            UserInfo userInfo = authService.getUserById(userId);
            return ResponseEntity.ok(new ApiResponse("successfully found user details", userInfo));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse(e.getMessage(), null));
        }
    }
}
