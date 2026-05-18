package com.example.MpFitness.Controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Services.FreteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fretes")
@RequiredArgsConstructor
public class FreteController {

    private final FreteService freteService;

    public static class FreteRequest {
        public String cepDestino;
        public Integer itemsCount;
        public String formaEntrega; // optional: RETIRADA or ENVIO
    }

    public static class FreteResponse {
        public BigDecimal valorFrete;

        public FreteResponse(BigDecimal valorFrete) {
            this.valorFrete = valorFrete;
        }
    }

    @PostMapping("/calc")
    public ResponseEntity<?> calcular(@RequestBody FreteRequest req) {
        // Validar que CEP foi fornecido
        if (req.cepDestino == null || req.cepDestino.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CEP não fornecido"));
        }

        String cep = req.cepDestino.replaceAll("\\D", "");

        // Validar que CEP tem números
        if (cep.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CEP inválido"));
        }

        int count = req.itemsCount == null ? 0 : req.itemsCount;

        List<Produto> produtos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            produtos.add(new Produto());
        }

        Pedido.FormaEntrega forma = Pedido.FormaEntrega.ENTREGA;
        if ("RETIRADA".equalsIgnoreCase(req.formaEntrega)) {
            forma = Pedido.FormaEntrega.RETIRADA;
        }

        BigDecimal valor = freteService.calcularFrete(cep, produtos, forma);

        return ResponseEntity.ok(new FreteResponse(valor));
    }
}
