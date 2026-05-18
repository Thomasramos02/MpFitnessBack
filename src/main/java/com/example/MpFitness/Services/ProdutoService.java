package com.example.MpFitness.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.MpFitness.Model.Carrinho;
import com.example.MpFitness.Model.CategoriaEnum;
import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Repositories.CarrinhoRepository;
import com.example.MpFitness.Repositories.ProdutoRepository;
import com.example.MpFitness.exceptions.FalhaProcessamentoImagemException;
import com.example.MpFitness.exceptions.ProdutoNaoEncontradoException;
import com.example.MpFitness.exceptions.RegraDeProdutoException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProdutoService {

    private static final String CLOUDINARY_FOLDER = "mpfitness/produtos";
    private static final String CLOUDINARY_RESOURCE_TYPE = "image";

    private final ProdutoRepository produtoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private record StoredCloudinaryImage(String url, String publicId) {
    }

    public Produto findById(Long id) {
        if (id == null) {
            throw new ProdutoNaoEncontradoException("Id do produto não pode ser nulo");
        }
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
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
        String imagemAnterior = produtoExistente.getImg();
        int quantidadeAntiga = produtoExistente.getQuantidade();

        atualizarDadosProduto(produtoExistente, produto);
        atualizarItensCombo(produtoExistente, produto);

        validarQuantidadeEstoque(produtoExistente.getQuantidade());
        registrarMudancaEstoque(produtoExistente, quantidadeAntiga, produtoExistente.getQuantidade());

        Produto salvo = produtoRepository.save(produtoExistente);
        removerImagemCloudinarySeNecessario(imagemAnterior, salvo.getImg());
        return salvo;
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

    @Transactional
    public void registrarVisualizacao(Long produtoId) {
        Produto produto = findById(produtoId);
        long visualizacoesAtuais = produto.getVisualizacoes() == null ? 0L : produto.getVisualizacoes();
        produto.setVisualizacoes(visualizacoesAtuais + 1);
        produtoRepository.save(produto);
    }

    public List<Produto> listarProdutosMaisVistos(int limite) {
        if (limite <= 10) {
            return produtoRepository.findTop10ByStatusOrderByVisualizacoesDesc("ATIVO");
        }

        return produtoRepository.findTop20ByStatusOrderByVisualizacoesDesc("ATIVO");
    }

    private void validarProduto(Produto produto) {
        if (produto == null) {
            throw new RegraDeProdutoException("O produto não pode ser nulo");
        }
        if ("COMBO".equals(produto.getTipoProduto())
                && (produto.getItensCombo() == null || produto.getItensCombo().isEmpty())) {
            throw new RegraDeProdutoException("Combos devem ter itens associados.");
        }
        if (produto.getEmOferta() && produto.getValorPromocional() == null) {
            throw new RegraDeProdutoException("Produto em oferta deve ter um valor promocional definido.");
        }
        if (produto.getCategoria() == null) {
            produto.setCategoria(CategoriaEnum.GERAL);
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
        existente.setImg(normalizarImagem(novo.getImg()));
        existente.setQuantidade(novo.getQuantidade());
        existente.setValor(novo.getValor());
        existente.setTamanho(novo.getTamanho());
        existente.setTipoProduto(novo.getTipoProduto());
        existente.setCategoria(novo.getCategoria() == null ? CategoriaEnum.GERAL : novo.getCategoria());
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
            throw new RegraDeProdutoException("Quantidade em estoque não pode ser negativa");
        }
    }

    private void registrarMudancaEstoque(Produto produto, int quantidadeAntiga, int novaQuantidade) {
        int diferenca = novaQuantidade - quantidadeAntiga;
        if (diferenca != 0) {
            // Mudança de estoque registrada silenciosamente
        }
    }

    @Transactional
    public Map<String, Object> uploadImagem(Long produtoId, MultipartFile file) {
        Produto produto = findById(produtoId);
        String imagemAnterior = produto.getImg();
        String caminhoCompleto = processarImagem(file);

        produto.setImg(caminhoCompleto);
        produtoRepository.save(produto);
        removerImagemCloudinarySeNecessario(imagemAnterior, caminhoCompleto);

        return deserializarImagem(caminhoCompleto);
    }

    private String processarImagem(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", CLOUDINARY_FOLDER,
                    "resource_type", CLOUDINARY_RESOURCE_TYPE,
                    "overwrite", true,
                    "transformation", new Transformation().width(1200).crop("limit").quality("auto")));

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            if (secureUrl == null || secureUrl.isBlank()) {
                throw new FalhaProcessamentoImagemException("Falha ao processar a imagem do produto");
            }

            return serializarImagem(secureUrl, publicId);
        } catch (Exception exception) {
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                throw new FalhaProcessamentoImagemException(
                        "Falha ao processar a imagem do produto: " + exception.getClass().getSimpleName());
            }

            throw new FalhaProcessamentoImagemException("Falha ao processar a imagem do produto: " + message);
        }
    }

    private String normalizarImagem(String imagem) {
        if (imagem == null || imagem.isBlank()) {
            return null;
        }

        return imagem.trim();
    }

    private String serializarImagem(String url, String publicId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("url", url);
            payload.put("publicId", publicId);
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                throw new FalhaProcessamentoImagemException("Falha ao processar a imagem do produto");
            }

            throw new FalhaProcessamentoImagemException("Falha ao processar a imagem do produto: " + message);
        }
    }

    private Map<String, Object> deserializarImagem(String imagem) {
        try {
            return objectMapper.readValue(imagem, new TypeReference<>() {
            });
        } catch (Exception exception) {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("url", imagem);
            resposta.put("publicId", null);
            return resposta;
        }
    }

    private StoredCloudinaryImage parseStoredImage(String imagem) {
        if (imagem == null || imagem.isBlank()) {
            return null;
        }

        String trimmed = imagem.trim();
        if (!trimmed.startsWith("{")) {
            return null;
        }

        try {
            Map<String, String> payload = objectMapper.readValue(trimmed, new TypeReference<>() {
            });
            String url = payload.get("url");
            if (url == null || url.isBlank()) {
                return null;
            }

            String publicId = payload.get("publicId");
            return new StoredCloudinaryImage(url.trim(),
                    publicId == null || publicId.isBlank() ? null : publicId.trim());
        } catch (Exception exception) {
            return null;
        }
    }

    private void removerImagemCloudinarySeNecessario(String imagemAnterior, String imagemNova) {
        StoredCloudinaryImage imagemAntiga = parseStoredImage(imagemAnterior);
        StoredCloudinaryImage imagemAtual = parseStoredImage(imagemNova);

        if (imagemAntiga == null || imagemAntiga.publicId() == null) {
            return;
        }

        if (imagemAtual != null && imagemAntiga.publicId().equals(imagemAtual.publicId())) {
            return;
        }

        try {
            cloudinary.uploader().destroy(imagemAntiga.publicId(), ObjectUtils.asMap(
                    "resource_type", CLOUDINARY_RESOURCE_TYPE));
        } catch (Exception exception) {
            // Falha ao remover imagem antiga - não afeta a operação atual
        }
    }

    @Transactional
    public void deletarProduto(Long id) {
        Produto produto = findById(id);
        String imagemProduto = produto.getImg();

        List<Carrinho> carrinhos = carrinhoRepository.findAll();
        for (Carrinho carrinho : carrinhos) {
            boolean alterado = carrinho.getItens().removeIf(item -> item.getProduto().getId().equals(id));
            if (alterado) {
                carrinhoRepository.save(carrinho);
            }
        }

        produtoRepository.delete(produto);
        removerImagemCloudinarySeNecessario(imagemProduto, null);
    }

    @Transactional
    public void removerImagemProduto(Long produtoId, String publicId) {
        Produto produto = findById(produtoId);
        String imagemAtual = produto.getImg();

        // Remover do Cloudinary
        removerImagemCloudinarySeNecessario(imagemAtual, publicId);

        // Limpar a imagem do produto no banco
        produto.setImg(null);
        produtoRepository.save(produto);
    }

    public List<Produto> findByCategoria(CategoriaEnum categoria) {
        return produtoRepository.findByCategoriaAndStatus(categoria, "ATIVO");
    }
}
