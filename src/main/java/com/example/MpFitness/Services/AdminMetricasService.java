package com.example.MpFitness.Services;

import com.example.MpFitness.DTO.ClienteAdminDTO;
import com.example.MpFitness.DTO.CheckoutAbandonoEtapaDTO;
import com.example.MpFitness.DTO.EnderecoDTO;
import com.example.MpFitness.DTO.ProdutoMaisVistoDTO;
import com.example.MpFitness.DTO.RecorrenciaDetalhadaDTO;
import com.example.MpFitness.DTO.RupturaDTO;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.Repositories.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminMetricasService {

        private final ProdutoService produtoService;
        private final ClienteRepository clienteRepository;
        private final PedidoRepository pedidoRepository;
        private final CheckoutAnalyticsService checkoutAnalyticsService;

        public List<ProdutoMaisVistoDTO> listarProdutosMaisVistos(int limite) {
                List<Produto> produtos = produtoService.listarProdutosMaisVistos(limite);
                return produtos.stream()
                                .map(produto -> new ProdutoMaisVistoDTO(
                                                produto.getId(),
                                                produto.getNome(),
                                                produto.getCategoria().getDisplayName(),
                                                produto.getStatus(),
                                                produto.getVisualizacoes() == null ? 0L : produto.getVisualizacoes()))
                                .toList();
        }

        public List<CheckoutAbandonoEtapaDTO> calcularAbandonoCheckout(LocalDateTime inicio, LocalDateTime fim) {
                return checkoutAnalyticsService.calcularAbandonoPorEtapa(inicio, fim);
        }

        public List<ClienteAdminDTO> listarTodosClientes() {
                return clienteRepository.findAll().stream()
                                .map(this::toClienteAdminDTO)
                                .toList();
        }

        public RecorrenciaDetalhadaDTO calcularRecorrenciaDetalhada() {
                long totalClientesComPedidos = pedidoRepository.countClientesComPedidosValidos();

                List<RecorrenciaDetalhadaDTO.ClienteRecorrenteDTO> clientesRecorrentes = pedidoRepository
                                .findClientesRecorrentes()
                                .stream()
                                .map(item -> new RecorrenciaDetalhadaDTO.ClienteRecorrenteDTO(
                                                item.getClienteId(),
                                                item.getNomeCliente(),
                                                item.getTotalPedidos(),
                                                item.getTotalGasto() == null ? BigDecimal.ZERO : item.getTotalGasto()))
                                .toList();

                long totalClientesRecorrentes = clientesRecorrentes.size();
                double taxaRecorrencia = totalClientesComPedidos <= 0
                                ? 0.0
                                : (totalClientesRecorrentes * 100.0) / totalClientesComPedidos;

                return new RecorrenciaDetalhadaDTO(
                                totalClientesComPedidos,
                                totalClientesRecorrentes,
                                Math.round(taxaRecorrencia * 100.0) / 100.0,
                                clientesRecorrentes);
        }

        public List<RupturaDTO> listarRupturas(LocalDateTime inicio, LocalDateTime fim, int limite) {
                LocalDateTime ocorrencia = fim == null ? LocalDateTime.now() : fim;

                return produtoService.listaTodosProdutos().stream()
                                .filter(produto -> produto.getQuantidade() <= 0
                                                || "INATIVO".equalsIgnoreCase(produto.getStatus()))
                                .sorted(Comparator.comparingInt(Produto::getQuantidade)
                                                .thenComparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                                .limit(Math.max(1, limite))
                                .map(produto -> new RupturaDTO(
                                                produto.getId(),
                                                produto.getNome(),
                                                produto.getCategoria().getDisplayName(),
                                                ocorrencia,
                                                0)) // Temporarily hardcoded until a proper 'days' calculation is implemented
                                .toList();
        }

        private ClienteAdminDTO toClienteAdminDTO(Cliente cliente) {
                EnderecoDTO enderecoDTO = null;

                if (cliente.getEndereco() != null) {
                        enderecoDTO = new EnderecoDTO();
                        enderecoDTO.setCep(cliente.getEndereco().getCep());
                        enderecoDTO.setRua(cliente.getEndereco().getRua());
                        enderecoDTO.setNumero(cliente.getEndereco().getNumero());
                        enderecoDTO.setComplemento(cliente.getEndereco().getComplemento());
                        enderecoDTO.setBairro(cliente.getEndereco().getBairro());
                        enderecoDTO.setCidade(cliente.getEndereco().getCidade());
                        enderecoDTO.setEstado(cliente.getEndereco().getEstado());
                }

                return new ClienteAdminDTO(
                                cliente.getId(),
                                cliente.getNome(),
                                cliente.getEmail(),
                                cliente.getTelefone(),
                                cliente.getRole() != null ? cliente.getRole().name() : null,
                                enderecoDTO);
        }

}
