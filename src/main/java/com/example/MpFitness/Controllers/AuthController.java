package com.example.MpFitness.Controllers;

import com.example.MpFitness.DTO.LoginRequestDto;
import com.example.MpFitness.DTO.RegisterRequestDto;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Services.ClienteService;
import com.example.MpFitness.exceptions.ClienteNaoEncontradoException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ClienteService clienteService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto request) {
        clienteService.criar(request);
        return ResponseEntity.ok("Cliente registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        Cliente clienteAutenticado = clienteService.autenticar(request.getEmail(), request.getSenha());
        String token = jwtUtils.generateToken(clienteAutenticado);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("login-google")
    public ResponseEntity<?> loginComGoogle(OAuth2AuthenticationToken authentication) {
        OAuth2User user = authentication.getPrincipal();
        String email = user.getAttribute("email");
        Cliente cliente = clienteService.buscarPorEmail(email)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado para o email: " + email));
        String token = jwtUtils.generateToken(cliente);

        return ResponseEntity.ok(Map.of("token", token));
    }

}
