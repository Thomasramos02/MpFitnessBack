package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Model.PedidoItem;
import com.example.MpFitness.Security.AuthenticatedUser;
import com.example.MpFitness.Services.PedidoService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final PedidoService pedidoService;

    @Value("${mercado_pago.webhook_secret:}")
    private String webhookSecret;

    @PostMapping("/create-preference/{pedidoId}")
    public ResponseEntity<?> criarPreferencia(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestBody(required = false) Map<String, Object> ignoredRequest) {

        Pedido pedido = pedidoService.buscarPedidoPorIdObrigatorio(pedidoId);
        validarAcessoAoPedido(pedido, currentUser);

        if (pedido.getStatusPedido() != StatusPedido.AGUARDANDO_PAGAMENTO &&
                pedido.getStatusPedido() != StatusPedido.PRE_PEDIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pedido nao pode mais gerar preferencia de pagamento");
        }

        try {
            // Habilitar Pix e Cartão - configura os itens da preferência
            PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                    .items(montarItensDaPreferencia(pedido))
                    .externalReference(pedido.getId().toString());

            if (pedido.getDataExpiracaoReserva() != null) {
                builder.expires(true)
                        .expirationDateTo(pedido.getDataExpiracaoReserva().atOffset(java.time.ZoneOffset.of("-03:00")));
            }

            PreferenceRequest preferenceRequest = builder.build();

            Preference preference = new PreferenceClient().create(preferenceRequest);
            pedidoService.registrarPreferenciaPagamento(
                    pedidoId,
                    preference.getId(),
                    preference.getInitPoint(),
                    preference.getSandboxInitPoint());

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

    @PostMapping("/webhook")
    public ResponseEntity<?> receberWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam Map<String, String> queryParams,
            @RequestBody(required = false) Map<String, Object> payload) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook do Mercado Pago recebido sem segredo configurado");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse("Webhook secret nao configurado"));
        }

        String dataIdUrl = queryParams.get("data.id");
        if (!assinaturaWebhookValida(xSignature, xRequestId, dataIdUrl)) {
            log.warn("Assinatura de webhook invalida");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Assinatura do webhook invalida"));
        }

        try {
            if (!ehEventoDePagamento(payload)) {
                return ResponseEntity.ok().build();
            }

            String pagamentoId = extrairPagamentoId(payload, dataIdUrl);
            if (pagamentoId == null || pagamentoId.isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Pagamento nao informado no webhook"));
            }

            Payment pagamento = new PaymentClient().get(Long.valueOf(pagamentoId));
            if (!"approved".equalsIgnoreCase(pagamento.getStatus())) {
                return ResponseEntity.ok().build();
            }

            if (pagamento.getExternalReference() == null || pagamento.getExternalReference().isBlank()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(new ErrorResponse("Pagamento aprovado sem referencia de pedido"));
            }

            Long pedidoId = Long.valueOf(pagamento.getExternalReference());
            pedidoService.registrarPagamentoAprovado(
                    pedidoId,
                    String.valueOf(pagamento.getId()),
                    pagamento.getTransactionAmount(),
                    pagamento.getDateApproved());

            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Identificador invalido no webhook"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (MPApiException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("Falha ao consultar pagamento no Mercado Pago"));
        } catch (MPException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("Erro de comunicacao com Mercado Pago"));
        }
    }

    private void validarAcessoAoPedido(Pedido pedido, AuthenticatedUser currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado");
        }

        if (!isAdmin(currentUser) && !pedido.getCliente().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido nao pertence ao usuario autenticado");
        }
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    private List<PreferenceItemRequest> montarItensDaPreferencia(Pedido pedido) {
        List<PreferenceItemRequest> items = new ArrayList<>();

        for (PedidoItem produto : pedido.getProdutos()) {
            items.add(PreferenceItemRequest.builder()
                    .id(String.valueOf(produto.getProdutoId()))
                    .title(produto.getNomeProduto())
                    .description(produto.getDescricaoProduto())
                    .pictureUrl(produto.getImgProduto())
                    .categoryId("fitness")
                    .quantity(produto.getQuantidade())
                    .currencyId("BRL")
                    .unitPrice(produto.getValorUnitario())
                    .build());
        }

        if (pedido.getValorFrete() != null && pedido.getValorFrete().signum() > 0) {
            items.add(PreferenceItemRequest.builder()
                    .id("frete")
                    .title("Frete - " + pedido.getFormaEntrega())
                    .quantity(1)
                    .currencyId("BRL")
                    .unitPrice(pedido.getValorFrete())
                    .build());
        }

        return items;
    }

    private boolean ehEventoDePagamento(Map<String, Object> payload) {
        return payload != null && "payment".equals(String.valueOf(payload.get("type")));
    }

    @SuppressWarnings("unchecked")
    private String extrairPagamentoId(Map<String, Object> payload, String dataIdUrl) {
        if (dataIdUrl != null && !dataIdUrl.isBlank()) {
            return dataIdUrl;
        }

        if (payload == null) {
            return null;
        }

        Object data = payload.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object id = ((Map<String, Object>) dataMap).get("id");
            if (id != null) {
                return String.valueOf(id);
            }
        }

        return null;
    }

    private boolean assinaturaWebhookValida(String xSignature, String xRequestId, String dataIdUrl) {
        if (xSignature == null || xSignature.isBlank()
                || xRequestId == null || xRequestId.isBlank()
                || dataIdUrl == null || dataIdUrl.isBlank()) {
            return false;
        }

        Map<String, String> partes = extrairPartesDaAssinatura(xSignature);
        String ts = partes.get("ts");
        String hash = partes.get("v1");

        if (ts == null || ts.isBlank() || hash == null || hash.isBlank()) {
            return false;
        }

        String manifest = "id:" + dataIdUrl.toLowerCase(Locale.ROOT)
                + ";request-id:" + xRequestId
                + ";ts:" + ts + ";";

        String assinaturaEsperada = gerarHashWebhook(manifest, webhookSecret);
        return MessageDigest.isEqual(
                assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                hash.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, String> extrairPartesDaAssinatura(String xSignature) {
        String ts = null;
        String hash = null;

        for (String part : xSignature.split(",")) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if ("ts".equals(key)) {
                ts = value;
            } else if ("v1".equals(key)) {
                hash = value;
            }
        }

        return Map.of(
                "ts", ts == null ? "" : ts,
                "v1", hash == null ? "" : hash);
    }

    private String gerarHashWebhook(String manifest, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Nao foi possivel validar a assinatura do webhook", e);
        }
    }

    @Data
    static class PreferenceResponse {
        private final String id;
        private final String initPoint;
        private final String sandboxInitPoint;
    }

    @Data
    static class ErrorResponse {
        private final String message;
        private final LocalDateTime timestamp = LocalDateTime.now();
    }
}
