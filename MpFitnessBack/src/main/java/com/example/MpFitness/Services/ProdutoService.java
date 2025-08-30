package com.example.MpFitness.Services;

import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Model.Carrinho;
import com.example.MpFitness.Repositories.CarrinhoRepository;
import com.example.MpFitness.Repositories.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CarrinhoRepository carrinhoRepository;
    
    @Value("${app.upload.dir}")
    private String uploadDir; // pasta absoluta

    @Autowired
    private CloudinaryService cloudinaryService;

    public Produto findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id do produto não pode ser nulo");
        }
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível encontrar o produto"));
    }

    public List<Produto> listaTodosProdutos() {
        return produtoRepository.findAll();
    }

    @Transactional
    public Produto criarProduto(Produto produto) {
        validarProduto(produto);
        configurarItensCombo(produto);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarProduto(Produto produto, Long id) {
        Produto produtoExistente = findById(id);
        int quantidadeAntiga = produtoExistente.getQuantidade();

        atualizarDadosProduto(produtoExistente, produto);
        atualizarItensCombo(produtoExistente, produto);

        validarQuantidadeEstoque(produtoExistente.getQuantidade());
        registrarMudancaEstoque(produtoExistente, quantidadeAntiga, produtoExistente.getQuantidade());

        return produtoRepository.save(produtoExistente);
    }

    public List<Produto> listarProdutosAtivos() {
        return produtoRepository.findByStatus("ATIVO");
    }

    public List<Produto> listarProdutosInativos() {
        return produtoRepository.findByStatus("INATIVO");
    }

    @Transactional
    public void atualizarEstoque(Long produtoId, int quantidade) {
        Produto produto = findById(produtoId);
        int novaQuantidade = produto.getQuantidade() - quantidade;

        validarQuantidadeEstoque(novaQuantidade);
        produto.setQuantidade(novaQuantidade);
        if (novaQuantidade <= 0) {
            produto.setStatus("INATIVO");
        } else {
            produto.setStatus("ATIVO");
        }
        produtoRepository.save(produto);
    }

    @Transactional
    public void atualizarEstoqueLote(Map<Long, Integer> itens) {
        itens.forEach(this::atualizarEstoque);
    }

    public int consultarEstoque(Long produtoId) {
        return findById(produtoId).getQuantidade();
    }

    public boolean verificarEstoque(Long produtoId, int quantidade) {
        return consultarEstoque(produtoId) >= quantidade;
    }

    private void validarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo");
        }
        if ("COMBO".equals(produto.getTipoProduto())
                && (produto.getItensCombo() == null || produto.getItensCombo().isEmpty())) {
            throw new IllegalArgumentException("Combos devem ter itens associados.");
        }
        if (produto.getEmOferta() && produto.getValorPromocional() == null) {
            throw new IllegalArgumentException("Produto em oferta deve ter um valor promocional definido.");
        }
    }

    private void configurarItensCombo(Produto produto) {
        if ("COMBO".equals(produto.getTipoProduto())) {
            produto.getItensCombo().replaceAll(item -> findById(item.getId()));
        }
    }

    private void atualizarDadosProduto(Produto existente, Produto novo) {
        existente.setNome(novo.getNome());
        existente.setDescricao(novo.getDescricao());
        existente.setImg(novo.getImg());
        existente.setQuantidade(novo.getQuantidade());
        existente.setValor(novo.getValor());
        existente.setTamanho(novo.getTamanho());
        existente.setTipoProduto(novo.getTipoProduto());
        existente.setEmOferta(novo.getEmOferta());
        existente.setValorPromocional(novo.getValorPromocional());

        if (novo.getQuantidade() <= 0) {
            existente.setStatus("INATIVO");
        } else {
            existente.setStatus("ATIVO");
        }
    }

    private void atualizarItensCombo(Produto existente, Produto novo) {
        if ("COMBO".equals(novo.getTipoProduto())) {
            existente.getItensCombo().clear();
            if (novo.getItensCombo() != null && !novo.getItensCombo().isEmpty()) {
                novo.getItensCombo().forEach(item -> existente.getItensCombo().add(findById(item.getId())));
            }
        }
    }

    private void validarQuantidadeEstoque(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
        }
    }

    private void registrarMudancaEstoque(Produto produto, int quantidadeAntiga, int novaQuantidade) {
        int diferenca = novaQuantidade - quantidadeAntiga;
        if (diferenca != 0) {
            System.out.printf("Estoque alterado - Produto: %s (ID: %d) de %d para %d%n",
                    produto.getNome(), produto.getId(), quantidadeAntiga, novaQuantidade);
        }
    }
    
     @Transactional
    public String uploadImagem(Long produtoId, MultipartFile file) throws IOException, NoSuchAlgorithmException {
        if (file.isEmpty()) throw new IllegalArgumentException("Nenhum arquivo enviado");

        Produto produto = findById(produtoId);
        String imageUrl = cloudinaryService.uploadFile(file);

        // Atualiza imagem de combos
        if ("COMBO".equals(produto.getTipoProduto())) {
            produto.getItensCombo().forEach(item -> {
                item.setImg(imageUrl);
                produtoRepository.save(item);
            });
        }

        produto.setImg(imageUrl);
        produtoRepository.save(produto);

        return imageUrl;
    }


    @Transactional
    public void deletarProduto(Long id) {
        Produto produto = findById(id);

        // Atualiza a remoção com base em ItemCarrinho
        List<Carrinho> carrinhos = carrinhoRepository.findAll();
        for (Carrinho carrinho : carrinhos) {
            boolean alterado = carrinho.getItens().removeIf(item -> item.getProduto().getId().equals(id));
            if (alterado) {
                carrinhoRepository.save(carrinho);
            }
        }

        produtoRepository.delete(produto);
    }

    // ✅ NOVO MÉTODO: Buscar produtos por categoria
    public List<Produto> findByCategoria(String categoria) {
        return produtoRepository.findByCategoriaAndStatus(categoria, "ATIVO");
    }
}
