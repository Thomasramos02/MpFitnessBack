package com.example.MpFitness.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToMany
    @JoinTable(name = "pedido_produtos", joinColumns = @JoinColumn(name = "pedido_id"), inverseJoinColumns = @JoinColumn(name = "produto_id"))
    @JsonIgnoreProperties({ "itensCombo", "hibernateLazyInitializer", "handler" })
    private List<Produto> produtos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER) // Garante que o cliente será carregado
    @JsonIgnoreProperties({ "pedidos", "senha", "carrinho" })
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "data_compra", nullable = false)
    private LocalDateTime dataCompra;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pedido", nullable = false)
    private StatusPedido statusPedido = StatusPedido.AGUARDANDO_PAGAMENTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_entrega", nullable = false)
    private FormaEntrega formaEntrega;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "codigo_rastreamento")
    private String codigoRastreamento;

    @Column(name = "data_entrega_prevista")
    private LocalDateTime dataEntregaPrevista;

    @Embedded
    private Endereco enderecoEntrega;

    public enum FormaEntrega {
        RETIRADA,
        ENTREGA
    }

    public enum StatusPedido {
        AGUARDANDO_PAGAMENTO,
        PAGO,
        EM_SEPARACAO,
        ENVIADO,
        ENTREGUE,
        CANCELADO
    }
}