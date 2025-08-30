package com.example.MpFitness.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carrinhos")
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "carrinho_seq")
    @SequenceGenerator(name = "carrinho_seq", sequenceName = "carrinhos_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @OneToOne(mappedBy = "carrinho", fetch = FetchType.LAZY)
    @JsonIgnore
    private Cliente cliente;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrinho> itens = new ArrayList<>();

    @Column(precision = 15, scale = 2)
    private BigDecimal valorTotal;

}