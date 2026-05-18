package com.example.MpFitness.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RupturaDTO {
    private Long produtoId;
    private String produtoNome;
    private String categoria;
    private LocalDateTime ocorrencia;
    private Integer diasSemEstoque;

}