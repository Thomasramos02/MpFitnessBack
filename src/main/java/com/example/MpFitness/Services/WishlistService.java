package com.example.MpFitness.Services;

import com.example.MpFitness.DTO.ProdutoClienteDTO;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Model.WishlistItem;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.Repositories.ProdutoRepository;
import com.example.MpFitness.Repositories.WishlistItemRepository;
import com.example.MpFitness.exceptions.ClienteNaoEncontradoException;
import com.example.MpFitness.exceptions.ProdutoNaoEncontradoException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public List<ProdutoClienteDTO> listar(Long clienteId) {
        return wishlistItemRepository.findByClienteIdOrderByCriadoEmDesc(clienteId).stream()
                .map(item -> toProdutoClienteDTO(item.getProduto()))
                .toList();
    }

    @Transactional
    public List<ProdutoClienteDTO> adicionar(Long clienteId, Long produtoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(produtoId));

        wishlistItemRepository.findByClienteIdAndProdutoId(clienteId, produtoId)
                .orElseGet(() -> {
                    WishlistItem item = new WishlistItem();
                    item.setCliente(cliente);
                    item.setProduto(produto);
                    item.setCriadoEm(LocalDateTime.now());
                    return wishlistItemRepository.save(item);
                });

        return listar(clienteId);
    }

    @Transactional
    public List<ProdutoClienteDTO> remover(Long clienteId, Long produtoId) {
        wishlistItemRepository.deleteByClienteIdAndProdutoId(clienteId, produtoId);
        return listar(clienteId);
    }

    private ProdutoClienteDTO toProdutoClienteDTO(Produto produto) {
        return new ProdutoClienteDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getImg(),
                produto.getCategoria().getDisplayName(),
                produto.getValor(),
                produto.getValorPromocional(),
                produto.getVisualizacoes() == null ? 0L : produto.getVisualizacoes());
    }
}
