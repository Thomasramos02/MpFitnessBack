package com.example.MpFitness.Services;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.MpFitness.Model.Carrinho;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.ItemCarrinho;
import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Repositories.CarrinhoRepository;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.Repositories.ProdutoRepository;
import com.example.MpFitness.exceptions.CarrinhoVazioException;
import com.example.MpFitness.exceptions.ClienteNaoEncontradoException;
import com.example.MpFitness.exceptions.QuantidadeInvalidaException;
import com.example.MpFitness.exceptions.ProdutoNaoEncontradoException;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public Carrinho adicionarProdutoAoCarrinho(Long idProduto, Long idCliente, int quantidade) {
        validarQuantidade(quantidade);

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ClienteNaoEncontradoException(idCliente));
        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(idProduto));

        Carrinho carrinho = cliente.getCarrinho();
        if (carrinho == null) {
            carrinho = new Carrinho();
            carrinho.setValorTotal(BigDecimal.ZERO);
            carrinho = carrinhoRepository.save(carrinho);
            cliente.setCarrinho(carrinho);
            clienteRepository.save(cliente);
        }

        Optional<ItemCarrinho> itemExistente = carrinho.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produto.getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrinho item = itemExistente.get();
            item.setQuantidade(item.getQuantidade() + quantidade);
        } else {
            ItemCarrinho novoItem = new ItemCarrinho();
            novoItem.setProduto(produto);
            novoItem.setQuantidade(quantidade);
            novoItem.setCarrinho(carrinho);
            carrinho.getItens().add(novoItem);
        }

        atualizarValorTotal(carrinho);
        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public Carrinho adicionaCarrinho(Long idProduto, Long idCliente, int quantidade) {
        return adicionarProdutoAoCarrinho(idProduto, idCliente, quantidade);
    }

    @Transactional
    public Carrinho atualizarQuantidadeItem(Long clienteId, Long produtoId, int quantidadeNova) {
        validarQuantidade(quantidadeNova);

        Carrinho carrinho = carrinhoRepository.findByCliente_Id(clienteId)
                .orElseThrow(CarrinhoVazioException::new);

        ItemCarrinho itemParaAtualizar = carrinho.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst()
                .orElseThrow(() -> new ProdutoNaoEncontradoException(produtoId));

        itemParaAtualizar.setQuantidade(quantidadeNova);
        atualizarValorTotal(carrinho);
        return carrinhoRepository.save(carrinho);

    }

    @Transactional
    public Carrinho removerItem(Long clienteId, Long produtoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

        Carrinho carrinho = cliente.getCarrinho();
        if (carrinho == null) {
            throw new CarrinhoVazioException();
        }

        carrinho.getItens().removeIf(item -> item.getProduto().getId().equals(produtoId));
        atualizarValorTotal(carrinho);

        return carrinhoRepository.save(carrinho);
    }

    public Carrinho visualizarCarrinho(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

        return Optional.ofNullable(cliente.getCarrinho())
                .orElseThrow(CarrinhoVazioException::new);
    }

    private void atualizarValorTotal(Carrinho carrinho) {
        BigDecimal total = carrinho.getItens().stream()
                .map(item -> item.getProduto().getPrecoEfetivo().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        carrinho.setValorTotal(total);
    }

    private void validarQuantidade(int quantidade) {
        if (quantidade < 1) {
            throw new QuantidadeInvalidaException("Quantidade deve ser pelo menos 1");
        }
    }
}
