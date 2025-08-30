package com.example.MpFitness.Services;

import com.example.MpFitness.DTO.PedidoDetalhadoDTO;
import com.example.MpFitness.DTO.ProdutoResumoDTO;
import com.example.MpFitness.Model.*;
import com.example.MpFitness.Model.Pedido.FormaEntrega;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final FreteService freteService;

    @Transactional
    public Pedido finalizarCompra(Long clienteId, FormaEntrega formaEntrega, Endereco enderecoEntrega) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Carrinho carrinho = cliente.getCarrinho();
        if (carrinho == null || carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho está vazio");
        }

        // Cálculos
        BigDecimal valorProdutos = calcularTotalItens(carrinho);
        BigDecimal valorFrete = freteService.calcularFrete(
                enderecoEntrega != null ? enderecoEntrega.getCep() : null,
                carrinho.getItens().stream().map(ItemCarrinho::getProduto).collect(Collectors.toList()),
                formaEntrega);

        validarEstoque(carrinho);

        Pedido pedido = criarPedido(cliente, carrinho, formaEntrega, enderecoEntrega, valorProdutos, valorFrete);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        limparCarrinhoEAtualizarEstoque(carrinho);

        return pedidoSalvo;
    }

    // Métodos de consulta
    public List<Pedido> listarTodosPedidos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPedidosAtivos() {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatusPedido() != StatusPedido.CANCELADO)
                .collect(Collectors.toList());
    }

    public List<Pedido> listarPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    public List<Pedido> listarPedidosPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.findByDataCompraBetween(inicio, fim);
    }

    public List<Pedido> listarPedidosPorMesEAno(int mes, int ano) {
        return pedidoRepository.findByMesEAno(mes, ano);
    }

    public Optional<Pedido> buscarPedidoPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    // Atualizações
    @Transactional
    public Pedido atualizarStatusPedido(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatusPedido(novoStatus);
        pedido.setDataAtualizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido adicionarCodigoRastreamento(Long pedidoId, String codigoRastreamento) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setCodigoRastreamento(codigoRastreamento);
        pedido.setDataAtualizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizarDataEntrega(Long pedidoId, LocalDateTime dataEntregaPrevista) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setDataEntregaPrevista(dataEntregaPrevista);
        pedido.setDataAtualizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizarDataEntrega(Long pedidoId, LocalDate novaData) {
        LocalDateTime dataEntregaPrevista = novaData.atStartOfDay();

        return this.atualizarDataEntrega(pedidoId, dataEntregaPrevista);
    }

    @Transactional
    public Pedido atualizarObservacoes(Long pedidoId, String observacoes) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        // Supondo que sua entidade Pedido tenha o método setObservacoes
        pedido.setObservacoes(observacoes);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatusPedido() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido já está cancelado");
        }

        devolverProdutosAoEstoque(pedido);
        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedido.setObservacoes("Pedido cancelado pelo sistema em " + LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    // Auxiliares
    private BigDecimal calcularTotalItens(Carrinho carrinho) {
        return carrinho.getItens().stream()
                .map(item -> item.getProduto().getValor().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarEstoque(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = item.getProduto();
            if (produto.getQuantidade() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }
        }
    }

    private Pedido criarPedido(Cliente cliente, Carrinho carrinho, FormaEntrega formaEntrega,
            Endereco enderecoEntrega, BigDecimal valorProdutos, BigDecimal valorFrete) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);

        // Adicionar os produtos com repetição de acordo com a quantidade
        List<Produto> produtos = new ArrayList<>();
        for (ItemCarrinho item : carrinho.getItens()) {
            for (int i = 0; i < item.getQuantidade(); i++) {
                produtos.add(item.getProduto());
            }
        }

        pedido.setProdutos(produtos);
        pedido.setValorTotal(valorProdutos.add(valorFrete));
        pedido.setDataCompra(LocalDateTime.now());
        pedido.setFormaEntrega(formaEntrega);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_PAGAMENTO);

        if (formaEntrega == FormaEntrega.ENTREGA) {
            pedido.setEnderecoEntrega(enderecoEntrega != null ? enderecoEntrega : cliente.getEndereco());
        }

        return pedido;
    }

    private void limparCarrinhoEAtualizarEstoque(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
            produtoRepository.save(produto);
        }

        carrinho.getItens().clear();
        carrinho.setValorTotal(BigDecimal.ZERO);
        carrinhoRepository.save(carrinho);
    }

    private void devolverProdutosAoEstoque(Pedido pedido) {
        Map<Produto, Long> produtosComQuantidade = pedido.getProdutos().stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));

        produtosComQuantidade.forEach((produto, quantidade) -> {
            produto.setQuantidade(produto.getQuantidade() + quantidade.intValue());
            produtoRepository.save(produto);
        });
    }

    // Relatórios
    public BigDecimal calcularTotalVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Pedido> pedidos = pedidoRepository.findByDataCompraBetween(inicio, fim);
        return pedidos.stream()
                .map(Pedido::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long contarPedidosPorStatus(StatusPedido status) {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatusPedido() == status)
                .count();
    }

    // detalhes de pedido
    public Optional<PedidoDetalhadoDTO> detalharPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId).map(pedido -> {
            Map<Produto, Long> contagem = pedido.getProdutos().stream()
                    .collect(Collectors.groupingBy(p -> p, Collectors.counting()));

            List<ProdutoResumoDTO> produtos = contagem.entrySet().stream()
                    .map(entry -> new ProdutoResumoDTO(
                            entry.getKey().getNome(),
                            entry.getKey().getValor(),
                            entry.getValue().intValue()))
                    .toList();

            return new PedidoDetalhadoDTO(
                    pedido.getId(),
                    pedido.getStatusPedido().name(),
                    pedido.getDataCompra(),
                    pedido.getFormaEntrega().name(),
                    pedido.getValorTotal(),
                    produtos,
                    pedido.getEnderecoEntrega(),
                    pedido.getObservacoes(),
                    pedido.getCodigoRastreamento(),
                    pedido.getDataEntregaPrevista());
        });
    }

}
