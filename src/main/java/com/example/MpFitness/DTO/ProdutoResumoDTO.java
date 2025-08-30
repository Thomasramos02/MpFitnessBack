package com.example.MpFitness.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProdutoResumoDTO {
    private String nome;
    private BigDecimal valor;
    private int quantidade;
}
