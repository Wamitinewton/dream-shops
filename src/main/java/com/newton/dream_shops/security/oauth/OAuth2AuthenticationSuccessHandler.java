package com.newton.dream_shops.security.oauth;

import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.repository.auth.UserRepository;
import com.newton.dream_shops.util.jwt.JwtHelperService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String redirectUri;

    private final JwtHelperService jwtHelperService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {

        User user = extractUserFromAuthentication(authentication);

        if (user == null) {
            authentication.getPrincipal().getClass().getSimpleName();
            return buildErrorUrl("User extraction failed");
        }

        try {
            String[] tokens = jwtHelperService.generateTokenPair(user);
            String accessToken = tokens[0];
            String refreshToken = tokens[1];

            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", URLEncoder.encode(accessToken, StandardCharsets.UTF_8))
                    .queryParam("refreshToken", URLEncoder.encode(refreshToken, StandardCharsets.UTF_8))
                    .queryParam("success", "true")
                    .build().toUriString();

        } catch (Exception e) {
            return buildErrorUrl("Authentication failed");
        }
    }

    private User extractUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            User user = ((OAuth2UserPrincipal) principal).getUser();
            return user;
        }

        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            String email = oidcUser.getEmail();

            if (email != null) {
                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()) {
                    return userOptional.get();
                } else {
                }
            } else {
            }
        }

        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");

            if (email != null) {
                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()) {
                    return userOptional.get();
                } else {
                }
            } else {
            }
        }

        principal.getClass().getSimpleName();
        return null;
    }

    private String buildErrorUrl(String errorMessage) {
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .queryParam("success", "false")
                .build().toUriString();
    }
}