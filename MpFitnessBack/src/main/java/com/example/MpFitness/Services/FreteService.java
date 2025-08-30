package com.example.MpFitness.Services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.Produto;

@Service
public class FreteService {

    private static final String CEP_ORIGEM = "35650000";

    public BigDecimal calcularFrete(String cepDestino, List<Produto> produtos, Pedido.FormaEntrega formaEntrega) {
        if (formaEntrega == Pedido.FormaEntrega.RETIRADA) {
            return BigDecimal.ZERO;
        }
        char regiao = cepDestino.charAt(0);

        // 2. Tabela de preços por região (em reais)
        Map<Character, BigDecimal> tabelaFrete = Map.of(
                '0', new BigDecimal("30.00"), // Norte
                '1', new BigDecimal("25.00"), // Nordeste
                '2', new BigDecimal("20.00"), // Rio de Janeiro
                '3', new BigDecimal("18.00"), // Minas Gerais
                '4', new BigDecimal("12.00"), // São Paulo
                '8', new BigDecimal("22.00"), // Paraná/Santa Catarina
                '9', new BigDecimal("25.00") // Rio Grande do Sul
        );

        // 3. Valor base + acréscimo por item
        BigDecimal valorBase = tabelaFrete.getOrDefault(regiao, new BigDecimal("30.00"));
        BigDecimal acrescimoPorItem = new BigDecimal("2.50");
        BigDecimal totalItens = new BigDecimal(produtos.size());

        return valorBase.add(acrescimoPorItem.multiply(totalItens));
    }
}