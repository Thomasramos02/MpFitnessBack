package com.example.MpFitness.DTO;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecorrenciaDetalhadaDTO {
    private long totalClientesComPedidos;
    private long totalClientesRecorrentes;
    private double taxaRecorrencia;
    private List<ClienteRecorrenteDTO> clientesRecorrentes;

    @Data
    @AllArgsConstructor
    public static class ClienteRecorrenteDTO {
        private Long clienteId;
        private String nomeCliente;
        private Long totalPedidos;
        private BigDecimal totalGasto;
    }
}
