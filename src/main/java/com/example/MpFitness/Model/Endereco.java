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
    /*Ná variavel String cep eu acho que uma boa decisão seria usar @Size(min = 8, max = 8) já que isso preveniria o usuário de colocar um cep que existem mais caracteres que o permitido exemplo: Se um usuário digitar o cep dessa forma 11111-111 é um formato válido pois existe 8 números + o hífen que da 9 caracteres, se não especificar a quantidade permitida o usuário pode sem querer cometendo um erro e digitando um número com mais caracteres 11111-1111*/
    private String cep;

    private String pais;
}
