package com.example.MpFitness.Controllers;

import com.example.MpFitness.DTO.CheckoutAbandonoEtapaDTO;
import com.example.MpFitness.DTO.ProdutoMaisVistoDTO;
import com.example.MpFitness.DTO.RecorrenciaDetalhadaDTO;
import com.example.MpFitness.Services.AdminMetricasService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMetricasController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMetricasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminMetricasService adminMetricasService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarProdutosMaisVistos() throws Exception {
        List<ProdutoMaisVistoDTO> resposta = List.of(
                new ProdutoMaisVistoDTO(1L, "Whey Premium", "Suplementos", "ATIVO", 128L));

        when(adminMetricasService.listarProdutosMaisVistos(5)).thenReturn(resposta);

        mockMvc.perform(get("/api/admin/metricas/produtos-mais-vistos").param("limite", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Whey Premium"))
                .andExpect(jsonPath("$[0].visualizacoes").value(128));

        verify(adminMetricasService).listarProdutosMaisVistos(5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCalcularAbandonoComPeriodoPadraoQuandoNaoInformado() throws Exception {
        when(adminMetricasService.calcularAbandonoCheckout(any(), any()))
                .thenReturn(List.of(new CheckoutAbandonoEtapaDTO("PAGAMENTO", 10L, 6L, 4L, 40.0)));

        mockMvc.perform(get("/api/admin/metricas/abandono-checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].etapa").value("PAGAMENTO"))
                .andExpect(jsonPath("$[0].entradas").value(10))
                .andExpect(jsonPath("$[0].taxaAbandono").value(40.0));

        ArgumentCaptor<LocalDateTime> inicioCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> fimCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(adminMetricasService).calcularAbandonoCheckout(inicioCaptor.capture(), fimCaptor.capture());

        LocalDateTime inicio = inicioCaptor.getValue();
        LocalDateTime fim = fimCaptor.getValue();

        assertThat(inicio).isBefore(fim);
        assertThat(Duration.between(inicio, fim).toDays()).isEqualTo(30);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarRecorrenciaDetalhada() throws Exception {
        RecorrenciaDetalhadaDTO resposta = new RecorrenciaDetalhadaDTO(
                8L,
                3L,
                37.5,
                List.of(new RecorrenciaDetalhadaDTO.ClienteRecorrenteDTO(7L, "Maria", 4L, new BigDecimal("299.90"))));

        when(adminMetricasService.calcularRecorrenciaDetalhada()).thenReturn(resposta);

        mockMvc.perform(get("/api/admin/metricas/recorrencia-detalhada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClientesComPedidos").value(8))
                .andExpect(jsonPath("$.totalClientesRecorrentes").value(3))
                .andExpect(jsonPath("$.clientesRecorrentes[0].nomeCliente").value("Maria"));

        verify(adminMetricasService).calcularRecorrenciaDetalhada();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCalcularAbandonoComPeriodoInformado() throws Exception {
        String inicio = "2026-01-01T00:00:00";
        String fim = "2026-01-31T23:59:59";

        when(adminMetricasService.calcularAbandonoCheckout(any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/metricas/abandono-checkout")
                        .param("inicio", inicio)
                        .param("fim", fim))
                .andExpect(status().isOk());

        verify(adminMetricasService).calcularAbandonoCheckout(
                eq(LocalDateTime.parse(inicio)),
                eq(LocalDateTime.parse(fim)));
    }
}
