package com.example.MpFitness.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PasswordResetEmailServiceImpl implements PasswordResetEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@mpfitness.local}")
    private String fromEmail;

    public PasswordResetEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String nome, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Redefinicao de senha - MpFitness");
            message.setText(String.format(
                    "Ola %s,%n%n" +
                            "Recebemos uma solicitacao para redefinir sua senha.%n" +
                            "Use o link abaixo para criar uma nova senha:%n%s%n%n" +
                            "Este link expira em alguns minutos.%n" +
                            "Se voce nao solicitou esta alteracao, ignore este email.%n",
                    nome,
                    resetLink));

            mailSender.send(message);
            log.info("Email de reset enviado para {}", toEmail);
        } catch (Exception e) {
            log.error("Erro ao enviar email de reset para {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar email de recuperação de senha", e);
        }
    }
}
