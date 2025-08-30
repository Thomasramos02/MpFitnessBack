package com.example.MpFitness.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.MpFitness.Model.Endereco;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PedidoDetalhadoDTO {
    private Long id;
    private String status;
    private LocalDateTime dataCompra;
    private String formaEntrega;
    private BigDecimal valorTotal;
    private List<ProdutoResumoDTO> produtos;
    private Endereco enderecoEntrega;
    private String observacoes;
    private String codigoRastreamento;
    private LocalDateTime dataEntregaPrevista;
}
