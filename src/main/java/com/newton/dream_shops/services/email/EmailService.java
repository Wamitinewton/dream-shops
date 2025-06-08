package com.newton.dream_shops.services.email;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.newton.dream_shops.exception.EmailServiceException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name:Dream Shops}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Override
    public void sendSignUpOtp(String email, String otp, String firstName) {
        Map<String, Object> variables = buildCommonVariables();
        variables.put("firstName", firstName);
        variables.put("otp", otp);
        variables.put("expiryMinutes", otpExpiryMinutes);

        sendTemplateEmail(email, "🔐 Verify Your Account - Dream Shops", "signup-otp", variables);
    }

    @Override
    public void sendForgotPasswordOtp(String email, String otp, String firstName) {
        Map<String, Object> variables = buildCommonVariables();
        variables.put("firstName", firstName);
        variables.put("otp", otp);
        variables.put("expiryMinutes", otpExpiryMinutes);
        variables.put("timestamp", getCurrentTimestamp());

        sendTemplateEmail(email, "🔑 Reset Your Password - Dream Shops", "forgot-password-otp", variables);
    }

    @Override
    public void sendPasswordResetSuccess(String email, String firstName) {
        Map<String, Object> variables = buildCommonVariables();
        variables.put("firstName", firstName);
        variables.put("timestamp", getCurrentTimestamp());

        sendTemplateEmail(email, "✅ Password Reset Successful - Dream Shops", "password-reset-success", variables);
    }

    @Override
    public void sendWelcomeEmail(String email, String firstName) {
        Map<String, Object> variables = buildCommonVariables();
        variables.put("firstName", firstName);
        variables.put("shopUrl", frontendUrl + "/shop");

        sendTemplateEmail(email, "🎉 Welcome to Dream Shops!", "welcome", variables);
    }

    @Override
    public void sendAccountActivationSuccess(String email, String firstName) {
        Map<String, Object> variables = buildCommonVariables();
        variables.put("firstName", firstName);
        variables.put("shopUrl", frontendUrl + "/shop");
        variables.put("profileUrl", frontendUrl + "/profile");

        sendTemplateEmail(email, "✅ Account Activated - Dream Shops", "account-activation-success", variables);
    }

    private void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setReplyTo(fromEmail);

            setEmailHeaders(message);

            String htmlContent = processTemplate(templateName, variables);
            String textContent = generatePlainTextVersion(htmlContent);

            helper.setText(textContent, htmlContent);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new EmailServiceException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new EmailServiceException("Failed to send email due to unexpected error", e);
        }
    }

    private void setEmailHeaders(MimeMessage message) throws MessagingException {
        message.setHeader("X-Priority", "1");
        message.setHeader("X-MSMail-Priority", "High");
        message.setHeader("X-Mailer", "Dream Shops");
        message.setHeader("X-Auto-Response-Suppress", "OOF, AutoReply");
        message.setHeader("Content-Type", "text/html; charset=UTF-8");
    }

    private String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process("email/" + templateName, context);
    }

    private String generatePlainTextVersion(String htmlContent) {
        return htmlContent
                .replaceAll("<[^>]+>", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Map<String, Object> buildCommonVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("supportEmail", fromEmail);
        variables.put("companyName", "Dream Shops");
        variables.put("currentYear", java.time.Year.now().getValue());
        variables.put("loginUrl", frontendUrl + "/login");
        return variables;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
    }
}