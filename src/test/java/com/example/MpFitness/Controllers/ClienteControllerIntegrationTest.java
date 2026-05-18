package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Services.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void deveRetornarClienteAutenticadoEmMe() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        cliente.setNome("Maria");
        cliente.setEmail("maria@example.com");
        cliente.setSenha("hash-senha");
        cliente.setRole(Cliente.Role.CLIENTE);

        when(jwtUtils.extractId("token-ok")).thenReturn(7L);
        when(clienteService.findById(7L)).thenReturn(cliente);

        mockMvc.perform(get("/api/clientes/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.senha").isEmpty());

        verify(jwtUtils).extractId("token-ok");
        verify(clienteService).findById(7L);
    }

    @Test
    void deveRetornarUnauthorizedQuandoTokenNaoForInformado() throws Exception {
        mockMvc.perform(get("/api/clientes/me")
                .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Token não informado"));

        verify(jwtUtils, never()).extractId(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void deveRetornarUnauthorizedQuandoTokenForInvalido() throws Exception {
        mockMvc.perform(get("/api/clientes/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer undefined"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Token inválido"));

        verify(jwtUtils, never()).extractId(org.mockito.ArgumentMatchers.anyString());
    }
}
