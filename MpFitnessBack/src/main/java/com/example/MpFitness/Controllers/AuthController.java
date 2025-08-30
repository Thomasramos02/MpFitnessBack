package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Services.ClienteService;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> register(@RequestBody Cliente cliente) {
        clienteService.criar(cliente);
        return ResponseEntity.ok("Cliente registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Cliente cliente) {
        try {
            Cliente clienteAutenticado = clienteService.autenticar(cliente.getEmail(), cliente.getSenha());
            String token = jwtUtils.generateToken(clienteAutenticado);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            String mensagem = e.getMessage();
            HttpStatus status = HttpStatus.BAD_REQUEST; 

            if (mensagem.contains("Senha incorreta")) {
                status = HttpStatus.UNAUTHORIZED; 
            } else if (mensagem.contains("Usuario não encontrado")) {
                status = HttpStatus.NOT_FOUND; // 404
            }

            return ResponseEntity.status(status)
                    .body(Map.of("error", mensagem));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno no servidor"));
        }
    }

    @GetMapping("login-google")
    public ResponseEntity<?> loginComGoogle(OAuth2AuthenticationToken authentication) {
        OAuth2User user = authentication.getPrincipal();
        String email = user.getAttribute("email");
        String nome = user.getAttribute("name");
        Optional<Cliente> optionalCliente = clienteService.buscarPorEmail(email);

        if (optionalCliente.isEmpty()) {
            return ResponseEntity.status(404).body("Cliente não encontrado");
        }
        Cliente cliente = optionalCliente.get();
        String token = jwtUtils.generateToken(cliente);

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String novaSenha = body.get("novaSenha"); // Para protótipo simples
        try {
            clienteService.alterarSenha(email, novaSenha);
            return ResponseEntity.ok("Senha alterada com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", e.getMessage()));
        }
    }


}
