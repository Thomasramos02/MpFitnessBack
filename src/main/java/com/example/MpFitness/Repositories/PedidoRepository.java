package com.example.MpFitness.Repositories;

import com.example.MpFitness.DTO.RupturaDTO;
import com.example.MpFitness.Model.Pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    interface ClienteRecorrenteProjection {
        Long getClienteId();

        String getNomeCliente();

        Long getTotalPedidos();

        java.math.BigDecimal getTotalGasto();
    }

    @Query("SELECT p FROM Pedido p WHERE MONTH(p.dataCompra) = :mes AND YEAR(p.dataCompra) = :ano")
    List<Pedido> findByMesEAno(int mes, int ano);

    List<Pedido> findByDataCompraBetween(OffsetDateTime inicio, OffsetDateTime fim);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId")
    List<Pedido> findByClienteId(Long clienteId);

    @Query("SELECT p FROM Pedido p WHERE p.statusPedido <> 'CANCELADO'")
    List<Pedido> findAllAtivos();

    @Query("SELECT p FROM Pedido p WHERE p.statusPedido IN ('PRE_PEDIDO', 'AGUARDANDO_PAGAMENTO') AND p.dataExpiracaoReserva < :now")
    List<Pedido> findExpiredPrePedidos(OffsetDateTime now);

    @Query("SELECT p FROM Pedido p WHERE p.statusPedido = :status")
    List<Pedido> findByStatus(Pedido.StatusPedido status);

    @Query("""
            SELECT p.cliente.id AS clienteId,
                   p.cliente.nome AS nomeCliente,
                   COUNT(p) AS totalPedidos,
                   SUM(p.valorTotal) AS totalGasto
            FROM Pedido p
            WHERE p.statusPedido <> 'CANCELADO'
            GROUP BY p.cliente.id, p.cliente.nome
            HAVING COUNT(p) > 1
            ORDER BY COUNT(p) DESC
            """)
    List<ClienteRecorrenteProjection> findClientesRecorrentes();

    @Query("SELECT COUNT(DISTINCT p.cliente.id) FROM Pedido p WHERE p.statusPedido <> 'CANCELADO'")
    Long countClientesComPedidosValidos();
}