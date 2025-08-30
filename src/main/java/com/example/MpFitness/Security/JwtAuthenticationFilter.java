package com.example.MpFitness.Security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Obtém o token do header Authorization
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                // Valida o token
                if (jwtUtils.validateToken(token)) {
                    // Extrai os claims do token
                    Claims claims = jwtUtils.extractAllClaims(token);

                    String email = claims.getSubject(); // normalmente o "sub" é o email/usuário
                    String role = claims.get("role", String.class);

                    if (email != null && role != null) {
                        // Prefixa "ROLE_" para compatibilidade com Spring Security
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(authority));

                        // Define o contexto de segurança com o usuário autenticado
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ex) {
            // Em caso de erro na autenticação JWT, limpa o contexto e prossegue (sem
            // autenticação)
            SecurityContextHolder.clearContext();
            logger.error("Falha na autenticação JWT: " + ex.getMessage());
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}
