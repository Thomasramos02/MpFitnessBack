package com.example.MpFitness.Services;

import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Repositories.relatorioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final relatorioRepository relatorioRepository;

    // Relatório básico de pedidos
    public List<Pedido> gerarRelatorioPedidos(LocalDate dataInicio, LocalDate dataFim, StatusPedido status, String nomeCliente) {
        if (dataInicio != null && dataFim != null) {
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
            
            if (status != null) {
                return relatorioRepository.findPedidosPorPeriodoEStatus(inicio, fim, status);
            } else {
                return relatorioRepository.findPedidosPorPeriodo(inicio, fim);
            }
        } else if (status != null) {
            return relatorioRepository.findPedidosPorStatus(status);
        } else {
            return relatorioRepository.findAll();
        }
    }

    // Relatório financeiro detalhado
    public Map<String, Object> gerarRelatorioFinanceiro(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
        
        List<Pedido> pedidos = relatorioRepository.findPedidosPorPeriodo(inicio, fim);
        Double totalVendas = relatorioRepository.calcularTotalVendasPorPeriodo(inicio, fim);
        Long totalPedidos = relatorioRepository.countPedidosPorPeriodo(inicio, fim);
        
        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("pedidos", pedidos);
        relatorio.put("totalVendas", totalVendas != null ? totalVendas : 0.0);
        relatorio.put("totalPedidos", totalPedidos);
        relatorio.put("pedidosCancelados", relatorioRepository.countPedidosPorPeriodoEStatus(inicio, fim, StatusPedido.CANCELADO));
        relatorio.put("pedidosEntregues", relatorioRepository.countPedidosPorPeriodoEStatus(inicio, fim, StatusPedido.ENTREGUE));
        
        return relatorio;
    }

    // Relatório consolidado por status
    public Map<String, Object> gerarRelatorioConsolidadoStatus(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
        
        Map<String, Object> relatorio = new HashMap<>();
        
        for (StatusPedido status : StatusPedido.values()) {
            List<Pedido> pedidosStatus = relatorioRepository.findPedidosPorPeriodoEStatus(inicio, fim, status);
            
            double totalStatus = 0.0;
            for (Pedido pedido : pedidosStatus) {
                if (pedido.getValorTotal() != null) {
                    totalStatus += pedido.getValorTotal().doubleValue();
                }
            }
            
            Map<String, Object> infoStatus = new HashMap<>();
            infoStatus.put("quantidade", pedidosStatus.size());
            infoStatus.put("valorTotal", totalStatus);
            infoStatus.put("pedidos", pedidosStatus);
            
            relatorio.put(status.toString(), infoStatus);
        }
        
        return relatorio;
    }

    // Dashboard de relatórios
    public Map<String, Object> gerarDashboard(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
        
        Map<String, Object> dashboard = new HashMap<>();
        
        // Estatísticas básicas
        dashboard.put("totalPedidos", relatorioRepository.countPedidosPorPeriodo(inicio, fim));
        dashboard.put("totalVendas", relatorioRepository.calcularTotalVendasPorPeriodo(inicio, fim));
        
        // Pedidos por status
        Map<String, Long> pedidosPorStatus = new HashMap<>();
        Map<String, Double> vendasPorStatus = new HashMap<>();
        
        for (StatusPedido status : StatusPedido.values()) {
            Long quantidade = relatorioRepository.countPedidosPorPeriodoEStatus(inicio, fim, status);
            pedidosPorStatus.put(status.toString(), quantidade);
            
            List<Pedido> pedidos = relatorioRepository.findPedidosPorPeriodoEStatus(inicio, fim, status);
            double total = pedidos.stream()
                    .mapToDouble(p -> p.getValorTotal() != null ? p.getValorTotal().doubleValue() : 0.0)
                    .sum();
            vendasPorStatus.put(status.toString(), total);
        }
        
        dashboard.put("pedidosPorStatus", pedidosPorStatus);
        dashboard.put("vendasPorStatus", vendasPorStatus);
        
        // Últimos pedidos
        List<Pedido> ultimosPedidos = relatorioRepository.findPedidosPorPeriodo(inicio, fim);
        dashboard.put("ultimosPedidos", ultimosPedidos);
        
        return dashboard;
    }

    // Relatório mensal
    public Map<String, Object> gerarRelatorioMensal(int mes, int ano) {
        List<Pedido> pedidos = relatorioRepository.findPedidosPorMesEAno(mes, ano);
        
        double totalVendas = pedidos.stream()
                .filter(p -> p.getStatusPedido() != StatusPedido.CANCELADO)
                .mapToDouble(p -> p.getValorTotal() != null ? p.getValorTotal().doubleValue() : 0.0)
                .sum();
        
        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("mes", mes);
        relatorio.put("ano", ano);
        relatorio.put("pedidos", pedidos);
        relatorio.put("totalPedidos", pedidos.size());
        relatorio.put("totalVendas", totalVendas);
        relatorio.put("pedidosCancelados", pedidos.stream().filter(p -> p.getStatusPedido() == StatusPedido.CANCELADO).count());
        
        return relatorio;
    }

    // Métodos auxiliares
    public Double calcularVendasTotaisPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
        Double total = relatorioRepository.calcularTotalVendasPorPeriodo(inicio, fim);
        return total != null ? total : 0.0;
    }

    public Long contarPedidosPorStatus(StatusPedido status) {
        return relatorioRepository.countPedidosPorStatus(status);
    }

    public List<Pedido> buscarPedidosPorCliente(String nomeCliente) {
        return relatorioRepository.findPedidosPorNomeCliente(nomeCliente);
    }
}