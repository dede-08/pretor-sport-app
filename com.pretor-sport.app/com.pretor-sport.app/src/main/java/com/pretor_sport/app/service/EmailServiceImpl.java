package com.pretor_sport.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String to, String name, String token) throws MessagingException {
        String subject = "Verifica tu cuenta en Pretor Sport";
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        
        String htmlContent = String.format(
            "<html>" +
            "<body>" +
            "<h2>¡Hola %s!</h2>" +
            "<p>Gracias por registrarte en Pretor Sport. Para activar tu cuenta, por favor haz clic en el siguiente enlace:</p>" +
            "<div style='margin: 20px 0;'>" +
            "  <a href='%s' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Verificar mi cuenta</a>" +
            "</div>" +
            "<p>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>" +
            "<p>%s</p>" +
            "<br>" +
            "<p>Si no creaste esta cuenta, puedes ignorar este correo.</p>" +
            "<p>Atentamente,<br>El equipo de Pretor Sport</p>" +
            "</body>" +
            "</html>",
            name, verificationUrl, verificationUrl
        );

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendGenericEmail(String to, String subject, String content) throws MessagingException {
        sendHtmlEmail(to, subject, "<html><body>" + content + "</body></html>");
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.info("Enviando correo a: {}", to);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        log.info("Correo enviado exitosamente a {}", to);
    }
}
