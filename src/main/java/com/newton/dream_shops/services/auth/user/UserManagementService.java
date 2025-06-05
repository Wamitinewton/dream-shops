package com.newton.dream_shops.services.auth.user;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.newton.dream_shops.dto.auth.UpdatePasswordRequest;
import com.newton.dream_shops.dto.auth.UpdateProfileRequest;
import com.newton.dream_shops.dto.auth.UserInfo;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.repository.auth.UserRepository;
import com.newton.dream_shops.util.jwt.JwtHelperService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManagementService implements IUserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtHelperService jwtHelperService;

    @Override
    @Transactional
    public UserInfo updatePassword(HttpServletRequest request, UpdatePasswordRequest updatePasswordRequest) {
        validatePasswordUpdateRequest(null, updatePasswordRequest);

        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        validatePasswordUpdateRequest(user, updatePasswordRequest);

        if (user.getPassword() != null &&
                isPasswordTooSimilar(updatePasswordRequest.getCurrentPassword(),
                        updatePasswordRequest.getNewPassword())) {
            throw new CustomException("New password must be significantly different from current password");
        }

        user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));

        User savedUser = userRepository.save(user);

        jwtHelperService.performLogoutAllDevices(request);

        return mapToUserInfo(savedUser);
    }

    @Override
    @Transactional
    public UserInfo updateProfile(HttpServletRequest request, UpdateProfileRequest updateProfileRequest) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        validateProfileUpdateRequest(user, updateProfileRequest);

        boolean updated = false;

        if (StringUtils.hasText(updateProfileRequest.getFirstName()) &&
                !updateProfileRequest.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(updateProfileRequest.getFirstName().trim());
            updated = true;
        }

        if (StringUtils.hasText(updateProfileRequest.getLastName()) &&
                !updateProfileRequest.getLastName().equals(user.getLastName())) {
            user.setLastName(updateProfileRequest.getLastName().trim());
            updated = true;
        }

        if (StringUtils.hasText(updateProfileRequest.getUsername()) &&
                !updateProfileRequest.getUsername().equals(user.getUsername())) {
            String newUsername = updateProfileRequest.getUsername().trim().toLowerCase();

            if (userRepository.existsByUsername(newUsername)) {
                throw new CustomException("Username already exists");
            }

            user.setUsername(newUsername);
            updated = true;
        }

        if (updated) {
            User savedUser = userRepository.save(user);
            return mapToUserInfo(savedUser);
        }

        return mapToUserInfo(user);
    }

    @Override
    public void validatePasswordUpdateRequest(User user, UpdatePasswordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Password update request is required");
        }

        if (!StringUtils.hasText(request.getNewPassword())) {
            throw new IllegalArgumentException("New password is required");
        }

        if (!StringUtils.hasText(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation is required");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("New password and confirmation do not match");
        }

        if (user != null) {
            if (user.getPassword() != null) {
                if (!StringUtils.hasText(request.getCurrentPassword())) {
                    throw new CustomException("Current password is required");
                }

                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                    throw new CustomException("Current password is incorrect");
                }
            }
        }
    }

    @Override
    public void validateProfileUpdateRequest(User user, UpdateProfileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Profile update request is required");
        }

        if (!StringUtils.hasText(request.getFirstName()) &&
                !StringUtils.hasText(request.getLastName()) &&
                !StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        if (StringUtils.hasText(request.getUsername())) {
            String username = request.getUsername().trim();
            if (username.length() < 3 || username.length() > 30) {
                throw new CustomException("Username must be between 3 and 30 characters");
            }
            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                throw new CustomException("Username can only contain letters, numbers, and underscores");
            }
        }
    }

    @Override
    public boolean isPasswordTooSimilar(String currentPassword, String newPassword) {
        if (currentPassword == null || newPassword == null) {
            return false;
        }

        String current = currentPassword.toLowerCase();
        String newPass = newPassword.toLowerCase();

        if (current.equals(newPass)) {
            return true;
        }

        // check if one is a substring of another
        int minLength = Math.min(current.length(), newPass.length());
        int maxLength = Math.max(current.length(), newPass.length());

        double overlapThreshold = 0.7;

        // Count common subsequences
        int commomChars = 0;
        for (int i = 0; i < minLength; i++) {
            if (current.charAt(i) == newPass.charAt(i)) {
                commomChars++;
            }
        }

        double overlapRatio = (double) commomChars / maxLength;
        if (overlapRatio > overlapThreshold) {
            return true;
        }

        String reversedCurrent = new StringBuilder(current).reverse().toString();
        if (reversedCurrent.equals(newPass)) {
            return true;
        }

        return false;
    }

    public UserInfo mapToUserInfo(User user) {
        UserInfo userInfo = modelMapper.map(user, UserInfo.class);

        return userInfo;
    }

}
