package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Services.ClienteService;
import com.example.MpFitness.exceptions.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void deveFazerLoginComSucessoERetornarToken() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(42L);
        cliente.setEmail("user@example.com");

        when(clienteService.autenticar("user@example.com", "Senha123")).thenReturn(cliente);
        when(jwtUtils.generateToken(cliente)).thenReturn("jwt-token-ok");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"senha\":\"Senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-ok"));

        verify(clienteService).autenticar(eq("user@example.com"), eq("Senha123"));
        verify(jwtUtils).generateToken(cliente);
    }

    @Test
    void deveRetornarUnauthorizedQuandoCredenciaisForemInvalidas() throws Exception {
        when(clienteService.autenticar("user@example.com", "errada"))
                .thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas ou token expirado"));
    }

    @Test
    void deveRetornarBadRequestQuandoPayloadDeLoginForInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validação falhou"));
    }

    @Test
    void deveRegistrarClienteComSucesso() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"novo@example.com\",\"nome\":\"Novo\",\"senha\":\"Senha1234\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cliente registrado com sucesso!"));
    }
}
