package com.example.MpFitness.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProdutoMaisVistoDTO {
    private Long id;
    private String nome;
    private String categoria;
    private String status;
    private Long visualizacoes;
}
