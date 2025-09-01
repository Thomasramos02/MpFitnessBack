package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Services.ProdutoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Arrays;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        List<Produto> produtos = produtoService.listaTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    // Lista só produtos ATIVOS
    @GetMapping("/ativos")
    public ResponseEntity<List<Produto>> listarAtivos() {
        List<Produto> ativos = produtoService.listarProdutosAtivos();
        return ResponseEntity.ok(ativos);
    }

    // Lista só produtos INATIVOS
    @GetMapping("/inativos")
    public ResponseEntity<List<Produto>> listarInativos() {
        List<Produto> inativos = produtoService.listarProdutosInativos();
        return ResponseEntity.ok(inativos);
    }

    // Lista categorias disponíveis
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> getCategorias() {
        List<String> categorias = Arrays.asList(
                "Equipamentos",
                "Suplementos",
                "Acessórios",
                "Roupas",
                "Calçados",
                "Bebidas",
                "Snacks",
                "Geral");
        return ResponseEntity.ok(categorias);
    }

    // Busca produtos por categoria
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Produto>> getProdutosPorCategoria(@PathVariable String categoria) {
        List<Produto> produtos = produtoService.findByCategoria(categoria);
        return ResponseEntity.ok(produtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarProduto(@RequestBody Produto produto) {
        try {
            if ("COMBO".equals(produto.getTipoProduto())
                    && (produto.getItensCombo() == null || produto.getItensCombo().isEmpty())) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Combos devem ter itens associados."));
            }

            if (produto.getEmOferta() && produto.getValorPromocional() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Produto em oferta deve ter um valor promocional."));
            }

            Produto produtoCriado = produtoService.criarProduto(produto);
            return ResponseEntity.status(HttpStatus.CREATED).body(produtoCriado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarProduto(@PathVariable Long id, @RequestBody Produto produto) {
        try {
            if ("COMBO".equals(produto.getTipoProduto())
                    && (produto.getItensCombo() == null || produto.getItensCombo().isEmpty())) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Combos devem ter itens associados."));
            }

            if (produto.getEmOferta() && produto.getValorPromocional() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Produto em oferta deve ter um valor promocional."));
            }

            Produto atualizado = produtoService.atualizarProduto(produto, id);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarProdutoPorId(@PathVariable Long id) {
        try {
            Produto produto = produtoService.findById(id);
            return ResponseEntity.ok(produto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Produto não encontrado"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarProduto(@PathVariable Long id,
            HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("Authorization Header: " + authHeader);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Current user roles: " + authentication.getAuthorities());

            produtoService.deletarProduto(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "ACCESS_DENIED",
                            "message", "Você não tem permissão para excluir produtos",
                            "details", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "PRODUCT_IN_USE",
                            "message", "Este produto está vinculado a pedidos existentes"));
        }
    }

    //Endpoint de upload de imagem
    @PostMapping("/{id}/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadImagem(@PathVariable Long id,
                                        @RequestParam("file") MultipartFile file) {
       try{
        String url = produtoService.uploadImagem(id, file);
        return ResponseEntity.ok(url);
       }catch(Exception e){
        return ResponseEntity.status(500).body("Erro ao enviar imagem"+ e.getMessage());
       }
    }


    // Endpoints para controle de estoque
    @PostMapping("/{id}/atualizar-estoque")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarEstoque(
            @PathVariable Long id,
            @RequestParam int quantidade) {
        try {
            produtoService.atualizarEstoque(id, quantidade);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Produto não encontrado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/atualizar-estoque-lote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarEstoqueLote(
            @RequestBody Map<Long, Integer> itens) {
        try {
            produtoService.atualizarEstoqueLote(itens);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Produto não encontrado: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/estoque")
    public ResponseEntity<?> consultarEstoque(@PathVariable Long id) {
        try {
            int estoque = produtoService.consultarEstoque(id);
            return ResponseEntity.ok(Map.of("estoque", estoque));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Produto não encontrado"));
        }
    }

    @PostMapping("/{id}/verificar-estoque")
    public ResponseEntity<?> verificarEstoque(
            @PathVariable Long id,
            @RequestParam int quantidade) {
        try {
            boolean disponivel = produtoService.verificarEstoque(id, quantidade);
            return ResponseEntity.ok(Map.of("disponivel", disponivel));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Produto não encontrado"));
        }
    }
}