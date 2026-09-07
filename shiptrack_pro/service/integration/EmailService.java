package com.shiptrack.shiptrack_pro.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender MailSender;

    @Value("${spring.mail.username:noreply@shiptrack.com}")
    private String fromEmail;

    /**
     * Send simple text email
     */
    public boolean sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            MailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send HTML email
     */
    public boolean sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = MailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            MailSender.send(message);
            log.info("HTML email sent successfully to: {}", toEmail);
            return true;
        } catch (MessagingException e) {
            log.error("Error sending HTML email to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send email with attachment
     */
    public boolean sendEmailWithAttachment(String toEmail, String subject, String body,
                                           String attachmentPath, String attachmentName) {
        try {
            MimeMessage message = MailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            // Add attachment
            helper.addAttachment(attachmentName, new java.io.File(attachmentPath));

            MailSender.send(message);
            log.info("Email with attachment sent successfully to: {}", toEmail);
            return true;
        } catch (MessagingException e) {
            log.error("Error sending email with attachment to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send shipment created notification email
     */
    public boolean sendShipmentCreatedEmail(String toEmail, String trackingNumber,
                                            String senderName, String receiverName) {
        try {
            String subject = "Shipment Created - " + trackingNumber;
            String htmlBody = String.format(
                    "<html><body>" +
                            "<h2>Shipment Confirmation</h2>" +
                            "<p>Your shipment has been created successfully.</p>" +
                            "<p><strong>Tracking Number:</strong> %s</p>" +
                            "<p><strong>From:</strong> %s</p>" +
                            "<p><strong>To:</strong> %s</p>" +
                            "<p><a href='https://shiptrack.com/track/%s'>Track Your Shipment</a></p>" +
                            "<br/>" +
                            "<p>Thank you for using ShipTrack!</p>" +
                            "</body></html>",
                    trackingNumber, senderName, receiverName, trackingNumber);

            return sendHtmlEmail(toEmail, subject, htmlBody);
        } catch (Exception e) {
            log.error("Error sending shipment created email", e);
            return false;
        }
    }

    /**
     * Send shipment status update email
     */
    public boolean sendShipmentStatusEmail(String toEmail, String trackingNumber,
                                           String status, String location, String updateTime) {
        try {
            String subject = "Shipment Status Update - " + trackingNumber;
            String htmlBody = String.format(
                    "<html><body>" +
                            "<h2>Shipment Update</h2>" +
                            "<p>Your shipment has been updated.</p>" +
                            "<p><strong>Tracking Number:</strong> %s</p>" +
                            "<p><strong>Status:</strong> %s</p>" +
                            "<p><strong>Location:</strong> %s</p>" +
                            "<p><strong>Updated At:</strong> %s</p>" +
                            "<p><a href='https://shiptrack.com/track/%s'>View Full Details</a></p>" +
                            "</body></html>",
                    trackingNumber, status, location, updateTime, trackingNumber);

            return sendHtmlEmail(toEmail, subject, htmlBody);
        } catch (Exception e) {
            log.error("Error sending status update email", e);
            return false;
        }
    }

    /**
     * Send delivery notification email
     */
    public boolean sendDeliveryNotificationEmail(String toEmail, String trackingNumber,
                                                 String deliveryAddress, String deliveryTime) {
        try {
            String subject = "Your Package Has Been Delivered - " + trackingNumber;
            String htmlBody = String.format(
                    "<html><body>" +
                            "<h2>Delivery Confirmed</h2>" +
                            "<p>Your shipment has been delivered successfully!</p>" +
                            "<p><strong>Tracking Number:</strong> %s</p>" +
                            "<p><strong>Delivered To:</strong> %s</p>" +
                            "<p><strong>Delivery Time:</strong> %s</p>" +
                            "<p><a href='https://shiptrack.com/feedback/%s'>Rate Your Experience</a></p>" +
                            "<br/>" +
                            "<p>Thank you for choosing ShipTrack!</p>" +
                            "</body></html>",
                    trackingNumber, deliveryAddress, deliveryTime, trackingNumber);

            return sendHtmlEmail(toEmail, subject, htmlBody);
        } catch (Exception e) {
            log.error("Error sending delivery notification email", e);
            return false;
        }
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(String toEmail, String resetLink, String expiryTime) {
        try {
            String subject = "Reset Your ShipTrack Password";
            String htmlBody = String.format(
                    "<html><body>" +
                            "<h2>Password Reset Request</h2>" +
                            "<p>You requested to reset your password. Click the link below to proceed:</p>" +
                            "<p><a href='%s' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Reset Password</a></p>" +
                            "<p>This link will expire in %s</p>" +
                            "<p>If you didn't request this, please ignore this email.</p>" +
                            "</body></html>",
                    resetLink, expiryTime);

            return sendHtmlEmail(toEmail, subject, htmlBody);
        } catch (Exception e) {
            log.error("Error sending password reset email", e);
            return false;
        }
    }

    /**
     * Send account verification email
     */
    public boolean sendVerificationEmail(String toEmail, String verificationLink) {
        try {
            String subject = "Verify Your ShipTrack Account";
            String htmlBody = String.format(
                    "<html><body>" +
                            "<h2>Welcome to ShipTrack!</h2>" +
                            "<p>Thank you for registering. Please verify your email address to activate your account.</p>" +
                            "<p><a href='%s' style='background-color: #008CBA; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Verify Email</a></p>" +
                            "<p>This link will expire in 24 hours.</p>" +
                            "</body></html>",
                    verificationLink);

            return sendHtmlEmail(toEmail, subject, htmlBody);
        } catch (Exception e) {
            log.error("Error sending verification email", e);
            return false;
        }
    }

    /**
     * Send delay notification email
     */
    public boolean sendDelayNotificationEmail(String toEmail, String trackingNumber,
                                              String reason, String newEstimate) {
        try {
            String subject = "Delivery Delay Notification - " + trackingNumber;
            String htmlBody = String.format(
                    "<html><body>" +
                            "<h2>Delivery Delay Update</h2>" +
                            "<p>We regret to inform you that your shipment has been delayed.</p>" +
                            "<p><strong>Tracking Number:</strong> %s</p>" +
                            "<p><strong>Reason:</strong> %s</p>" +
                            "<p><strong>New Estimated Delivery:</strong> %s</p>" +
                            "<p><a href='https://shiptrack.com/track/%s'>Track Your Shipment</a></p>" +
                            "<p>We apologize for any inconvenience caused.</p>" +
                            "</body></html>",
                    trackingNumber, reason, newEstimate, trackingNumber);

            return sendHtmlEmail(toEmail, subject, htmlBody);
        } catch (Exception e) {
            log.error("Error sending delay notification email", e);
            return false;
        }
    }

    /**
     * Send bulk email to multiple recipients
     */
    public Map<String, Boolean> sendBulkEmail(List<String> toEmails, String subject, String body) {
        Map<String, Boolean> results = new HashMap<>();

        for (String email : toEmails) {
            results.put(email, sendSimpleEmail(email, subject, body));
        }

        return results;
    }
}

