package com.example.MpFitness.Config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Model.CategoriaEnum;
import com.example.MpFitness.Repositories.ProdutoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductInitializer implements CommandLineRunner {

        private static final String PLACEHOLDER_IMAGE = "https://placehold.co/800x800/png?text=MP+Fitness";

        private final ProdutoRepository produtoRepository;

        @Override
        @Transactional
        public void run(String... args) {
                if (produtoRepository.count() > 0) {
                        log.info("⏩ Catálogo já possui produtos, seed inicial ignorado");
                        return;
                }

                produtoRepository.saveAll(createSeedProducts());
                log.info("✅ Produtos iniciais carregados no banco com imagem placeholder");
        }

        private List<Produto> createSeedProducts() {
                return List.of(
                                createProduto("Whey Protein Isolado 900g", CategoriaEnum.SUPLEMENTOS,
                                                new BigDecimal("189.90"),
                                                new BigDecimal("249.90"), 50, true, 24L,
                                                "Whey protein isolado para suporte de hipertrofia e recuperacao."),
                                createProduto("Creatina Monohidratada 300g", CategoriaEnum.SUPLEMENTOS,
                                                new BigDecimal("69.90"),
                                                new BigDecimal("89.90"), 80, true, 42L,
                                                "Creatina pura para aumento de forca e desempenho."),
                                createProduto("Pre-Treino Explosion 300g", CategoriaEnum.SUPLEMENTOS,
                                                new BigDecimal("79.90"),
                                                null, 35, true, 18L,
                                                "Pre-treino com foco em energia e concentracao."),
                                createProduto("Camiseta Dry Fit Performance", CategoriaEnum.ROUPAS_ACADEMIA,
                                                new BigDecimal("79.90"),
                                                new BigDecimal("99.90"), 100, false, 12L,
                                                "Camiseta dry fit leve, respiravel e versatil para treino."),
                                createProduto("Tenis Running Pro Max", CategoriaEnum.CALCADOS_ESPORTIVOS,
                                                new BigDecimal("399.90"),
                                                new BigDecimal("499.90"), 30, true, 56L,
                                                "Tenis de corrida com amortecimento premium."),
                                createProduto("Tenis Training All Day", CategoriaEnum.CALCADOS_ESPORTIVOS,
                                                new BigDecimal("279.90"),
                                                null, 35, false, 28L,
                                                "Tenis multiuso para treino funcional e dia a dia."));
        }

        private Produto createProduto(String nome, CategoriaEnum categoria, BigDecimal valor,
                        BigDecimal valorPromocional,
                        int quantidade, boolean emOferta, long visualizacoes, String descricao) {
                Produto produto = new Produto();
                produto.setNome(nome);
                produto.setCategoria(categoria);
                produto.setValor(valor);
                produto.setValorPromocional(valorPromocional);
                produto.setQuantidade(quantidade);
                produto.setEmOferta(emOferta);
                produto.setDescricao(descricao);
                produto.setImg(PLACEHOLDER_IMAGE);
                produto.setStatus(quantidade > 0 ? "ATIVO" : "INATIVO");
                produto.setVisualizacoes(visualizacoes);
                produto.setTipoProduto("UNICO");
                return produto;
        }
}
