package com.example.MpFitness.Services;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.MpFitness.Model.Carrinho;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.ItemCarrinho;
import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Repositories.CarrinhoRepository;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.Repositories.ProdutoRepository;

import jakarta.transaction.Transactional;

@Service
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
    public Carrinho adicionaCarrinho(Long idProduto, Long idCliente, int quantidade) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Carrinho carrinho = cliente.getCarrinho();
        if (carrinho == null) {
            carrinho = new Carrinho();
            carrinho.setValorTotal(BigDecimal.ZERO);
            carrinho = carrinhoRepository.save(carrinho);
            cliente.setCarrinho(carrinho);
            clienteRepository.save(cliente);
        }

        // esse metodo verifica se o produto já está no carrinho
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
    public Carrinho atualizarQuantidadeItem(Long clienteId, Long produtoId, int quantidadeNova) {
        Carrinho carrinho = carrinhoRepository.findByCliente_Id(clienteId)
                .orElseThrow(
                        () -> new NoSuchElementException("Carrinho não encontrado para o cliente ID: " + clienteId));

        ItemCarrinho itemParaAtualizar = carrinho.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst()
                .orElseThrow(
                        () -> new NoSuchElementException("Produto ID: " + produtoId + " não encontrado no carrinho."));

        itemParaAtualizar.setQuantidade(quantidadeNova);
        return carrinhoRepository.save(carrinho);

    }

    @Transactional
    public Carrinho removerItem(Long clienteId, Long produtoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Carrinho carrinho = cliente.getCarrinho();
        if (carrinho == null) {
            throw new RuntimeException("Carrinho vazio");
        }

        carrinho.getItens().removeIf(item -> item.getProduto().getId().equals(produtoId));
        atualizarValorTotal(carrinho);

        return carrinhoRepository.save(carrinho);
    }

    public Carrinho visualizarCarrinho(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return Optional.ofNullable(cliente.getCarrinho())
                .orElseThrow(() -> new RuntimeException("Carrinho vazio"));
    }

    private void atualizarValorTotal(Carrinho carrinho) {
        BigDecimal total = carrinho.getItens().stream()
                .map(item -> item.getProduto().getValor().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        carrinho.setValorTotal(total);
    }
}
