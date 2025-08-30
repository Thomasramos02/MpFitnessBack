package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Services.PedidoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class MercadoPagoController {

    @Value("${mercado_pago.access_token}")
    private String accessToken;

    private PedidoService pedidoService;

    @PostMapping("/create-preference/{pedidoId}")
    public ResponseEntity<?> criarPreferencia(
            @PathVariable Long pedidoId,
            @RequestBody MercadoPagoRequest request,
            @RequestHeader("Authorization") String authToken) {

        try {
            // Validação do token
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Token inválido ou ausente"));
            }

            // Configura o access token do Mercado Pago
            MercadoPagoConfig.setAccessToken(accessToken);

            // Valida os produtos
            if (request.getProdutos() == null || request.getProdutos().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Nenhum produto no carrinho"));
            }

            List<PreferenceItemRequest> items = new ArrayList<>();

            // Adiciona produtos
            for (ProdutoDTO produto : request.getProdutos()) {
                if (produto.getQuantidade() <= 0 || produto.getValor() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(new ErrorResponse(
                                    "Quantidade ou valor inválido para o produto " + produto.getNome()));
                }

                PreferenceItemRequest item = PreferenceItemRequest.builder()
                        .id(produto.getId().toString())
                        .title(produto.getNome())
                        .description(produto.getDescricao())
                        .pictureUrl(produto.getImg())
                        .categoryId("fitness")
                        .quantity(produto.getQuantidade())
                        .currencyId("BRL")
                        .unitPrice(new BigDecimal(produto.getValor()))
                        .build();
                items.add(item);
            }

            // Adiciona frete se necessário
            if (request.getValorFrete() > 0) {
                items.add(PreferenceItemRequest.builder()
                        .id("frete")
                        .title("Frete - " + request.getFormaEntrega())
                        .quantity(1)
                        .currencyId("BRL")
                        .unitPrice(BigDecimal.valueOf(request.getValorFrete()))
                        .build());
            }

            // Cria a preferência
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(pedidoId.toString())
                    .build();

            Preference preference = new PreferenceClient().create(preferenceRequest);

            return ResponseEntity.ok(new PreferenceResponse(
                    preference.getId(),
                    preference.getInitPoint(),
                    preference.getSandboxInitPoint()));

        } catch (MPApiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erro no Mercado Pago: " + e.getApiResponse().getContent()));
        } catch (MPException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar pagamento: " + e.getMessage()));
        }
    }

    // Link publico do backend
    @RestController
    @RequestMapping("/api/payments")
    public class WebhookController {

        @PostMapping("/webhook")
        public ResponseEntity<Void> receberWebhook(@RequestBody Map<String, Object> payload) {
            try {
                String tipoEvento = (String) payload.get("type");

                if ("payment".equals(tipoEvento)) {
                    Long pagamentoId = Long.valueOf(((Map<String, Object>) payload.get("data")).get("id").toString());

                    // Consultar o pagamento pelo ID
                    MercadoPagoConfig.setAccessToken("SEU_ACCESS_TOKEN");

                    PaymentClient paymentClient = new PaymentClient();
                    Payment pagamento = paymentClient.get(pagamentoId);

                    if ("approved".equalsIgnoreCase(pagamento.getStatus())) {
                        Long pedidoId = Long.parseLong(pagamento.getExternalReference());
                        // Atualize o status do pedido aqui
                        pedidoService.atualizarStatusPedido(pedidoId, StatusPedido.PAGO);
                    }
                }

                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    // Classes DTO internas
    @Data
    static class MercadoPagoRequest {
        private List<ProdutoDTO> produtos;
        private String formaEntrega;
        private double valorFrete;
    }

    @Data
    static class ProdutoDTO {
        private Long id;
        private String nome;
        private String descricao;
        private String img;
        private Double valor;
        private Integer quantidade;
    }

    @Data
    static class PreferenceResponse {
        private String id;
        private String initPoint;
        private String sandboxInitPoint;

        public PreferenceResponse(String id, String initPoint, String sandboxInitPoint) {
            this.id = id;
            this.initPoint = initPoint;
            this.sandboxInitPoint = sandboxInitPoint;
        }
    }

    @Data
    static class ErrorResponse {
        private String message;
        private LocalDateTime timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }
}