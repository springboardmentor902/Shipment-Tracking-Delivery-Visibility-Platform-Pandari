package com.shiptrack.shiptrack_pro.service.integration;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioSmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    /**
     * Send SMS to a phone number
     */
    public boolean sendSms(String toPhoneNumber, String messageBody) {
        try {
            Twilio.init(accountSid, authToken);

            Message message = Message.creator(
                            new PhoneNumber(toPhoneNumber),           // To number
                            new PhoneNumber(twilioPhoneNumber),       // From number
                            messageBody)                              // SMS body
                    .create();

            log.info("SMS sent successfully. SID: {}, To: {}", message.getSid(), toPhoneNumber);
            return true;
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send shipment status update SMS
     */
    public boolean sendShipmentStatusSms(String phoneNumber, String trackingNumber, String status) {
        try {
            String messageBody = String.format(
                    "ShipTrack: Your shipment %s status is now %s. Track it at: https://shiptrack.com/track/%s",
                    trackingNumber, status.toLowerCase(), trackingNumber);

            return sendSms(phoneNumber, messageBody);
        } catch (Exception e) {
            log.error("Error sending shipment status SMS", e);
            return false;
        }
    }

    /**
     * Send delivery notification SMS
     */
    public boolean sendDeliveryNotificationSms(String phoneNumber, String trackingNumber,
                                               String deliveryAddress) {
        try {
            String messageBody = String.format(
                    "ShipTrack: Your package %s is out for delivery to %s. Track updates at: https://shiptrack.com/track/%s",
                    trackingNumber, deliveryAddress, trackingNumber);

            return sendSms(phoneNumber, messageBody);
        } catch (Exception e) {
            log.error("Error sending delivery notification SMS", e);
            return false;
        }
    }

    /**
     * Send delay notification SMS
     */
    public boolean sendDelayNotificationSms(String phoneNumber, String trackingNumber,
                                            String reason, String newEstimate) {
        try {
            String messageBody = String.format(
                    "ShipTrack: Your package %s has been delayed due to %s. New estimated delivery: %s",
                    trackingNumber, reason, newEstimate);

            return sendSms(phoneNumber, messageBody);
        } catch (Exception e) {
            log.error("Error sending delay notification SMS", e);
            return false;
        }
    }

    /**
     * Send OTP/verification code SMS
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode) {
        try {
            String messageBody = String.format(
                    "ShipTrack: Your OTP verification code is: %s. This code expires in 5 minutes.",
                    otpCode);

            return sendSms(phoneNumber, messageBody);
        } catch (Exception e) {
            log.error("Error sending OTP SMS", e);
            return false;
        }
    }

    /**
     * Send password reset SMS
     */
    public boolean sendPasswordResetSms(String phoneNumber, String resetLink) {
        try {
            String messageBody = String.format(
                    "ShipTrack: Click here to reset your password: %s This link expires in 30 minutes.",
                    resetLink);

            return sendSms(phoneNumber, messageBody);
        } catch (Exception e) {
            log.error("Error sending password reset SMS", e);
            return false;
        }
    }

    /**
     * Send promotional SMS
     */
    public boolean sendPromotionalSms(String phoneNumber, String promotion) {
        try {
            String messageBody = String.format(
                    "ShipTrack: %s Reply STOP to unsubscribe.",
                    promotion);

            return sendSms(phoneNumber, messageBody);
        } catch (Exception e) {
            log.error("Error sending promotional SMS", e);
            return false;
        }
    }

    /**
     * Send delivery appointment confirmation SMS
     */
    public boolean sendDeliveryAppointmentSms(String phoneNumber, String trackingNumber,
                                              String appointmentDate, String appointmentTime) {
        try {
            String messageBody = String.format(
                    "ShipTrack: Delivery appointment confirmed for %s at %s for package %s. Reply Y to confirm or N to reschedule.",
                    appointmentDate, appointmentTime, trackingNumber);

            return sendSms(phoneNumber, messageBody);
        } catch (Exception e) {
            log.error("Error sending delivery appointment SMS", e);
            return false;
        }
    }

    /**
     * Send bulk SMS to multiple recipients
     */
    public java.util.Map<String, Boolean> sendBulkSms(java.util.List<String> phoneNumbers,
                                                      String messageBody) {
        java.util.Map<String, Boolean> results = new java.util.HashMap<>();

        for (String phoneNumber : phoneNumbers) {
            results.put(phoneNumber, sendSms(phoneNumber, messageBody));
        }

        return results;
    }
}

