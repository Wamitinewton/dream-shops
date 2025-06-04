package com.newton.dream_shops.controller.auth;

import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.security.oauth.OAuth2UserPrincipal;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.services.auth.AuthService;
import com.newton.dream_shops.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2 Authentication", description = "OAuth2 authentication endpoints")
public class OAuth2Controller {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String authorizedRedirectUri;

    @Operation(summary = "Initiate Google OAuth2 authentication", description = "Redirects user to Google OAuth2 authorization page")
    @GetMapping("/google")
    public void initiateGoogleAuth(HttpServletResponse response) throws IOException {
        String authUrl = "/oauth2/authorize/google";
        response.sendRedirect(authUrl);
    }

    @Operation(summary = "Handle OAuth2 callback", description = "Handles the callback from OAuth2 provider and processes authentication")
    @GetMapping("/callback/{provider}")
    public void handleOAuth2Callback(
            @Parameter(description = "OAuth2 provider name") @PathVariable String provider,
            @Parameter(description = "Authorization code") @RequestParam(required = false) String code,
            @Parameter(description = "State parameter") @RequestParam(required = false) String state,
            @Parameter(description = "Error parameter") @RequestParam(required = false) String error,
            @Parameter(description = "Error description") @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        try {
            if (error != null) {
                redirectWithError(response, "OAuth2 authentication failed: " + errorDescription);
                return;
            }

            if (authentication != null && authentication.isAuthenticated()) {
                OAuth2UserPrincipal oAuth2UserPrincipal = (OAuth2UserPrincipal) authentication.getPrincipal();
                User user = oAuth2UserPrincipal.getUser();

                String accessToken = jwtUtil.generateAccessToken(user);
                String refreshToken = authService.generateRefreshToken(user);

                authService.cleanUpExpiredTokens();
                authService.limitActiveTokensPerUser(user.getId());

                redirectWithSuccess(response, accessToken, refreshToken);
            } else {
                redirectWithError(response, "Authentication failed");
            }

        } catch (Exception e) {
            redirectWithError(response, "Authentication processing failed");
        }
    }

    @Operation(summary = "Get current OAuth2 user information", description = "Returns information about the currently authenticated OAuth2 user")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse> getCurrentUser(@AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            if (oauth2User == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse("User not authenticated", null));
            }

            Map<String, Object> attributes = oauth2User.getAttributes();

            Map<String, Object> userData = Map.of(
                    "id", attributes.get("sub"),
                    "email", attributes.get("email"),
                    "name", attributes.get("name"),
                    "picture", attributes.get("picture"),
                    "emailVerified", attributes.get("email_verified"));

            return ResponseEntity.ok(new ApiResponse("User information retrieved successfully", userData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve user information", null));
        }
    }

    @Operation(summary = "Generate JWT tokens from OAuth2 session", description = "Converts OAuth2 session to JWT tokens for API access")
    @PostMapping("/token")
    public ResponseEntity<ApiResponse> generateTokenFromOAuth2(
            @AuthenticationPrincipal OAuth2UserPrincipal oauth2UserPrincipal) {
        try {
            if (oauth2UserPrincipal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse("User not authenticated", null));
            }

            User user = oauth2UserPrincipal.getUser();

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = authService.generateRefreshToken(user);

            authService.cleanUpExpiredTokens();
            authService.limitActiveTokensPerUser(user.getId());

            Map<String, Object> tokenData = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "tokenType", "Bearer",
                    "user", user);

            return ResponseEntity.ok(new ApiResponse("Tokens generated successfully", tokenData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to generate tokens", null));
        }
    }

    @Operation(summary = "Check OAuth2 authentication status", description = "Returns the current OAuth2 authentication status")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getAuthStatus(Authentication authentication) {
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();

        Map<String, Object> statusData = Map.of(
                "authenticated", isAuthenticated,
                "authType", isAuthenticated ? authentication.getClass().getSimpleName() : "none");

        return ResponseEntity.ok(new ApiResponse("Authentication status retrieved", statusData));
    }

    private void redirectWithSuccess(HttpServletResponse response, String accessToken, String refreshToken)
            throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("token", URLEncoder.encode(accessToken, StandardCharsets.UTF_8))
                .queryParam("refreshToken", URLEncoder.encode(refreshToken, StandardCharsets.UTF_8))
                .queryParam("success", "true")
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }

    private void redirectWithError(HttpServletResponse response, String errorMessage) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .queryParam("success", "false")
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}