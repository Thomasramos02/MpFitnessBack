package com.example.MpFitness.Model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Endereco {

    private String rua;

    private String numero;

    private String complemento;

    private String bairro;

    private String cidade;

    private String estado;

    private String cep;

    private String pais;
}
