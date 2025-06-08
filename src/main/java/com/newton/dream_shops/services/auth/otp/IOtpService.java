package com.newton.dream_shops.services.auth.otp;

import com.newton.dream_shops.enums.OtpType;
import com.newton.dream_shops.models.auth.User;

public interface IOtpService {

    void generateAndSendEmailVerificationOtp(User user);

    void generateAndSendPasswordResetOtp(String email);

    boolean verifyEmailVerificationOtp(String email, String otpCode);

    boolean verifyPasswordResetOtp(String email, String otpCode);

    void activateUserAccount(String email, String otpCode);

    void resendEmailVerificationOtp(String email);

    boolean isOtpValid(String email, String otpCode, OtpType type);
}