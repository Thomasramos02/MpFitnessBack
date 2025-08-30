package com.example.MpFitness.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Produtos")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(name = "nome_produto", nullable = false, length = 150)
    private String nome;

    @Column(name = "descricao_produto", length = 500)
    private String descricao;

    @Column(name = "valor_produto", length = 10, nullable = false)
    private BigDecimal valor;

    @Column(name = "img_produto", length = 2000)
    private String img;

    @Column(name = "quant_produto", nullable = false)
    private int quantidade;

    @Column(name = "tam_produto")
    private String tamanho;

    @Column(name = "tipo_produto", length = 20)
    private String tipoProduto = "UNICO"; // valores possíveis: "UNICO", "COMBO"

    @Column(name = "categoria_produto", length = 50)
    private String categoria = "Geral"; // valores: "Equipamentos", "Suplementos", "Acessórios", "Roupas", "Calçados",
                                        // etc.

    @Column(name = "em_oferta")
    private Boolean emOferta = false;

    @Column(name = "valor_promocional")
    private BigDecimal valorPromocional;

    @Column(name = "status_produto", length = 20, nullable = false)
    private String status = "ATIVO"; // valores: "ATIVO", "INATIVO" inativo quando estoque for 0

    @OneToMany
    @JsonIgnoreProperties({ "itensCombo", "hibernateLazyInitializer", "handler" })
    private List<Produto> itensCombo = new ArrayList<>();

}