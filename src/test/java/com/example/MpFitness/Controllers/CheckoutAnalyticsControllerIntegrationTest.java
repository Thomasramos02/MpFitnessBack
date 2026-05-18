package com.example.MpFitness.Controllers;

import com.example.MpFitness.DTO.CheckoutEtapaEventoRequestDTO;
import com.example.MpFitness.Model.CheckoutEtapaEvento.EtapaCheckout;
import com.example.MpFitness.Model.CheckoutEtapaEvento.TipoEventoCheckout;
import com.example.MpFitness.Services.CheckoutAnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutAnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckoutAnalyticsControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CheckoutAnalyticsService checkoutAnalyticsService;

        @Test
        void deveRegistrarEventoDeCheckout() throws Exception {
                CheckoutEtapaEventoRequestDTO payload = new CheckoutEtapaEventoRequestDTO(
                                "sess-12345",
                                99L,
                                EtapaCheckout.PAGAMENTO,
                                TipoEventoCheckout.ENTRADA);

                mockMvc.perform(post("/api/checkout-analytics/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk());

                ArgumentCaptor<CheckoutEtapaEventoRequestDTO> captor = ArgumentCaptor
                                .forClass(CheckoutEtapaEventoRequestDTO.class);
                verify(checkoutAnalyticsService).registrarEvento(captor.capture());

                CheckoutEtapaEventoRequestDTO recebido = captor.getValue();
                assertThat(recebido.getSessionId()).isEqualTo("sess-12345");
                assertThat(recebido.getClienteId()).isEqualTo(99L);
                assertThat(recebido.getEtapa()).isEqualTo(EtapaCheckout.PAGAMENTO);
                assertThat(recebido.getEvento()).isEqualTo(TipoEventoCheckout.ENTRADA);
        }

        @Test
        void deveRetornarBadRequestQuandoPayloadInvalido() throws Exception {
                String payloadInvalido = "{\"sessionId\":\"\",\"etapa\":null,\"evento\":null}";

                mockMvc.perform(post("/api/checkout-analytics/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadInvalido))
                                .andExpect(status().isBadRequest());
        }
}
