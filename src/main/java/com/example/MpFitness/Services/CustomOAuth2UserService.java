package com.example.MpFitness.Services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.MpFitness.DTO.RegisterRequestDto;
import com.example.MpFitness.Model.Cliente;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final ClienteService clienteService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
        OAuth2User oauth2User = delegate.loadUser(oAuth2UserRequest);

        String email = oauth2User.getAttribute("email");
        String nome = oauth2User.getAttribute("name");
        Optional<Cliente> optionalCliente = clienteService.buscarPorEmail(email);

        if (optionalCliente.isEmpty()) {
            RegisterRequestDto novoCliente = new RegisterRequestDto(email, nome, "oauth2user");
            clienteService.criar(novoCliente);
        }
        return oauth2User;

    }

}