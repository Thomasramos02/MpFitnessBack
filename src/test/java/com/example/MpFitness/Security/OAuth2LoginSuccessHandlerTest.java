package com.example.MpFitness.Security;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Services.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2LoginSuccessHandlerTest {

    @Test
    void testRedirectsToFrontendWithToken() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        ClienteService clienteService = mock(ClienteService.class);

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(jwtUtils, clienteService);
        java.lang.reflect.Field f = handler.getClass().getDeclaredField("frontendUrl");
        f.setAccessible(true);
        f.set(handler, "http://localhost:3000");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = mock(Authentication.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("user@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("User Name");
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("user@example.com");

        when(clienteService.buscarPorEmail("user@example.com")).thenReturn(Optional.of(cliente));
        when(jwtUtils.generateToken(cliente)).thenReturn("jwt-token-here");

        handler.onAuthenticationSuccess(request, response, authentication);

        String redirected = response.getRedirectedUrl();
        assertThat(redirected).startsWith("http://localhost:3000/login");
        assertThat(redirected).contains("token=jwt-token-here");
    }
}
