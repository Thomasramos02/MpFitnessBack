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
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // CORRIGIDO: usando dataCompra
    @Query("SELECT p FROM Pedido p WHERE MONTH(p.dataCompra) = :mes AND YEAR(p.dataCompra) = :ano")
    List<Pedido> findByMesEAno(@Param("mes") int mes, @Param("ano") int ano);

    // CORRIGIDO: usando dataCompra
    List<Pedido> findByDataCompraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId")
    List<Pedido> findByClienteId(@Param("clienteId") Long clienteId);

    // CORRIGIDO: usando statusPedido
    @Query("SELECT p FROM Pedido p WHERE p.statusPedido <> 'CANCELADO'")
    List<Pedido> findAllAtivos();

    // CORRIGIDO: usando statusPedido
    @Query("SELECT p FROM Pedido p WHERE p.statusPedido = :status")
    List<Pedido> findByStatus(@Param("status") StatusPedido status);

    // CORRIGIDO: usando dataCompra e statusPedido
    @Query("SELECT p FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim AND p.statusPedido = :status")
    List<Pedido> findByPeriodoAndStatus(@Param("inicio") LocalDateTime inicio, 
                                      @Param("fim") LocalDateTime fim, 
                                      @Param("status") StatusPedido status);

    // CORRIGIDO: usando dataCompra
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim")
    Long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // CORRIGIDO: usando dataCompra e statusPedido
    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim AND p.statusPedido <> 'CANCELADO'")
    Double calcularTotalVendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // CORRIGIDO: usando statusPedido
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.statusPedido = :status")
    Long countByStatus(@Param("status") StatusPedido status);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.nome LIKE %:nomeCliente%")
    List<Pedido> findByNomeClienteContaining(@Param("nomeCliente") String nomeCliente);

    // CORRIGIDO: usando dataCompra e statusPedido
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataCompra BETWEEN :inicio AND :fim AND p.statusPedido = :status")
    Long countByPeriodoAndStatus(@Param("inicio") LocalDateTime inicio, 
                                @Param("fim") LocalDateTime fim, 
                                @Param("status") StatusPedido status);
}