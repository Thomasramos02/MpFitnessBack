package com.example.MpFitness.Repositories;

import com.example.MpFitness.Model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}