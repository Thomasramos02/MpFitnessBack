package com.example.MpFitness.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(name = "nome_cliente", length = 100, nullable = false)
    private String nome;

    @Column(name = "email_cliente", length = 100, nullable = false, unique = true)
    private String email;

    //Permite que a senha seja nula para logins via OAuth2
    @Column(name = "senha_cliente", length = 60, nullable = true)
    private String senha;

    // Google
    @Column(name = "telefone_cliente", length = 15, nullable = true)
    private String telefone;

    @Embedded
    private Endereco endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.CLIENTE;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "carrinho_id", referencedColumnName = "id")
    private Carrinho carrinho;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();

    public enum Role {
        ADMIN,
        CLIENTE
    }
}