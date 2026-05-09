package com.pretor_sport.app.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendVerificationEmail(String to, String name, String token) throws MessagingException;
    void sendGenericEmail(String to, String subject, String content) throws MessagingException;
}
