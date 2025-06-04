package com.newton.dream_shops.models.oauth2;

import java.util.Map;

import com.newton.dream_shops.enums.AuthProvider;
import com.newton.dream_shops.exception.CustomException;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new CustomException("Login with " + registrationId + " is not supported");
        }
    }

}
