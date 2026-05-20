package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Endereco;
import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.Pedido.FormaEntrega;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Services.PedidoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/finalizar/{clienteId}")
    @PreAuthorize("#clienteId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Pedido> finalizarCompra(
            @PathVariable Long clienteId,
            @RequestParam FormaEntrega formaEntrega,
            @RequestBody(required = false) Endereco enderecoEntrega) {
        Pedido pedido = pedidoService.finalizarCompra(clienteId, formaEntrega, enderecoEntrega);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/clientes/{clienteId}/pedidos")
    @PreAuthorize("#clienteId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> listarPedidosDoCliente(@PathVariable Long clienteId) {
        List<Pedido> pedidos = pedidoService.listarPedidosPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{pedidoId}/detalhes")
    @PreAuthorize("@pedidoService.usuarioPodeAcessarPedido(#pedidoId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<?> detalharPedido(@PathVariable Long pedidoId) {
        Optional<Pedido> pedido = pedidoService.buscarPedidoPorId(pedidoId);
        return pedido.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("@pedidoService.usuarioPodeAcessarPedido(#pedidoId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<?> buscarDetalhesPedido(@PathVariable Long pedidoId) {
        return pedidoService.detalharPedido(pedidoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> listarTodosPedidos() {
        List<Pedido> pedidos = pedidoService.listarTodosPedidos();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> listarPedidosAtivos() {
        List<Pedido> pedidos = pedidoService.listarPedidosAtivos();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> listarPedidosPorPeriodo(
            @RequestParam OffsetDateTime inicio,
            @RequestParam OffsetDateTime fim) {
        List<Pedido> pedidos = pedidoService.listarPedidosPorPeriodo(inicio, fim);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/mes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> listarPedidosPorMes(
            @RequestParam int mes,
            @RequestParam int ano) {
        List<Pedido> pedidos = pedidoService.listarPedidosPorMesEAno(mes, ano);
        return ResponseEntity.ok(pedidos);
    }

    @PatchMapping("/{pedidoId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> atualizarStatus(
            @PathVariable Long pedidoId,
            @RequestParam StatusPedido novoStatus) {
        Pedido pedido = pedidoService.atualizarStatusPedido(pedidoId, novoStatus);
        return ResponseEntity.ok(pedido);
    }

    @PatchMapping("/{pedidoId}/rastreamento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> adicionarRastreamento(
            @PathVariable Long pedidoId,
            @RequestParam String codigo) {
        Pedido pedido = pedidoService.adicionarCodigoRastreamento(pedidoId, codigo);
        return ResponseEntity.ok(pedido);
    }

    @PatchMapping("/{pedidoId}/previsao-entrega")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> atualizarPrevisaoEntrega(
            @PathVariable Long pedidoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate novaData) {
        Pedido pedido = pedidoService.atualizarDataEntrega(pedidoId, novaData);
        return ResponseEntity.ok(pedido);
    }

    @PatchMapping("/{pedidoId}/observacoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> atualizarObservacoes(
            @PathVariable Long pedidoId,
            @RequestBody String observacoes) {
        Pedido pedido = pedidoService.atualizarObservacoes(pedidoId, observacoes);
        return ResponseEntity.ok(pedido);
    }

    @DeleteMapping("/{pedidoId}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> cancelarPedido(@PathVariable Long pedidoId) {
        Pedido pedido = pedidoService.cancelarPedido(pedidoId);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/relatorios/vendas-totais")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> calcularVendasTotais(
            @RequestParam OffsetDateTime inicio,
            @RequestParam OffsetDateTime fim) {
        BigDecimal total = pedidoService.calcularTotalVendasPorPeriodo(inicio, fim);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/relatorios/contagem-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> contarPedidosPorStatus(
            @RequestParam StatusPedido status) {
        Long quantidade = pedidoService.contarPedidosPorStatus(status);
        return ResponseEntity.ok(quantidade);
    }
}
