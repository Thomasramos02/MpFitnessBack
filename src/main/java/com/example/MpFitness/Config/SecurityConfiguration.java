package com.example.MpFitness.Config;

import com.example.MpFitness.Security.JwtAuthenticationFilter;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Security.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

        private final JwtUtils jwtUtils;
        private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

        @Value("${frontend.url:http://localhost:3000}")
        private String frontendUrl;

        @Value("${CORS_ALLOWED_ORIGINS:}")
        private String corsAllowedOrigins;

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
                return new JwtAuthenticationFilter(jwtUtils);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/register",
                                                                "/api/auth/login",
                                                                "/api/checkout-analytics/eventos",
                                                                "/oauth2/**",
                                                                "/login/oauth2/code/google",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/api/produtos/**",
                                                                "/api/auth/forgot-password",
                                                                "/api/auth/reset-password",
                                                                "/uploads/**",
                                                                "/actuator/health",
                                                                "/actuator/health/**",
                                                                "/actuator/info")
                                                .permitAll()
                                                .requestMatchers("/api/payments/webhook").permitAll()
                                                .requestMatchers("/clientes/*/carrinho/**").authenticated()
                                                .requestMatchers("/api/clientes/**").authenticated()
                                                .requestMatchers("/api/pedidos/**").authenticated()
                                                .requestMatchers("/api/payments/**").authenticated()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(oAuth2LoginSuccessHandler)
                                                .failureHandler((request, response, exception) -> {
                                                        String targetUrl = UriComponentsBuilder
                                                                        .fromUriString(frontendUrl)
                                                                        .path("/login")
                                                                        .queryParam("error", "true")
                                                                        .build().toUriString();
                                                        response.sendRedirect(targetUrl);
                                                }))
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                                                        "Não autorizado");
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                                                        "Acesso negado");
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(resolveAllowedOrigins());
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        private List<String> resolveAllowedOrigins() {
                List<String> allowedOrigins = new ArrayList<>(List.of(
                                "http://127.0.0.1:5500",
                                "http://localhost:5500",
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://localhost:5173",
                                frontendUrl));

                if (corsAllowedOrigins != null && !corsAllowedOrigins.isBlank()) {
                        Arrays.stream(corsAllowedOrigins.split(","))
                                        .map(String::trim)
                                        .filter(origin -> !origin.isBlank())
                                        .forEach(allowedOrigins::add);
                }

                return allowedOrigins.stream().distinct().toList();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }
}