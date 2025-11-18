package com.example.MpFitness.Repositories;

import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface relatorioRepository extends JpaRepository<Pedido, Long> {

    // Relatórios por período
    @Query("SELECT p FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim")
    List<Pedido> findPedidosPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT p FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim AND p.statusPedido = :status")
    List<Pedido> findPedidosPorPeriodoEStatus(@Param("inicio") LocalDateTime inicio, 
                                            @Param("fim") LocalDateTime fim, 
                                            @Param("status") StatusPedido status);

    // Relatórios mensais
    @Query("SELECT p FROM Pedido p WHERE MONTH(p.dataCompra) = :mes AND YEAR(p.dataCompra) = :ano")
    List<Pedido> findPedidosPorMesEAno(@Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT p FROM Pedido p WHERE MONTH(p.dataCompra) = :mes AND YEAR(p.dataCompra) = :ano AND p.statusPedido = :status")
    List<Pedido> findPedidosPorMesEAnoEStatus(@Param("mes") int mes, @Param("ano") int ano, @Param("status") StatusPedido status);

    // Estatísticas e totais
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim")
    Long countPedidosPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim AND p.statusPedido <> 'CANCELADO'")
    Double calcularTotalVendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim AND p.statusPedido = :status")
    Long countPedidosPorPeriodoEStatus(@Param("inicio") LocalDateTime inicio, 
                                     @Param("fim") LocalDateTime fim, 
                                     @Param("status") StatusPedido status);

    // Relatórios por status
    @Query("SELECT p FROM Pedido p WHERE p.statusPedido = :status")
    List<Pedido> findPedidosPorStatus(@Param("status") StatusPedido status);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.statusPedido = :status")
    Long countPedidosPorStatus(@Param("status") StatusPedido status);

    // Relatórios por cliente
    @Query("SELECT p FROM Pedido p WHERE p.cliente.nome LIKE %:nomeCliente%")
    List<Pedido> findPedidosPorNomeCliente(@Param("nomeCliente") String nomeCliente);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId")
    List<Pedido> findPedidosPorClienteId(@Param("clienteId") Long clienteId);

    // Métodos para dashboard
    @Query("SELECT p.statusPedido, COUNT(p) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim GROUP BY p.statusPedido")
    List<Object[]> countPedidosPorStatusNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT p.statusPedido, SUM(p.valorTotal) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim AND p.statusPedido <> 'CANCELADO' GROUP BY p.statusPedido")
    List<Object[]> sumValorTotalPorStatusNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}