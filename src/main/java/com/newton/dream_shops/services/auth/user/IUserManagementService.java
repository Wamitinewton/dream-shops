package com.newton.dream_shops.services.auth.user;

import com.newton.dream_shops.dto.auth.UpdatePasswordRequest;
import com.newton.dream_shops.dto.auth.UpdateProfileRequest;
import com.newton.dream_shops.dto.auth.UserInfo;
import com.newton.dream_shops.models.auth.User;

import jakarta.servlet.http.HttpServletRequest;

public interface IUserManagementService {

    UserInfo updatePassword(HttpServletRequest request, UpdatePasswordRequest updatePasswordRequest);

    UserInfo updateProfile(HttpServletRequest request, UpdateProfileRequest updateProfileRequest);

    void validatePasswordUpdateRequest(User user, UpdatePasswordRequest request);

    void validateProfileUpdateRequest(User user, UpdateProfileRequest request);

    boolean isPasswordTooSimilar(String currentPassword, String newPassword);

    void initiatePasswordReset(String email);

    void resetPassword(String email, String otp, String newPassword);

    UserInfo getUserById(HttpServletRequest request);

    void deleteUser(HttpServletRequest request);

    void logout(String refreshToken);

    void logoutAllDevices(HttpServletRequest request);

}
