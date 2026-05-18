package com.example.MpFitness.Services;

import org.springframework.stereotype.Service;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.Security.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final ClienteRepository clienteRepository;
    private final JwtUtils jwtUtils;
    private final ClienteService clienteService;
    private final PasswordResetEmailService passwordResetEmailService;

    @Value("${frontend.reset-password-url:${frontend.url}}")
    private String resetPasswordBaseUrl;

    public ForgotPasswordServiceImpl(ClienteRepository clienteRepository, JwtUtils jwtUtils,
            ClienteService clienteService,
            PasswordResetEmailService passwordResetEmailService) {
        this.clienteRepository = clienteRepository;
        this.jwtUtils = jwtUtils;
        this.clienteService = clienteService;
        this.passwordResetEmailService = passwordResetEmailService;
    }

    @Override
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email não encontrado");
        }

        Cliente cliente = clienteRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Email não encontrado"));
        String token = generateResetToken(cliente);
        String resetLink = UriComponentsBuilder.fromUriString(resetPasswordBaseUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        passwordResetEmailService.sendPasswordResetEmail(cliente.getEmail(), cliente.getNome(), resetLink);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token inválido");
        }
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new IllegalArgumentException("Nova senha inválida");
        }
        if (!jwtUtils.validatePasswordResetToken(token)) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }

        String email = jwtUtils.extractEmailFromPasswordResetToken(token);
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email não encontrado"));

        clienteService.alterarSenha(cliente.getId(), novaSenha.trim());
    }

    private String generateResetToken(Cliente cliente) {
        return jwtUtils.generatePasswordResetToken(cliente);
    }

}
