package com.example.MpFitness.Services;

public interface ForgotPasswordService {
    void requestPasswordReset(String email);

    void resetPassword(String token, String novaSenha);
}
