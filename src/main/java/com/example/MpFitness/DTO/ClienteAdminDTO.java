package com.example.MpFitness.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClienteAdminDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String role;
    private EnderecoDTO endereco;
}