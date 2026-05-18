package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.CategoriaEnum;
import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Services.ProdutoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        List<Produto> produtos = produtoService.listaTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Produto>> listarAtivos() {
        List<Produto> ativos = produtoService.listarProdutosAtivos();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<Produto>> listarInativos() {
        List<Produto> inativos = produtoService.listarProdutosInativos();
        return ResponseEntity.ok(inativos);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<Map<String, String>>> getCategorias() {
        return ResponseEntity.ok(
                Arrays.stream(CategoriaEnum.values())
                        .map(cat -> Map.of(
                                "value", cat.name(),
                                "label", cat.getDisplayName()))
                        .toList());
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Produto>> getProdutosPorCategoria(@PathVariable String categoria) {
        try {
            CategoriaEnum catEnum = CategoriaEnum.fromValue(categoria);
            List<Produto> produtos = produtoService.findByCategoria(catEnum);
            return ResponseEntity.ok(produtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarProduto(@RequestBody Produto produto) {
        Produto produtoCriado = produtoService.criarProduto(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoCriado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarProduto(@PathVariable Long id, @RequestBody Produto produto) {
        Produto atualizado = produtoService.atualizarProduto(produto, id);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarProdutoPorId(@PathVariable Long id) {
        Produto produto = produtoService.findById(id);
        return ResponseEntity.ok(produto);
    }

    @PostMapping("/{id}/visualizacao")
    public ResponseEntity<?> registrarVisualizacao(@PathVariable Long id) {
        produtoService.registrarVisualizacao(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadImagem(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Nenhum arquivo enviado"));
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Arquivo muito grande (máx 5MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens permitidas");
        }
        String fileName = file.getOriginalFilename();
        if (!fileName.matches(".*\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Extensao não permitida");
        }
        return ResponseEntity.ok(produtoService.uploadImagem(id, file));
    }

    @DeleteMapping("/{id}/imagem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarImagem(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String publicId = body != null ? body.get("publicId") : null;
        produtoService.removerImagemProduto(id, publicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/atualizar-estoque")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarEstoque(
            @PathVariable Long id,
            @RequestParam int quantidade) {
        produtoService.atualizarEstoque(id, quantidade);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/atualizar-estoque-lote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarEstoqueLote(
            @RequestBody Map<Long, Integer> itens) {
        produtoService.atualizarEstoqueLote(itens);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/estoque")
    public ResponseEntity<?> consultarEstoque(@PathVariable Long id) {
        int estoque = produtoService.consultarEstoque(id);
        return ResponseEntity.ok(Map.of("estoque", estoque));
    }

    @PostMapping("/{id}/verificar-estoque")
    public ResponseEntity<?> verificarEstoque(
            @PathVariable Long id,
            @RequestParam int quantidade) {
        boolean disponivel = produtoService.verificarEstoque(id, quantidade);
        return ResponseEntity.ok(Map.of("disponivel", disponivel));
    }
}
