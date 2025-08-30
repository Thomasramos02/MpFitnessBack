package com.example.MpFitness.Controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Cliente.Role;
import com.example.MpFitness.Services.ClienteService;

import lombok.RequiredArgsConstructor;

@Service
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
            Cliente novoCliente = new Cliente();
            novoCliente.setEmail(email);
            novoCliente.setNome(nome);
            novoCliente.setSenha("oauth2user");
            novoCliente.setRole(Role.CLIENTE);
            clienteService.criar(novoCliente);
        }
        return oauth2User;

    }

}