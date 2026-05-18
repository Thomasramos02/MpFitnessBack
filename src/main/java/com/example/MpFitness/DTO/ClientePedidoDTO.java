package com.example.MpFitness.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientePedidoDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private EnderecoDTO endereco;
}