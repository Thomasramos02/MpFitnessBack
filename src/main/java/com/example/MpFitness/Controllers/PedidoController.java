
package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.*;
import com.example.MpFitness.Model.Pedido.FormaEntrega;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Services.PedidoService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // ========== ENDPOINTS PÚBLICOS/CLIENTE ==========

    @PostMapping("/finalizar/{clienteId}")
    public ResponseEntity<Pedido> finalizarCompra(
            @PathVariable Long clienteId,
            @RequestParam FormaEntrega formaEntrega,
            @RequestBody(required = false) Endereco enderecoEntrega) {

        try {
            Pedido pedido = pedidoService.finalizarCompra(clienteId, formaEntrega, enderecoEntrega);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/clientes/{clienteId}/pedidos")
    @PreAuthorize("#clienteId == authentication.principal.id")
    public ResponseEntity<?> listarPedidosDoCliente(@PathVariable Long clienteId) {
        try {
            List<Pedido> pedidos = pedidoService.listarPedidosPorCliente(clienteId);
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao buscar pedidos: " + e.getMessage());
        }
    }

    @GetMapping("/{pedidoId}/detalhes")
    @PreAuthorize("@pedidoService.buscarPedidoPorId(#pedidoId).get().cliente.id == authentication.principal.id")
    public ResponseEntity<?> detalharPedido(@PathVariable Long pedidoId) {
        try {
            Optional<Pedido> pedido = pedidoService.buscarPedidoPorId(pedidoId);
            return pedido.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao buscar pedido: " + e.getMessage());
        }
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("@pedidoService.buscarPedidoPorId(#pedidoId).get().cliente.id == authentication.principal.id")
    public ResponseEntity<?> buscarDetalhesPedido(@PathVariable Long pedidoId) {
        try {
            return pedidoService.detalharPedido(pedidoId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao detalhar pedido: " + e.getMessage());
        }
    }

    // ========== ENDPOINTS ADMINISTRATIVOS ==========

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
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
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
        try {
            Pedido pedido = pedidoService.atualizarStatusPedido(pedidoId, novoStatus);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{pedidoId}/rastreamento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> adicionarRastreamento(
            @PathVariable Long pedidoId,
            @RequestParam String codigo) {
        try {
            Pedido pedido = pedidoService.adicionarCodigoRastreamento(pedidoId, codigo);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{pedidoId}/previsao-entrega")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> atualizarPrevisaoEntrega(
            @PathVariable Long pedidoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate novaData) {
        try {
            Pedido pedido = pedidoService.atualizarDataEntrega(pedidoId, novaData);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{pedidoId}/observacoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> atualizarObservacoes(
            @PathVariable Long pedidoId,
            @RequestBody String observacoes) {
        try {
            Pedido pedido = pedidoService.atualizarObservacoes(pedidoId, observacoes);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{pedidoId}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pedido> cancelarPedido(@PathVariable Long pedidoId) {
        try {
            Pedido pedido = pedidoService.cancelarPedido(pedidoId);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ========== RELATÓRIOS ==========

    @GetMapping("/relatorios/vendas-totais")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> calcularVendasTotais(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
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