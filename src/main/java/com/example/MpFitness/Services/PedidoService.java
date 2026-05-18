package com.example.MpFitness.Services;

import com.example.MpFitness.DTO.PedidoDetalhadoDTO;
import com.example.MpFitness.DTO.ClientePedidoDTO;
import com.example.MpFitness.DTO.EnderecoDTO;
import com.example.MpFitness.DTO.ProdutoResumoDTO;
import com.example.MpFitness.Model.Carrinho;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Endereco;
import com.example.MpFitness.Model.ItemCarrinho;
import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.Pedido.FormaEntrega;
import com.example.MpFitness.Model.Pedido.StatusPedido;
import com.example.MpFitness.Model.PedidoItem;
import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Repositories.CarrinhoRepository;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.Repositories.PedidoRepository;
import com.example.MpFitness.Repositories.ProdutoRepository;
import com.example.MpFitness.exceptions.CarrinhoVazioException;
import com.example.MpFitness.exceptions.ClienteNaoEncontradoException;
import com.example.MpFitness.exceptions.EstoqueInsuficienteException;
import com.example.MpFitness.exceptions.PedidoJaCanceladoException;
import com.example.MpFitness.exceptions.PedidoNaoEncontradoException;
import com.example.MpFitness.Services.OrderConfirmationEmailService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final FreteService freteService;
    private final OrderConfirmationEmailService orderConfirmationEmailService;

    @Transactional
    public Pedido finalizarCompra(Long clienteId, FormaEntrega formaEntrega, Endereco enderecoEntrega) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

        Carrinho carrinho = cliente.getCarrinho();
        if (carrinho == null || carrinho.getItens().isEmpty()) {
            throw new CarrinhoVazioException("Carrinho esta vazio");
        }

        BigDecimal valorProdutos = calcularTotalItens(carrinho);
        BigDecimal valorFrete = freteService.calcularFrete(
                enderecoEntrega != null ? enderecoEntrega.getCep() : null,
                carrinho.getItens().stream().map(ItemCarrinho::getProduto).toList(),
                formaEntrega);

        validarEstoque(carrinho);

        Pedido pedido = criarPedido(cliente, carrinho, formaEntrega, enderecoEntrega, valorProdutos, valorFrete);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        limparCarrinhoEAtualizarEstoque(carrinho);

        return pedidoSalvo;
    }

    public List<Pedido> listarTodosPedidos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPedidosAtivos() {
        return pedidoRepository.findAllAtivos();
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

    public Pedido buscarPedidoPorIdObrigatorio(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    public boolean usuarioPodeAcessarPedido(Long pedidoId, Long usuarioId) {
        return pedidoRepository.findById(pedidoId)
                .map(pedido -> pedido.getCliente() != null
                        && pedido.getCliente().getId() != null
                        && pedido.getCliente().getId().equals(usuarioId))
                .orElse(false);
    }

    @Transactional
    public Pedido atualizarStatusPedido(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = buscarPedidoPorIdObrigatorio(pedidoId);
        pedido.setStatusPedido(novoStatus);
        pedido.setDataAtualizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido adicionarCodigoRastreamento(Long pedidoId, String codigoRastreamento) {
        Pedido pedido = buscarPedidoPorIdObrigatorio(pedidoId);
        pedido.setCodigoRastreamento(codigoRastreamento);
        pedido.setDataAtualizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizarDataEntrega(Long pedidoId, LocalDateTime dataEntregaPrevista) {
        Pedido pedido = buscarPedidoPorIdObrigatorio(pedidoId);
        pedido.setDataEntregaPrevista(dataEntregaPrevista);
        pedido.setDataAtualizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizarDataEntrega(Long pedidoId, LocalDate novaData) {
        return atualizarDataEntrega(pedidoId, novaData.atStartOfDay());
    }

    @Transactional
    public Pedido atualizarObservacoes(Long pedidoId, String observacoes) {
        Pedido pedido = buscarPedidoPorIdObrigatorio(pedidoId);
        pedido.setObservacoes(observacoes);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long pedidoId) {
        Pedido pedido = buscarPedidoPorIdObrigatorio(pedidoId);

        if (pedido.getStatusPedido() == StatusPedido.CANCELADO) {
            throw new PedidoJaCanceladoException();
        }

        devolverProdutosAoEstoque(pedido);
        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedido.setObservacoes("Pedido cancelado pelo sistema em " + LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void processarExpiracaoReservas() {
        LocalDateTime now = LocalDateTime.now();
        List<Pedido> expirados = pedidoRepository.findExpiredPrePedidos(now);

        for (Pedido pedido : expirados) {
            devolverProdutosAoEstoque(pedido);
            pedidoRepository.delete(pedido);
        }
    }

    private BigDecimal calcularTotalItens(Carrinho carrinho) {
        return carrinho.getItens().stream()
                .map(item -> item.getProduto().getPrecoEfetivo().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarEstoque(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = item.getProduto();
            if (produto.getQuantidade() < item.getQuantidade()) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para o produto: " + produto.getNome());
            }
        }
    }

    private Pedido criarPedido(Cliente cliente, Carrinho carrinho, FormaEntrega formaEntrega,
            Endereco enderecoEntrega, BigDecimal valorProdutos, BigDecimal valorFrete) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProdutos(criarSnapshotsDoPedido(pedido, carrinho));
        pedido.setValorProdutos(valorProdutos);
        pedido.setValorFrete(valorFrete);
        pedido.setValorTotal(valorProdutos.add(valorFrete));
        pedido.setDataCompra(LocalDateTime.now());
        pedido.setFormaEntrega(formaEntrega);
        pedido.setDataExpiracaoReserva(LocalDateTime.now().plusMinutes(15));

        if (formaEntrega == FormaEntrega.ENTREGA) {
            pedido.setStatusPedido(StatusPedido.PRE_PEDIDO);
            pedido.setEnderecoEntrega(enderecoEntrega != null ? enderecoEntrega : cliente.getEndereco());
        } else {
            pedido.setStatusPedido(StatusPedido.AGUARDANDO_PAGAMENTO);
        }

        return pedido;
    }

    private List<PedidoItem> criarSnapshotsDoPedido(Pedido pedido, Carrinho carrinho) {
        List<PedidoItem> produtos = new ArrayList<>();

        for (ItemCarrinho itemCarrinho : carrinho.getItens()) {
            Produto produto = itemCarrinho.getProduto();
            PedidoItem pedidoItem = new PedidoItem();
            pedidoItem.setPedido(pedido);
            pedidoItem.setProdutoId(produto.getId());
            pedidoItem.setNomeProduto(produto.getNome());
            pedidoItem.setDescricaoProduto(produto.getDescricao());
            pedidoItem.setImgProduto(produto.getImg());
            pedidoItem.setTamanhoProduto(produto.getTamanho());
            pedidoItem.setValorUnitario(produto.getPrecoEfetivo());
            pedidoItem.setValorOriginalUnitario(produto.getValor());
            pedidoItem.setQuantidade(itemCarrinho.getQuantidade());
            produtos.add(pedidoItem);
        }

        return produtos;
    }

    private void limparCarrinhoEAtualizarEstoque(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
            produto.setStatus(produto.getQuantidade() <= 0 ? "INATIVO" : "ATIVO");
            produtoRepository.save(produto);
        }

        carrinho.getItens().clear();
        carrinho.setValorTotal(BigDecimal.ZERO);
        carrinhoRepository.save(carrinho);
    }

    private void devolverProdutosAoEstoque(Pedido pedido) {
        pedido.getProdutos().forEach(item -> produtoRepository.findById(item.getProdutoId()).ifPresent(produto -> {
            produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
            produto.setStatus(produto.getQuantidade() <= 0 ? "INATIVO" : "ATIVO");
            produtoRepository.save(produto);
        }));
    }

    public BigDecimal calcularTotalVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.findByDataCompraBetween(inicio, fim).stream()
                .map(Pedido::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long contarPedidosPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status).stream().count();
    }

    public Optional<PedidoDetalhadoDTO> detalharPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId).map(pedido -> {
            List<ProdutoResumoDTO> produtos = pedido.getProdutos().stream()
                    .map(item -> new ProdutoResumoDTO(
                            item.getProdutoId(),
                            item.getNomeProduto(),
                            item.getValorUnitario(),
                            item.getQuantidade(),
                            item.getImgProduto()))
                    .toList();

            ClientePedidoDTO cliente = null;
            if (pedido.getCliente() != null) {
                EnderecoDTO enderecoCliente = null;
                if (pedido.getCliente().getEndereco() != null) {
                    enderecoCliente = new EnderecoDTO();
                    enderecoCliente.setCep(pedido.getCliente().getEndereco().getCep());
                    enderecoCliente.setRua(pedido.getCliente().getEndereco().getRua());
                    enderecoCliente.setNumero(pedido.getCliente().getEndereco().getNumero());
                    enderecoCliente.setComplemento(pedido.getCliente().getEndereco().getComplemento());
                    enderecoCliente.setBairro(pedido.getCliente().getEndereco().getBairro());
                    enderecoCliente.setCidade(pedido.getCliente().getEndereco().getCidade());
                    enderecoCliente.setEstado(pedido.getCliente().getEndereco().getEstado());
                }

                cliente = new ClientePedidoDTO(
                        pedido.getCliente().getId(),
                        pedido.getCliente().getNome(),
                        pedido.getCliente().getEmail(),
                        pedido.getCliente().getTelefone(),
                        enderecoCliente);
            }

            return new PedidoDetalhadoDTO(
                    pedido.getId(),
                    pedido.getStatusPedido().name(),
                    pedido.getDataCompra(),
                    pedido.getFormaEntrega().name(),
                    pedido.getValorTotal(),
                    produtos,
                    pedido.getEnderecoEntrega(),
                    cliente,
                    pedido.getObservacoes(),
                    pedido.getCodigoRastreamento(),
                    pedido.getDataEntregaPrevista(),
                    pedido.getPreferenciaPagamentoId(),
                    pedido.getPreferenciaInitPoint(),
                    pedido.getPreferenciaSandboxInitPoint(),
                    pedido.getDataExpiracaoReserva());
        });
    }

    @Transactional
    public Pedido registrarPreferenciaPagamento(Long pedidoId, String preferenciaId, String initPoint,
            String sandboxInitPoint) {
        Pedido pedido = buscarPedidoPorIdObrigatorio(pedidoId);
        pedido.setPreferenciaPagamentoId(preferenciaId);
        pedido.setPreferenciaInitPoint(initPoint);
        pedido.setPreferenciaSandboxInitPoint(sandboxInitPoint);
        if (pedido.getDataExpiracaoReserva() == null) {
            pedido.setDataExpiracaoReserva(LocalDateTime.now().plusMinutes(15));
        }
        pedido.setDataAtualizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido registrarPagamentoAprovado(Long pedidoId, String pagamentoExternoId, BigDecimal valorPago,
            OffsetDateTime dataAprovacao) {
        Pedido pedido = buscarPedidoPorIdObrigatorio(pedidoId);

        if (pedido.getStatusPedido() == StatusPedido.PAGO) {
            return pedido;
        }

        // Aceita pagamento para PRE_PEDIDO ou AGUARDANDO_PAGAMENTO
        if (pedido.getStatusPedido() != StatusPedido.PRE_PEDIDO &&
                pedido.getStatusPedido() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new IllegalArgumentException(
                    "Pedido em status " + pedido.getStatusPedido() + " nao pode ser aprovado");
        }

        if (valorPago == null || valorPago.compareTo(pedido.getValorTotal()) != 0) {
            throw new IllegalArgumentException("Valor recebido difere do total do pedido");
        }

        pedido.setPagamentoExternoId(pagamentoExternoId);
        pedido.setDataPagamentoAprovado(dataAprovacao != null ? dataAprovacao.toLocalDateTime() : LocalDateTime.now());
        pedido.setStatusPedido(StatusPedido.PAGO);
        pedido.setDataExpiracaoReserva(null); // Limpa reserva ao pagar
        pedido.setPreferenciaPagamentoId(null);
        pedido.setPreferenciaInitPoint(null);
        pedido.setPreferenciaSandboxInitPoint(null);
        pedido.setDataAtualizacao(LocalDateTime.now());
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Enviar email de confirmação de pedido
        try {
            orderConfirmationEmailService.sendOrderConfirmationEmail(pedidoSalvo);
        } catch (Exception e) {
            // Log error but don't fail the payment registration
            System.err.println("Erro ao enviar email de confirmação: " + e.getMessage());
            e.printStackTrace();
        }

        return pedidoSalvo;
    }
}
