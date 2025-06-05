package com.newton.dream_shops.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        String errorMessage = exception.getLocalizedMessage();
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = exception.getMessage();
        }
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "Authentication failed";
        }

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .queryParam("success", "false")
                .build().toUriString();

        log.error("OAuth2 authentication failed: {}", errorMessage);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}