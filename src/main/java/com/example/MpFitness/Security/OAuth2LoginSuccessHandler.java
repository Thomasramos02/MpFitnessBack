package com.example.MpFitness.Security;

import com.example.MpFitness.DTO.RegisterRequestDto;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Services.ClienteService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

        String nome = oAuth2User.getAttribute("name");
        if (nome == null || nome.isBlank()) {
            nome = email.split("@")[0];
        }

        log.info("Usuario autenticado via OAuth2 com email: {}", email);

        final String finalNome = nome;
        Cliente cliente = clienteService.buscarPorEmail(email).orElseGet(() -> {
            log.info("Cliente com email {} nao encontrado. Criando novo cliente.", email);
            RegisterRequestDto novoCliente = new RegisterRequestDto(email, finalNome, "oauth2user");
            return clienteService.criar(novoCliente);
        });

        String token = jwtUtils.generateToken(cliente);
        log.info("Token JWT gerado para o cliente: {}", cliente.getEmail());

        Cookie cookie = new Cookie("mpfitness_token", token);
        boolean production = frontendUrl != null && frontendUrl.startsWith("https://");
        cookie.setHttpOnly(true);
        cookie.setSecure(production);
        cookie.setPath("/");
        cookie.setMaxAge(36000); // 10 horas
        cookie.setAttribute("SameSite", production ? "None" : "Lax");
        response.addCookie(cookie);

        log.info("Cookie criado com sucesso: {} ", cliente.getEmail());

        clearAuthenticationAttributes(request);
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/login")
                .queryParam("token", token)
                .build(true)
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
