package com.newton.dream_shops.services.auth;

import com.newton.dream_shops.enums.AuthProvider;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.models.oauth2.OAuth2UserInfo;
import com.newton.dream_shops.models.oauth2.OAuth2UserInfoFactory;
import com.newton.dream_shops.repository.auth.UserRepository;
import com.newton.dream_shops.security.oauth.OAuth2UserPrincipal;
import com.newton.dream_shops.services.cart.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ICartService cartService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user: {}", ex.getMessage());
            throw new OAuth2AuthenticationException("OAuth2 authentication failed: " + ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new CustomException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getProvider().equals(AuthProvider.valueOf(
                    oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()))) {
                throw new CustomException("You're signed up with " + user.getProvider() +
                        " account. Please use your " + user.getProvider() + " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        // Create cart for the user if it doesn't exist
        try {
            cartService.getOrCreateCartForUser(user.getId());
        } catch (Exception e) {
            log.warn("Failed to create cart for OAuth2 user {}: {}", user.getId(), e.getMessage());
        }

        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        user.setProvider(
                AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setFirstName(oAuth2UserInfo.getFirstName() != null ? oAuth2UserInfo.getFirstName() : "");
        user.setLastName(oAuth2UserInfo.getLastName() != null ? oAuth2UserInfo.getLastName() : "");
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setEmailVerified(true);
        user.setImageUrl(oAuth2UserInfo.getImageUrl());

        // Generate unique username for OAuth2 users
        user.setUsername(generateUniqueUsername(oAuth2UserInfo.getEmail()));

        // OAuth2 users don't have passwords
        user.setPassword(null);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        if (StringUtils.hasText(oAuth2UserInfo.getFirstName()) &&
                !oAuth2UserInfo.getFirstName().equals(existingUser.getFirstName())) {
            existingUser.setFirstName(oAuth2UserInfo.getFirstName());
        }

        if (StringUtils.hasText(oAuth2UserInfo.getLastName()) &&
                !oAuth2UserInfo.getLastName().equals(existingUser.getLastName())) {
            existingUser.setLastName(oAuth2UserInfo.getLastName());
        }

        if (StringUtils.hasText(oAuth2UserInfo.getImageUrl()) &&
                !oAuth2UserInfo.getImageUrl().equals(existingUser.getImageUrl())) {
            existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        }

        existingUser.setEmailVerified(true);

        return userRepository.save(existingUser);
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = baseUsername;

        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}