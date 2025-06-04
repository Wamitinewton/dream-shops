package com.newton.dream_shops.security.oauth;

import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.util.JwtHelperService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String redirectUri;

    private final JwtHelperService jwtHelperService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {

        OAuth2UserPrincipal oAuth2UserPrincipal = (OAuth2UserPrincipal) authentication.getPrincipal();
        User user = oAuth2UserPrincipal.getUser();

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
            log.error("Error generating tokens for OAuth2 user: {}", e.getMessage());
            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", URLEncoder.encode("Authentication failed", StandardCharsets.UTF_8))
                    .queryParam("success", "false")
                    .build().toUriString();
        }
    }
}