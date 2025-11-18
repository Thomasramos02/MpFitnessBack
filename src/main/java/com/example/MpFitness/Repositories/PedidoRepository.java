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

    @Query("SELECT p FROM Pedido p WHERE MONTH(p.dataCompra) = :mes AND YEAR(p.dataCompra) = :ano")
    List<Pedido> findByMesEAno(int mes, int ano);

    List<Pedido> findByDataCompraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId")
    List<Pedido> findByClienteId(Long clienteId);

    @Query("SELECT p FROM Pedido p WHERE p.statusPedido <> 'CANCELADO'")
    List<Pedido> findAllAtivos();

    @Query("SELECT p FROM Pedido p WHERE p.statusPedido = :status")
    List<Pedido> findByStatus(Pedido.StatusPedido status);

    // Novos métodos para relatórios
    // Métodos para relatórios (mantendo seus nomes)
    @Query("SELECT p FROM Pedido p WHERE p.dataCriacao BETWEEN :inicio AND :fim AND p.status = :status")
    List<Pedido> findByPeriodoAndStatus(@Param("inicio") LocalDateTime inicio, 
                                      @Param("fim") LocalDateTime fim, 
                                      @Param("status") StatusPedido status);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataCriacao BETWEEN :inicio AND :fim")
    Long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.dataCriacao BETWEEN :inicio AND :fim AND p.status <> 'CANCELADO'")
    Double calcularTotalVendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.status = :status")
    Long countByStatus(@Param("status") StatusPedido status);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.nome LIKE %:nomeCliente%")
    List<Pedido> findByNomeClienteContaining(@Param("nomeCliente") String nomeCliente);

    // Método adicional para contar pedidos por status em um período
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataCriacao BETWEEN :inicio AND :fim AND p.status = :status")
    Long countByPeriodoAndStatus(@Param("inicio") LocalDateTime inicio, 
                                @Param("fim") LocalDateTime fim, 
                                @Param("status") StatusPedido status);
}