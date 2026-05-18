package com.example.MpFitness.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProdutoClienteDTO {
    private Long id;
    private String nome;
    private String descricao;
    private String img;
    private String categoria;
    private BigDecimal valor;
    private BigDecimal valorPromocional;
    private Long visualizacoes;
}
