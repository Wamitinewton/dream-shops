package com.newton.dream_shops.controller.auth;

import com.newton.dream_shops.dto.auth.PasswordResetRequest;
import com.newton.dream_shops.dto.auth.RefreshTokenRequest;
import com.newton.dream_shops.dto.auth.UpdatePasswordRequest;
import com.newton.dream_shops.dto.auth.UpdateProfileRequest;
import com.newton.dream_shops.dto.auth.UserInfo;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.auth.user.IUserManagementService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserManagementService userManagementService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getUserDetailsById(HttpServletRequest request) {
        try {
            UserInfo userInfo = userManagementService.getUserById(request);
            return ResponseEntity.ok(new ApiResponse("Successfully found user details", userInfo));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/delete-user/delete")
    public ResponseEntity<ApiResponse> deleteUserByUserId(HttpServletRequest request) {
        try {
            userManagementService.deleteUser(request);
            return ResponseEntity.ok(new ApiResponse("Successfully deleted account", null));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<ApiResponse> logOutAllDevices(HttpServletRequest request) {
        try {

            userManagementService.logoutAllDevices(request);
            return ResponseEntity.ok(new ApiResponse("Logout successful", null));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(
            HttpServletRequest request,
            @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        try {
            UserInfo updatedUser = userManagementService.updatePassword(request, updatePasswordRequest);
            return ResponseEntity
                    .ok(new ApiResponse("Password updated successfully. Please login again.", updatedUser));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Password update failed. Please try again.", null));
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        try {
            UserInfo updatedUser = userManagementService.updateProfile(request, updateProfileRequest);
            return ResponseEntity.ok(new ApiResponse("Profile updated successfully", updatedUser));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Profile update failed. Please try again.", null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody RefreshTokenRequest request) {
        try {
            userManagementService.logout(request.getRefreshToken());
            return ResponseEntity.ok(new ApiResponse("Logout successful", null));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        try {
            userManagementService.initiatePasswordReset(email);
            return ResponseEntity
                    .ok(new ApiResponse("Password reset email sent successfully! Please check your inbox.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        try {
            userManagementService.resetPassword(
                    passwordResetRequest.getEmail(),
                    passwordResetRequest.getOtp(),
                    passwordResetRequest.getNewPassword());
            return ResponseEntity
                    .ok(new ApiResponse("Password reset successful! You can now login with your new password.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}
