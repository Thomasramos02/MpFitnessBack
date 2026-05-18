package com.example.MpFitness.Services;

public interface PasswordResetEmailService {
    void sendPasswordResetEmail(String toEmail, String nome, String resetLink);
}
