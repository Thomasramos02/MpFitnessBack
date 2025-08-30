package com.example.MpFitness.Security;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Cliente.Role;
import com.example.MpFitness.Services.ClienteService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final ClienteService clienteService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        //Pega o nome e prepara um nome alternativo caso seja nulo
        String nome = oAuth2User.getAttribute("name");
        if (nome == null || nome.isBlank()) {
            nome = email.split("@")[0]; // Usa a parte local do e-mail como nome
        }

        log.info("Usuário autenticado via OAuth2 com email: {}", email);

        final String finalNome = nome; // Variável final para usar dentro do lambda
        Cliente cliente = clienteService.buscarPorEmail(email).orElseGet(() -> {
            log.info("Cliente com email {} não encontrado. Criando novo cliente.", email);
            Cliente novoCliente = new Cliente();
            novoCliente.setEmail(email);
            novoCliente.setNome(finalNome); // Usa o nome (garantido que não é nulo)
            novoCliente.setSenha(null);
            novoCliente.setRole(Role.CLIENTE);
            return clienteService.criar(novoCliente);
        });

        String token = jwtUtils.generateToken(cliente);
        log.info("Token JWT gerado para o cliente: {}", cliente.getEmail());

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/index.html")
                .queryParam("token", token) // ✅ envia o token na URL
                .build().toUriString();

        clearAuthenticationAttributes(request);
        log.info("Redirecionando para o frontend: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}