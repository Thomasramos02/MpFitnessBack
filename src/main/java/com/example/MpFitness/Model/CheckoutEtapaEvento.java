package com.example.MpFitness.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "checkout_etapa_eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutEtapaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 120)
    private String sessionId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false, length = 40)
    private EtapaCheckout etapa;

    @Enumerated(EnumType.STRING)
    @Column(name = "evento", nullable = false, length = 40)
    private TipoEventoCheckout evento;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public enum EtapaCheckout {
        CARRINHO,
        IDENTIFICACAO,
        ENDERECO,
        PAGAMENTO,
        CONFIRMACAO
    }

    public enum TipoEventoCheckout {
        ENTRADA,
        CONCLUSAO,
        ABANDONO
    }
}
