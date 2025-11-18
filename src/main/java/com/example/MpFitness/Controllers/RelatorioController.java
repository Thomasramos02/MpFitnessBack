package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Services.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/pedidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> relatorioPedidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) String cliente) {
        
        List<Pedido> pedidos = relatorioService.gerarRelatorioPedidos(dataInicio, dataFim, status, cliente);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/financeiro")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> relatorioFinanceiro(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        Map<String, Object> relatorio = relatorioService.gerarRelatorioFinanceiro(dataInicio, dataFim);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/consolidado-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> relatorioConsolidadoStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        Map<String, Object> relatorio = relatorioService.gerarRelatorioConsolidadoStatus(dataInicio, dataFim);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        Map<String, Object> dashboard = relatorioService.gerarDashboard(dataInicio, dataFim);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/mensal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> relatorioMensal(
            @RequestParam int mes,
            @RequestParam int ano) {
        
        Map<String, Object> relatorio = relatorioService.gerarRelatorioMensal(mes, ano);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/vendas-totais")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Double> vendasTotais(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        Double total = relatorioService.calcularVendasTotaisPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/contagem-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> contagemPorStatus(@RequestParam StatusPedido status) {
        Long quantidade = relatorioService.contarPedidosPorStatus(status);
        return ResponseEntity.ok(quantidade);
    }

    @GetMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> relatorioPorCliente(@RequestParam String nomeCliente) {
        List<Pedido> pedidos = relatorioService.buscarPedidosPorCliente(nomeCliente);
        return ResponseEntity.ok(pedidos);
    }
}