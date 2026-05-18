package com.example.MpFitness.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstoqueScheduler {

    private final PedidoService pedidoService;

    /**
     * Roda a cada 1 minuto para verificar se existem pedidos pendentes expirados.
     * Se expirar, o estoque e devolvido e o pedido eh removido do banco.
     */
    @Scheduled(fixedRate = 60000)
    public void verificarReservasExpiradas() {
        log.info("Iniciando verificacao de reservas expiradas...");
        try {
            pedidoService.processarExpiracaoReservas();
        } catch (Exception e) {
            log.error("Erro ao processar expiracao de reservas", e);
        }
    }
}
