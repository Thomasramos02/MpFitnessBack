package com.example.MpFitness.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutAbandonoEtapaDTO {
    private String etapa;
    private long entradas;
    private long conclusoes;
    private long abandonos;
    private double taxaAbandono;
}
