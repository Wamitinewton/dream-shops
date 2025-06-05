package com.newton.dream_shops.services.auth.user;

import com.newton.dream_shops.dto.auth.UpdatePasswordRequest;
import com.newton.dream_shops.dto.auth.UpdateProfileRequest;
import com.newton.dream_shops.dto.auth.UserInfo;
import com.newton.dream_shops.models.auth.User;

import jakarta.servlet.http.HttpServletRequest;

public interface IUserManagementService {

    /**
     * Updates user password with security validations
     * @param request HTTP request to extract current user
     * @param updatePasswordRequest Password update details
     * @return Updated user information
     */
    UserInfo updatePassword(HttpServletRequest request, UpdatePasswordRequest updatePasswordRequest);
    
    /**
     * Updates user profile information (excluding email)
     * @param request HTTP request to extract current user
     * @param updateProfileRequest Profile update details
     * @return Updated user information
     */
    UserInfo updateProfile(HttpServletRequest request, UpdateProfileRequest updateProfileRequest);
    
    /**
     * Validates password update request
     * @param user Current user
     * @param request Password update request
     */
    void validatePasswordUpdateRequest(User user, UpdatePasswordRequest request);
    
    /**
     * Validates profile update request
     * @param user Current user
     * @param request Profile update request
     */
    void validateProfileUpdateRequest(User user, UpdateProfileRequest request);
    
    /**
     * Checks if new password is similar to current password
     * @param currentPassword Current password (plain text)
     * @param newPassword New password (plain text)
     * @return true if passwords are too similar
     */
    boolean isPasswordTooSimilar(String currentPassword, String newPassword);
}
