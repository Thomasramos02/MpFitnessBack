package com.example.MpFitness.Model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PedidoItem> produtos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER) // Garante que o cliente será carregado
    @JsonIgnoreProperties({ "pedidos", "senha", "carrinho" })
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "valor_produtos", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorProdutos;

    @Column(name = "valor_frete", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "data_compra", nullable = false)
    private OffsetDateTime dataCompra;

    @Column(name = "data_atualizacao")
    private OffsetDateTime dataAtualizacao;

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

    @Column(name = "preferencia_pagamento_id")
    private String preferenciaPagamentoId;

    @Column(name = "preferencia_init_point")
    private String preferenciaInitPoint;

    @Column(name = "preferencia_sandbox_init_point")
    private String preferenciaSandboxInitPoint;

    @Column(name = "pagamento_externo_id")
    private String pagamentoExternoId;

    @Column(name = "data_pagamento_aprovado")
    private OffsetDateTime dataPagamentoAprovado;

    @Column(name = "data_entrega_prevista")
    private OffsetDateTime dataEntregaPrevista;

    @Column(name = "data_expiracao_reserva")
    private OffsetDateTime dataExpiracaoReserva;

    @Embedded
    private Endereco enderecoEntrega;

    public enum FormaEntrega {
        RETIRADA,
        ENTREGA
    }

    public enum StatusPedido {
        PRE_PEDIDO,
        AGUARDANDO_PAGAMENTO,
        PAGO,
        EM_SEPARACAO,
        ENVIADO,
        ENTREGUE,
        CANCELADO,
        RESERVA_EXPIRADA
    }
}
