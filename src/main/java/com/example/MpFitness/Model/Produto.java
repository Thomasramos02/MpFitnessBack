package com.example.MpFitness.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_produto", length = 30, nullable = false)
    private CategoriaEnum categoria = CategoriaEnum.GERAL;

    @Column(name = "em_oferta")
    private Boolean emOferta = false;

    @Column(name = "valor_promocional")
    private BigDecimal valorPromocional;

    @Column(name = "status_produto", length = 20, nullable = false)
    private String status = "ATIVO"; // valores: "ATIVO", "INATIVO" inativo quando estoque for 0

    @Column(name = "visualizacoes", nullable = false)
    private Long visualizacoes = 0L;

    @OneToMany
    @JsonIgnoreProperties({ "itensCombo", "hibernateLazyInitializer", "handler" })
    private List<Produto> itensCombo = new ArrayList<>();

    public BigDecimal getPrecoEfetivo() {
        if (Boolean.TRUE.equals(emOferta)
                && valorPromocional != null
                && valorPromocional.compareTo(BigDecimal.ZERO) > 0
                && valorPromocional.compareTo(valor) < 0) {
            return valorPromocional;
        }

        return valor;
    }

}
